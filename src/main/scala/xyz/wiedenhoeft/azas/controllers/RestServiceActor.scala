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

import akka.actor._
import spray.routing.{ RoutingSettings, ExceptionHandler }

object RestServiceActor {
  /**
   * Actor props to initialize the RestServiceActor
   *
   * @param db Initialized implementation of the db trait
   * @return Props for starting a RestServiceActor
   */
  def props(db: Database, participantValidator: Validator) = Props(classOf[RestServiceActor], db, participantValidator)
}

/**
 * Actor that handles HTTP requests
 *
 * @param db Database implementation
 */
class RestServiceActor(val db: Database, val participantValidator: Validator) extends Actor with RestService with ActorLogging {

  implicit val routingSettings = RoutingSettings.default
  implicit val exceptionHandler = ExceptionHandler.default

  /**
   * Log an exception
   *
   * @param e The exception
   */
  override def logException(e: Exception): Unit = {
    log.error(e, e.getMessage)
  }

  /**
   * Returns a partial function that handles messages to the actor
   */
  override def receive: Receive = runRoute(route)

  /**
   * Specifies the way new actors should be initialized
   */
  override def actorRefFactory: ActorRefFactory = context

  /**
   * Execution context for asynchronous operations
   */
  override def executor = context.dispatcher
}
