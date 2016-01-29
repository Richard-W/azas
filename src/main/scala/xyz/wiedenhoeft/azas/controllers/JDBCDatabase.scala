/*
 * Copyright 2016 Richard Wiedenhöft <richard@wiedenhoeft.xyz>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package xyz.wiedenhoeft.azas.controllers

import java.sql.{ ResultSet, Connection, DriverManager }

import com.typesafe.config.ConfigFactory
import xyz.wiedenhoeft.azas.models.{ PartInfo, Participant, Council }

import scala.annotation.tailrec
import scala.concurrent.{ Future, ExecutionContext }

class JDBCDatabase extends Database {

  private val config = ConfigFactory.load
  Class.forName(config.getString("jdbc.driver"))
  private val url = config.getString("jdbc.url")
  private val userOption: Option[String] = if (config.hasPath("jdbc.user")) {
    Some(config.getString("jdbc.user"))
  } else {
    None
  }
  private val passOption: Option[String] = if (config.hasPath("jdbc.pass")) {
    Some(config.getString("jdbc.pass"))
  } else {
    None
  }

  private def withConnection[T](f: Connection ⇒ T)(implicit executor: ExecutionContext): Future[T] = Future {
    val connection = userOption match {
      case Some(user) ⇒
        passOption match {
          case Some(pass) ⇒ DriverManager.getConnection(url, user, pass)
          case None       ⇒ throw new RuntimeException("Username given but no password")
        }
      case None ⇒ DriverManager.getConnection(url)
    }
    f(connection)
  }

  @tailrec private def resultHelper[T](resultSet: ResultSet, seq: Seq[T] = Seq[T]())(f: ResultSet ⇒ T): Seq[T] = {
    if (resultSet.next()) {
      resultHelper(resultSet, seq :+ f(resultSet))(f)
    } else {
      seq
    }
  }

  private def participantQuery(where: String, params: Seq[String] = Seq())(implicit conn: Connection): Seq[Participant] = {
    val stmt = conn.prepareStatement(
      """
      |SELECT
      | id,
      | councilId,
      | approved,
      | firstName,
      | lastName,
      | nickName,
      | email,
      | cell,
      | gremium,
      | tshirt,
      | food,
      | allergies,
      | excursion1,
      | excursion2,
      | excursion3,
      | dayOfBirth,
      | nationality,
      | address,
      | comment,
      | zaepfchen,
      | swimmer,
      | snorer,
      | arrival
      |FROM participants WHERE """.stripMargin + where, params.toArray
    )
    val result = stmt.executeQuery()
    resultHelper[Participant](result) { resultSet ⇒
      Participant(
        resultSet.getInt("id").toString,
        resultSet.getInt("councilId").toString,
        if (resultSet.getInt("approved") == 1) true else false,
        PartInfo(
          resultSet.getString("firstName"),
          resultSet.getString("lastName"),
          resultSet.getString("nickName"),
          resultSet.getString("email"),
          resultSet.getString("cell"),
          resultSet.getString("gremium"),
          resultSet.getString("tshirt"),
          resultSet.getString("food"),
          resultSet.getString("allergies"),
          resultSet.getString("excursion1"),
          resultSet.getString("excursion2"),
          resultSet.getString("excursion3"),
          resultSet.getString("dayOfBirth"),
          resultSet.getString("nationality"),
          resultSet.getString("address"),
          resultSet.getString("comment"),
          if (resultSet.getInt("zaepfchen") == 1) true else false,
          resultSet.getString("swimmer"),
          resultSet.getString("snorer"),
          resultSet.getString("arrival")
        )
      )
    }
  }

  private def councilQuery(where: String, params: Seq[String] = Seq())(implicit conn: Connection): Seq[Council] = {
    val stmt = conn.prepareStatement(
      """
        |SELECT
        |  id,
        |  university,
        |  address,
        |  email,
        |  token
        |FROM councils WHERE """.stripMargin + where,
      params.toArray
    )
    val result = stmt.executeQuery()
    resultHelper[Council](result) { resultSet ⇒
      Council(
        resultSet.getInt("id").toString,
        resultSet.getString("university"),
        resultSet.getString("address"),
        resultSet.getString("email"),
        resultSet.getString("token")
      )
    }
  }

  override def insertParticipant(participant: Participant)(implicit executor: ExecutionContext): Future[Participant] = withConnection { conn ⇒
    val stmt = conn.prepareStatement(
      """
        |INSERT INTO participants (
        | councilId,
        | approved,
        | firstName,
        | lastName,
        | nickName,
        | email,
        | cell,
        | gremium,
        | tshirt,
        | food,
        | allergies,
        | excursion1,
        | excursion2,
        | excursion3,
        | dayOfBirth,
        | nationality,
        | address,
        | comment,
        | zaepfchen,
        | swimmer,
        | snorer,
        | arrival
        |) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """.stripMargin
    )
    stmt.setInt(1, participant.councilId.toInt)
    stmt.setInt(2, if (participant.approved) 1 else 0)
    stmt.setString(3, participant.info.firstName)
    stmt.setString(4, participant.info.lastName)
    stmt.setString(5, participant.info.nickName)
    stmt.setString(6, participant.info.email)
    stmt.setString(7, participant.info.cell)
    stmt.setString(8, participant.info.gremium)
    stmt.setString(9, participant.info.tshirt)
    stmt.setString(10, participant.info.food)
    stmt.setString(11, participant.info.allergies)
    stmt.setString(12, participant.info.excursion1)
    stmt.setString(13, participant.info.excursion2)
    stmt.setString(14, participant.info.excursion3)
    stmt.setString(15, participant.info.dayOfBirth)
    stmt.setString(16, participant.info.nationality)
    stmt.setString(17, participant.info.address)
    stmt.setString(18, participant.info.comment)
    stmt.setInt(19, if (participant.info.zaepfchen) 1 else 0)
    stmt.setString(20, participant.info.swimmer)
    stmt.setString(21, participant.info.snorer)
    stmt.setString(22, participant.info.arrival)
    stmt.executeUpdate()
    val idSet = stmt.getGeneratedKeys
    if (idSet.next()) {
      participant.copy(id = idSet.getInt(1).toString)
    } else {
      throw new DatabaseException("Invalid response from MySQL server")
    }
  }

  override def deleteParticipant(participant: Participant)(implicit executor: ExecutionContext): Future[Unit] = withConnection { conn ⇒
    val stmt = conn.prepareStatement("DELETE FROM participants WHERE id = ?")
    stmt.setInt(1, participant.id.toInt)
    stmt.executeUpdate()
  }

  override def findParticipantByID(id: String)(implicit executor: ExecutionContext): Future[Option[Participant]] = withConnection { implicit conn ⇒
    participantQuery("id = ?", Seq(id)).headOption
  }

  override def findAllCouncils(implicit executor: ExecutionContext): Future[Seq[Council]] = withConnection { implicit conn ⇒
    councilQuery("'1' = '1'")
  }

  override def findCouncilByID(id: String)(implicit executor: ExecutionContext): Future[Option[Council]] = withConnection { implicit conn ⇒
    councilQuery("id = ?", Seq(id)).headOption
  }

  override def findCouncilByToken(token: String)(implicit executor: ExecutionContext): Future[Option[Council]] = withConnection { implicit conn ⇒
    councilQuery("token = ?", Seq(token)).headOption
  }

  override def updateParticipant(participant: Participant)(implicit executor: ExecutionContext): Future[Participant] = withConnection { conn ⇒
    val stmt = conn.prepareStatement(
      """
        |UPDATE participants SET
        | councilId = ?,
        | approved = ?,
        | firstName = ?,
        | lastName = ?,
        | nickName = ?,
        | email = ?,
        | cell = ?,
        | gremium = ?,
        | tshirt = ?,
        | food = ?,
        | allergies = ?,
        | excursion1 = ?,
        | excursion2 = ?,
        | excursion3 = ?,
        | dayOfBirth = ?,
        | nationality = ?,
        | address = ?,
        | comment = ?,
        | zaepfchen = ?,
        | swimmer = ?,
        | snorer = ?,
        | arrival = ?
        |WHERE id = ?
      """.stripMargin,
      Array(
        participant.councilId,
        if (participant.approved) "1" else "0",
        participant.info.firstName,
        participant.info.lastName,
        participant.info.nickName,
        participant.info.email,
        participant.info.cell,
        participant.info.gremium,
        participant.info.tshirt,
        participant.info.food,
        participant.info.allergies,
        participant.info.excursion1,
        participant.info.excursion2,
        participant.info.excursion3,
        participant.info.dayOfBirth,
        participant.info.nationality,
        participant.info.address,
        participant.info.comment,
        if (participant.info.zaepfchen) "1" else "0",
        participant.info.swimmer,
        participant.info.snorer,
        participant.info.arrival,
        participant.id
      )
    )
    stmt.executeUpdate()
    participant
  }

  override def findAllParticipants(implicit executor: ExecutionContext): Future[Seq[Participant]] = withConnection { implicit conn ⇒
    participantQuery("'1' = '1'")
  }

  override def findParticipantByCouncil(council: Council)(implicit executor: ExecutionContext): Future[Seq[Participant]] = withConnection { implicit conn ⇒
    participantQuery("councilId = ?", Seq(council.id))
  }

  def initializeTables(implicit executor: ExecutionContext) = withConnection { conn ⇒
    val participantTable = conn.prepareStatement(
      """
        |CREATE TABLE IF NOT EXISTS participations (
        | id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        | councilId INT NOT NULL,
        | approved INT NOT NULL,
        | firstName TEXT NOT NULL,
        | lastName TEXT NOT NULL,
        | nickName TEXT NOT NULL,
        | email TEXT NOT NULL,
        | cell TEXT NOT NULL,
        | gremium TEXT NOT NULL,
        | tshirt TEXT NOT NULL,
        | food TEXT NOT NULL,
        | allergies TEXT NOT NULL,
        | excursion1 TEXT NOT NULL,
        | excursion2 TEXT NOT NULL,
        | excursion3 TEXT NOT NULL,
        | dayOfBirth TEXT NOT NULL,
        | nationality TEXT NOT NULL,
        | address TEXT NOT NULL,
        | comment TEXT NOT NULL,
        | zaepfchen INT NOT NULL,
        | swimmer TEXT NOT NULL,
        | snorer TEXT NOT NULL,
        | arrival TEXT NOT NULL
        |)
      """.stripMargin
    )
    if (!participantTable.execute()) throw new DatabaseException("Table participants failed to initialize")
    val councilTable = conn.prepareStatement(
      """
        |CREATE TABLE IF NOT EXISTS councils (
        | id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        | university TEXT NOT NULL,
        | address TEXT NOT NULL,
        | email TEXT NOT NULL,
        | token VARCHAR(255) NOT NULL,
        | UNIQUE(token)
        |)
      """.stripMargin
    )
    if (!councilTable.execute()) throw new DatabaseException("Table councils failed to initialize")
  }

  override def insertCouncil(council: Council)(implicit executor: ExecutionContext): Future[Council] = withConnection { conn ⇒
    val stmt = conn.prepareStatement(
      """
        |INSERT INTO councils (
        | university,
        | address,
        | email,
        | token
        |) VALUES (?, ?, ?, ?)
      """.stripMargin,
      Array(council.university, council.address, council.email, council.token)
    )
    stmt.executeUpdate()
    val idSet = stmt.getGeneratedKeys
    if (idSet.next()) {
      council.copy(id = idSet.getInt(1).toString)
    } else {
      throw new DatabaseException("Invalid response from MySQL server")
    }
  }
}
