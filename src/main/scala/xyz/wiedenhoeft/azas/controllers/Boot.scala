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

import java.io.File

import akka.actor._
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import spray.util._
import xyz.wiedenhoeft.azas.models.Council

import scala.concurrent.duration._
import scala.io.Source
import scala.util.{ Failure, Success, Try }

/**
 * Main application
 *
 * Initializes the actor system and sets up database connections
 */
object Boot extends App {

  println("Starting up AZaS")
  System.getProperty("config.file") match {
    case null ⇒ println("WARNING: Using default configuration")
    case prop ⇒ println("Loading config file from " + prop)
  }

  implicit val system = ActorSystem("azas")
  import system.dispatcher
  implicit val timeout = Timeout(5.seconds)

  val db: JDBCDatabase = Try(new JDBCDatabase) flatMap { db ⇒
    Try(db.initializeTables.await).map(_ ⇒ db)
  } match {
    case Success(s) ⇒ s
    case Failure(f) ⇒
      system.terminate().await
      throw f
  }

  System.getProperty("database.seed") match {
    case null ⇒
    case filePath ⇒
      val file = new File(filePath)
      if (!file.exists) throw new RuntimeException("database seed file does not exist")

      import spray.json._
      case class CouncilSeed(uni: String, address: String, email: String, token: String)
      object JsonFormat extends DefaultJsonProtocol {
        implicit val councilSeedFormat = jsonFormat4(CouncilSeed)
      }
      import JsonFormat._

      val seed = Source.fromFile(file).mkString.parseJson.convertTo[Seq[CouncilSeed]]
      for (council <- seed) {
        db.insertCouncil(Council(
          "",
          council.uni,
          council.address,
          council.email,
          council.token
        )).await
      }
      System.exit(0)
  }

  val service = system.actorOf(RestServiceActor.props(db, Validator.get(Config.scheme.participantType)))
  IO(Http) ? Http.Bind(service, interface = Config.http.address.getOrElse("127.0.0.1"), port = Config.http.port)
}
