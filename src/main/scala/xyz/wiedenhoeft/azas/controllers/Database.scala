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

import xyz.wiedenhoeft.azas.models._

import scala.concurrent._

trait Database {

  def insertParticipant(participant: Participant)(implicit executor: ExecutionContext): Future[Participant]

  def updateParticipant(participant: Participant)(implicit executor: ExecutionContext): Future[Participant]

  def deleteParticipant(participant: Participant)(implicit executor: ExecutionContext): Future[Unit]

  def findParticipantByID(id: String)(implicit executor: ExecutionContext): Future[Option[Participant]]

  def findParticipantByCouncil(council: Council)(implicit executor: ExecutionContext): Future[Seq[Participant]]

  def findAllParticipants(implicit executor: ExecutionContext): Future[Seq[Participant]]

  def insertCouncil(council: Council)(implicit executor: ExecutionContext): Future[Council]

  def findCouncilByID(id: String)(implicit executor: ExecutionContext): Future[Option[Council]]

  def findCouncilByToken(token: String)(implicit executor: ExecutionContext): Future[Option[Council]]

  def findAllCouncils(implicit executor: ExecutionContext): Future[Seq[Council]]
}

class DatabaseException(message: String) extends Exception(message)
