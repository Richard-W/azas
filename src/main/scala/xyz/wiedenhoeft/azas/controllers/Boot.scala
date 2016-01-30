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
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http

import scala.concurrent.Await
import scala.concurrent.duration._

object Boot extends App {

  val config = ConfigFactory.load

  implicit val system = ActorSystem("azas")

  var db: JDBCDatabase = null
  try {
    db = new JDBCDatabase
    Await.result(db.initializeTables(system.dispatcher), 5.seconds)
  } catch {
    case e: Exception ⇒
      Await.result(system.terminate(), 5.seconds)
      throw e
  }

  val service = system.actorOf(RestServiceActor.props(db))

  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(service, interface = "127.0.0.1", port = config.getInt("http.port"))
}
