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

import spray.http.HttpHeaders.RawHeader
import spray.routing._
import spray.http._
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._
import spray.httpx.SprayJsonSupport._
import spray.json._
import JsonProtocol._
import xyz.wiedenhoeft.azas.models.{ Mascot, Participant }
import xyz.wiedenhoeft.azas.views.v1._

import scala.concurrent._
import scala.util.{ Failure, Success, Try }

trait RestService extends HttpService {

  implicit def executor: ExecutionContext
  implicit def db: Database

  class ForbiddenException extends Exception
  class NotFoundException extends Exception
  class BadRequestException extends Exception

  def logException(e: Exception): Unit = {}

  val participantValidator: Validator
  val config = Config.api

  /**
   * Actually unnecessary but needed to satisfy the analyzer of IDEA and scalariform
   */
  def toResponse[T](t: T)(implicit marshaller: ToResponseMarshaller[T]): ToResponseMarshallable = {
    ToResponseMarshallable.isMarshallable(t)
  }

  /** Way too meta */
  def apiCall[T, V](name: String, handler: T ⇒ Future[V])(
    implicit
    um: FromRequestUnmarshaller[T],
    format: JsonFormat[T],
    marshaller: Marshaller[V]
  ): Route =
    path("v1" / name) {
      respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
        post {
          entity(as[String]) { str ⇒
            Try(str.parseJson.convertTo[T]) match {
              case Success(req) ⇒
                val response: ToResponseMarshallable = handler(req) map { res ⇒
                  toResponse(res)
                } recover {
                  case e: ForbiddenException  ⇒ toResponse(StatusCodes.Forbidden)
                  case e: NotFoundException   ⇒ toResponse(StatusCodes.NotFound)
                  case e: BadRequestException ⇒ toResponse(StatusCodes.BadRequest)
                  case e: Exception ⇒
                    logException(e)
                    toResponse(StatusCodes.InternalServerError)
                }
                complete(response)
              case Failure(f) ⇒
                respondWithStatus(StatusCodes.BadRequest) {
                  complete("Invalid JSON request: " + f.getMessage)
                }
            }
          }
        } ~ {
          complete("This should be a POST request")
        }
      }
    }

  val route =
    apiCall("addpart", handleAddPart) ~
      apiCall("editpart", handleEditPart) ~
      apiCall("delpart", handleDelPart) ~
      apiCall("addmascot", handleAddMascot) ~
      apiCall("editmascot", handleEditMascot) ~
      apiCall("delmascot", handleDelMascot) ~
      apiCall("getcouncil", handleGetCouncil) ~
      apiCall("dumpdata", handleDumpData) ~
      apiCall("metainfo", handleMetaInfo) ~
      respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
        get {
          pathPrefix("assets")(getFromResourceDirectory("assets")) ~
            path("") {
              getFromResource("assets/html/index.html")
            }
        } ~ {
          complete(StatusCodes.NotFound)
        }
      }

  def handleAddPart(req: AddPartRequest): Future[GenericResponse] = {
    if (!config.allowAdd) return Future.failed(new ForbiddenException)
    if (!participantValidator.validate(req.info)) return Future.failed(new BadRequestException)
    db.findCouncilByToken(req.token) flatMap {
      case None ⇒ throw new ForbiddenException
      case Some(council) ⇒
        Participant(
          "",
          council.id,
          req.priority.getOrElse(0),
          false,
          req.info
        ).insert map { inserted ⇒
            GenericResponse(Some(inserted.id))
          }
    }
  }

  def handleEditPart(req: EditPartRequest): Future[GenericResponse] = {
    if (!config.allowEdit) return Future.failed(new ForbiddenException)
    if (!participantValidator.validate(req.info)) return Future.failed(new BadRequestException)
    db.findCouncilByToken(req.token) flatMap {
      case None ⇒ throw new ForbiddenException
      case Some(council) ⇒
        db.findParticipantByID(req.id) flatMap {
          case None ⇒ throw new NotFoundException
          case Some(participant) ⇒
            if (participant.councilId != council.id) {
              throw new ForbiddenException
            } else {
              participant.copy(priority = req.priority, info = req.info).update map { _ ⇒
                GenericResponse()
              }
            }
        }
    }
  }

  def handleDelPart(req: DelPartRequest): Future[GenericResponse] = {
    if (!config.allowAdd) return Future.failed(new ForbiddenException)
    db.findCouncilByToken(req.token) flatMap {
      case None ⇒ throw new ForbiddenException
      case Some(council) ⇒
        db.findParticipantByID(req.id) flatMap {
          case None ⇒ throw new NotFoundException
          case Some(participant) ⇒
            if (participant.councilId != council.id) {
              throw new ForbiddenException
            } else {
              participant.delete map { _ ⇒
                GenericResponse()
              }
            }
        }
    }
  }

  def handleGetCouncil(req: GetCouncilRequest): Future[GetCouncilResponse] = {
    db.findCouncilByToken(req.token) flatMap {
      case None ⇒ throw new ForbiddenException
      case Some(council) ⇒
        db.findParticipantByCouncil(council) flatMap { participants ⇒
          db.findMascotsByCouncil(council) map { mascots ⇒
            GetCouncilResponse(council, participants.sortBy(_.priority), mascots)
          }
        }
    }
  }

  def handleAddMascot(req: AddMascotRequest): Future[GenericResponse] = {
    db.findCouncilByToken(req.token) flatMap {
      case None ⇒ throw new ForbiddenException
      case Some(council) ⇒
        Mascot(
          "",
          council.id,
          req.fullName,
          req.nickName
        ).insert map { inserted ⇒
            GenericResponse(Some(inserted.id))
          }
    }
  }

  def handleEditMascot(req: EditMascotRequest): Future[GenericResponse] = {
    db.findCouncilByToken(req.token) flatMap {
      case None ⇒ throw new ForbiddenException
      case Some(council) ⇒
        db.findMascotByID(req.id) flatMap {
          case None ⇒ throw new NotFoundException
          case Some(mascot) ⇒
            if (council.id != mascot.councilId) {
              throw new ForbiddenException
            } else {
              mascot.copy(fullName = req.fullName, nickName = req.nickName).update map { _ ⇒
                GenericResponse()
              }
            }
        }
    }
  }

  def handleDelMascot(req: DelMascotRequest): Future[GenericResponse] = {
    db.findCouncilByToken(req.token) flatMap {
      case None ⇒ throw new ForbiddenException
      case Some(council) ⇒
        db.findMascotByID(req.id) flatMap {
          case None ⇒ throw new NotFoundException
          case Some(mascot) ⇒
            if (council.id != mascot.councilId) {
              throw new ForbiddenException
            } else {
              mascot.delete map { _ ⇒
                GenericResponse()
              }
            }
        }
    }
  }

  def handleDumpData(req: DumpDataRequest): Future[DumpDataResponse] = {
    if (config.masterPassword != req.password) {
      Future.failed(new ForbiddenException)
    } else {
      db.findAllCouncils flatMap { councils ⇒
        db.findAllParticipants flatMap { participants ⇒
          db.findAllMascots map { mascots ⇒
            DumpDataResponse(councils, participants, mascots)
          }
        }
      }
    }
  }

  def handleMetaInfo(req: GenericRequest): Future[MetaInfoResponse] = {
    Future.successful(MetaInfoResponse(
      title = Config.meta.title,
      participantType = Config.scheme.participantType,
      types = JsObject(Config.scheme.types map {
        case (ty, fields) ⇒
          (ty, JsArray((fields map { _.toJson }).toVector))
      }),
      numDisplayedParticipantFields = Config.meta.numDisplayedParticipantFields,
      allowAdd = Config.api.allowAdd,
      allowEdit = Config.api.allowEdit
    ))
  }
}

