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

import xyz.wiedenhoeft.azas.models.{ Council, Participant }

import scala.concurrent.{ Future, ExecutionContext }

class MockDatabase extends Database {

  var participantNextID = 1
  var participants = Seq[Participant]()
  var councilNextID = 1
  var councils = Seq[Council](
    Council(
      "1",
      "Universität Bielefeld", /* Some obscure nonexistant university */
      "addr",
      "email",
      "biel"
    ), Council(
      "2",
      "Universität Jena",
      "addr",
      "email",
      "jena"
    )
  )

  override def insertParticipant(participant: Participant)(implicit executor: ExecutionContext): Future[Participant] = {
    val insert = participant.copy(id = participantNextID.toString)
    participantNextID = participantNextID + 1
    participants = participants :+ insert
    Future.successful(insert)
  }

  override def deleteParticipant(participant: Participant)(implicit executor: ExecutionContext): Future[Unit] = {
    if (participants.exists(_.id == participant.id)) {
      participants = participants.filter(_.id != participant.id)
      Future.successful(Unit)
    } else {
      throw new DatabaseException("id not found")
    }
  }

  override def findParticipantByID(id: String)(implicit executor: ExecutionContext): Future[Option[Participant]] = {
    Future.successful(participants.find(_.id == id))
  }

  override def findAllCouncils(implicit executor: ExecutionContext): Future[Seq[Council]] = {
    Future.successful(councils)
  }

  override def findCouncilByID(id: String)(implicit executor: ExecutionContext): Future[Option[Council]] = {
    Future.successful(councils.find(_.id == id))
  }

  override def findCouncilByToken(token: String)(implicit executor: ExecutionContext): Future[Option[Council]] = {
    Future.successful(councils.find(_.token == token))
  }

  override def updateParticipant(participant: Participant)(implicit executor: ExecutionContext): Future[Participant] = {
    if (participants.exists(_.id == participant.id)) {
      participants = participants.filter(_.id != participant.id) :+ participant
      Future.successful(participant)
    } else {
      throw new DatabaseException("id not found")
    }
  }

  override def findParticipantByCouncil(council: Council)(implicit executor: ExecutionContext): Future[Seq[Participant]] = {
    Future.successful(participants.filter(_.councilId == council.id))
  }

  override def findAllParticipants(implicit executor: ExecutionContext): Future[Seq[Participant]] = {
    Future.successful(participants)
  }

  def reset = {
    participants = Seq()
    participantNextID = 1
  }

  override def insertCouncil(council: Council)(implicit executor: ExecutionContext): Future[Council] = {
    val insert = council.copy(id = councilNextID.toString)
    councilNextID = councilNextID + 1
    councils = councils :+ insert
    Future.successful(insert)
  }
}
