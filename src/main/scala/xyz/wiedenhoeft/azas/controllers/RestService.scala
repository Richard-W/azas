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

import spray.routing._
import spray.http._
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._
import spray.httpx.SprayJsonSupport._

import JsonProtocol._
import xyz.wiedenhoeft.azas.models.Participant
import xyz.wiedenhoeft.azas.views._

import scala.concurrent._

trait RestService extends HttpService {

  implicit def executor: ExecutionContext
  implicit def db: Database

  val route =
    path("v1" / "addpart") {
      post {
        entity(as[AddPartRequest]) { req ⇒
          complete(handleAddPart(req))
        }
      }
    } ~ path("v1" / "editpart") {
      post {
        entity(as[EditPartRequest]) { req ⇒
          complete(handleEditPart(req))
        }
      }
    } ~ path("v1" / "delpart") {
      post {
        entity(as[DelPartRequest]) { req ⇒
          complete(handleDelPart(req))
        }
      }
    } ~ path("v1" / "addmascot") {
      complete(StatusCodes.NotImplemented)
    } ~ path("v1" / "editmascot") {
      complete(StatusCodes.NotImplemented)
    } ~ path("v1" / "delmascot") {
      complete(StatusCodes.NotImplemented)
    } ~ path("v1" / "getcouncil") {
      post {
        entity(as[GetCouncilRequest]) { req ⇒
          complete(handleGetCouncil(req))
        }
      }
    } ~ path("v1" / "setpriorities") {
      complete(StatusCodes.NotImplemented)
    } ~ path("v1" / "dumpdata") {
      complete(StatusCodes.NotImplemented)
    }

  def handleAddPart(req: AddPartRequest): Future[StatusCode] = {
    db.findCouncilByToken(req.token) flatMap {
      case None ⇒ Future.successful(StatusCodes.Unauthorized)
      case Some(council) ⇒
        Participant(
          "",
          council.id,
          false,
          req.info
        ).insert map { _ ⇒
            StatusCodes.OK
          }
    }
  }

  def handleEditPart(req: EditPartRequest): Future[StatusCode] = {
    db.findCouncilByToken(req.token) flatMap {
      case None ⇒ Future.successful(StatusCodes.Unauthorized)
      case Some(council) ⇒
        db.findParticipantByID(req.id) flatMap {
          case None ⇒ Future.successful(StatusCodes.NotFound)
          case Some(participant) ⇒
            if (participant.councilId != council.id) {
              Future.successful(StatusCodes.Unauthorized)
            } else {
              participant.copy(info = req.info).update map { _ ⇒
                StatusCodes.OK
              }
            }
        }
    }
  }

  def handleDelPart(req: DelPartRequest): Future[StatusCode] = {
    db.findCouncilByToken(req.token) flatMap {
      case None ⇒ Future.successful(StatusCodes.Unauthorized)
      case Some(council) ⇒
        db.findParticipantByID(req.id) flatMap {
          case None ⇒ Future.successful(StatusCodes.NotFound)
          case Some(participant) ⇒
            if (participant.councilId != council.id) {
              Future.successful(StatusCodes.Unauthorized)
            } else {
              participant.delete map { _ ⇒
                StatusCodes.OK
              }
            }
        }
    }
  }

  def handleGetCouncil(req: GetCouncilRequest): Future[Either[StatusCode, GetCouncilResponse]] = {
    db.findCouncilByToken(req.token) flatMap {
      case None ⇒ Future.successful(Left(StatusCodes.Unauthorized))
      case Some(council) ⇒
        db.findParticipantByCouncil(council) map { participants ⇒
          Right(GetCouncilResponse(council, participants))
        }
    }
  }
}

