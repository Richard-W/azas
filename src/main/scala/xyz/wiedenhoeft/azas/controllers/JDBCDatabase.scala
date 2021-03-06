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

import java.sql.{ Connection, DriverManager, ResultSet, Statement }

import spray.json._
import xyz.wiedenhoeft.azas.models._

import scala.annotation.tailrec
import scala.concurrent.{ ExecutionContext, Future }

/**
 * JDBC implementation of Database
 *
 * May only work with MySQL and H2 (MySQL flavor)
 */
class JDBCDatabase extends Database {

  private val config = Config.database.jdbc
  Class.forName(config.driver) // Initialize driver with JVM classloading magic

  def withConnection[T](f: Connection ⇒ T)(implicit executor: ExecutionContext): Future[T] = Future {
    val connection = config.user match {
      case Some(user) ⇒
        config.pass match {
          case Some(pass) ⇒ DriverManager.getConnection(config.url, user, pass)
          case None       ⇒ throw new RuntimeException("Username given but no password")
        }
      case None ⇒ DriverManager.getConnection(config.url)
    }
    val rv = f(connection)
    connection.close()
    rv
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
      | priority,
      | approved,
      | info
      |FROM participants WHERE """.stripMargin + where
    )
    for (i <- params.indices) {
      stmt.setString(i + 1, params(i))
    }
    val result = stmt.executeQuery()
    resultHelper[Participant](result) { resultSet ⇒
      Participant(
        resultSet.getInt("id").toString,
        resultSet.getInt("councilId").toString,
        resultSet.getInt("priority"),
        if (resultSet.getInt("approved") == 1) true else false,
        resultSet.getString("info").parseJson.asJsObject
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
        |FROM councils WHERE """.stripMargin + where
    )
    for (i <- params.indices) {
      stmt.setString(i + 1, params(i))
    }
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

  private def mascotQuery(where: String, params: Seq[String] = Seq())(implicit conn: Connection): Seq[Mascot] = {
    val stmt = conn.prepareStatement(
      """
        |SELECT
        | id,
        | councilId,
        | fullName,
        | nickName
        |FROM mascots WHERE """.stripMargin + where
    )
    for (i <- params.indices) {
      stmt.setString(i + 1, params(i))
    }
    val result = stmt.executeQuery
    resultHelper[Mascot](result) { resultSet ⇒
      Mascot(
        resultSet.getInt("id").toString,
        resultSet.getInt("councilId").toString,
        resultSet.getString("fullName"),
        resultSet.getString("nickName")
      )
    }
  }

  override def insertParticipant(participant: Participant)(implicit executor: ExecutionContext): Future[Participant] = withConnection { conn ⇒
    val stmt = conn.prepareStatement(
      """
        |INSERT INTO participants (
        | councilId,
        | priority,
        | approved,
        | info
        |) VALUES (?, ?, ?, ?)
      """.stripMargin,
      Statement.RETURN_GENERATED_KEYS
    )
    stmt.setInt(1, participant.councilId.toInt)
    stmt.setInt(2, participant.priority)
    stmt.setInt(3, if (participant.approved) 1 else 0)
    stmt.setString(4, participant.info.compactPrint)
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
        | priority = ?,
        | approved = ?,
        | info = ?
        |WHERE id = ?
      """.stripMargin
    )
    stmt.setString(1, participant.councilId)
    stmt.setInt(2, participant.priority)
    stmt.setString(3, if (participant.approved) "1" else "0")
    stmt.setString(4, participant.info.compactPrint)
    stmt.setString(5, participant.id)
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
        |CREATE TABLE IF NOT EXISTS participants (
        | id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        | councilId INT NOT NULL,
        | priority INT NOT NULL,
        | approved INT NOT NULL,
        | info TEXT NOT NULL
        |)
      """.stripMargin
    )
    participantTable.executeUpdate()
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
    councilTable.executeUpdate()
    val mascotTable = conn.prepareStatement(
      """
        |CREATE TABLE IF NOT EXISTS mascots (
        | id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        | councilId INT NOT NULL,
        | fullName TEXT NOT NULL,
        | nickName TEXT NOT NULL
        |)
      """.stripMargin
    )
    mascotTable.executeUpdate()
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
      Statement.RETURN_GENERATED_KEYS
    )
    stmt.setString(1, council.university)
    stmt.setString(2, council.address)
    stmt.setString(3, council.email)
    stmt.setString(4, council.token)
    stmt.executeUpdate()
    val idSet = stmt.getGeneratedKeys
    if (idSet.next()) {
      council.copy(id = idSet.getInt(1).toString)
    } else {
      throw new DatabaseException("Invalid response from MySQL server")
    }
  }

  override def insertMascot(mascot: Mascot)(implicit executor: ExecutionContext): Future[Mascot] = withConnection { conn ⇒
    val stmt = conn.prepareStatement(
      """
        |INSERT INTO mascots (
        | councilId,
        | fullName,
        | nickName
        |) VALUES (?, ?, ?)
      """.stripMargin,
      Statement.RETURN_GENERATED_KEYS
    )
    stmt.setInt(1, mascot.councilId.toInt)
    stmt.setString(2, mascot.fullName)
    stmt.setString(3, mascot.nickName)
    stmt.executeUpdate()
    val idSet = stmt.getGeneratedKeys
    if (idSet.next()) {
      mascot.copy(id = idSet.getInt(1).toString)
    } else {
      throw new DatabaseException("Invalid response from MySQL server")
    }
  }

  override def findMascotByID(id: String)(implicit executor: ExecutionContext): Future[Option[Mascot]] = withConnection { implicit conn ⇒
    mascotQuery("id = ?", Seq(id)).headOption
  }

  override def updateMascot(mascot: Mascot)(implicit executor: ExecutionContext): Future[Mascot] = withConnection { conn ⇒
    val stmt = conn.prepareStatement(
      """
        |UPDATE mascots SET
        | councilId = ?,
        | fullName = ?,
        | nickName = ?
        |WHERE id = ?
      """.stripMargin
    )
    stmt.setInt(1, mascot.councilId.toInt)
    stmt.setString(2, mascot.fullName)
    stmt.setString(3, mascot.nickName)
    stmt.setInt(4, mascot.id.toInt)
    stmt.executeUpdate()
    mascot
  }

  override def deleteMascot(mascot: Mascot)(implicit executor: ExecutionContext): Future[Unit] = withConnection { conn ⇒
    val stmt = conn.prepareStatement("DELETE FROM mascots WHERE id = ?")
    stmt.setInt(1, mascot.id.toInt)
    stmt.executeUpdate()
  }

  override def findAllMascots(implicit executor: ExecutionContext): Future[Seq[Mascot]] = withConnection { implicit conn ⇒
    mascotQuery("'1' = '1'")
  }

  override def findMascotsByCouncil(council: Council)(implicit executor: ExecutionContext): Future[Seq[Mascot]] = withConnection { implicit conn ⇒
    mascotQuery("councilId = ?", Seq(council.id))
  }
}
