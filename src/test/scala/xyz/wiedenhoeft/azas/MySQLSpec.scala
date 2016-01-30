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
package xyz.wiedenhoeft.azas

import org.scalatest._
import xyz.wiedenhoeft.azas.models.{ Mascot, PartInfo, Council, Participant }

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import xyz.wiedenhoeft.azas.controllers.JDBCDatabase

class MySQLSpec extends FlatSpec with Matchers with BeforeAndAfterAll {
  implicit val db = new JDBCDatabase
  Await.result(db.initializeTables, 5.seconds)

  override def afterAll(): Unit = {
    Await.result(db.withConnection { conn ⇒
      conn.prepareStatement("DROP ALL OBJECTS DELETE FILES").executeUpdate()
    }, 5.seconds)
  }

  val bielefeldId = Await.result(Council(
    "",
    "Universität Bielefeld", /* Some obscure nonexistant university */
    "addr",
    "email",
    "biel"
  ).insert, 5.seconds).id

  val jenaId = Await.result(Council(
    "",
    "Universität Jena",
    "addr",
    "email",
    "jena"
  ).insert, 5.seconds).id

  val testInfo = PartInfo(
    firstName = "Ralf",
    lastName = "Ralfinson",
    nickName = "Ente",
    email = "ente@example.org",
    cell = "3nt3",
    gremium = "stapf",
    tshirt = "m S",
    food = "vegan",
    allergies = "alles",
    excursion1 = "AKW",
    excursion2 = "AKW",
    excursion3 = "AKW",
    dayOfBirth = "30.02.86",
    nationality = "deutsch",
    address = "nope",
    comment = "Enteenteente",
    zaepfchen = false,
    swimmer = "Ja",
    snorer = "Motorsäge",
    arrival = "garnicht, da nichtexistent"
  )

  "Participants" should "be insertable" in {
    val inserted = Await.result(Participant(
      "",
      bielefeldId,
      0,
      false,
      testInfo
    ).insert, 5.seconds)
    Await.result(db.findAllParticipants, 5.seconds).isEmpty should be (false)
    Await.result(db.findParticipantByID(inserted.id), 5.seconds).get should be (inserted)
  }

  they should "be editable" in {
    val inserted = Await.result(Participant(
      "",
      bielefeldId,
      0,
      false,
      testInfo
    ).insert, 5.seconds)
    val updated = inserted.copy(info = testInfo.copy(firstName = "Rudolph"))
    Await.result(updated.update, 5.seconds)
    val fetched = Await.result(db.findParticipantByID(updated.id), 5.seconds).get
    fetched should be (updated)
  }

  they should "be deletable" in {
    val inserted = Await.result(Participant(
      "",
      bielefeldId,
      0,
      false,
      testInfo
    ).insert, 5.seconds)
    Await.result(db.deleteParticipant(inserted), 5.seconds)
    Await.result(db.findParticipantByID(inserted.id), 5.seconds) should be (None)
  }

  "Mascots" should "be insertable" in {
    val inserted = Await.result(Mascot(
      "",
      jenaId,
      "Frankfurter Bembel",
      "woistdasteil?"
    ).insert, 5.seconds)
    Await.result(db.findAllMascots, 5.seconds).isEmpty should be (false)
    Await.result(db.findMascotByID(inserted.id), 5.seconds).get should be (inserted)
  }

  they should "be updatable" in {
    val inserted = Await.result(Mascot(
      "",
      jenaId,
      "Frankfurter Bembel",
      "woistdasteil?"
    ).insert, 5.seconds)
    val updated = inserted.copy(nickName = "ihrklauschweine!")
    Await.result(updated.update, 5.seconds)
    Await.result(db.findMascotByID(inserted.id), 5.seconds).get should be (updated)
  }

  they should "be deletable" in {
    val inserted = Await.result(Mascot(
      "",
      jenaId,
      "Frankfurter Bembel",
      "woistdasteil?"
    ).insert, 5.seconds)
    Await.result(inserted.delete, 5.seconds)
    Await.result(db.findMascotByID(inserted.id), 5.seconds).isDefined should be (false)
  }
}
