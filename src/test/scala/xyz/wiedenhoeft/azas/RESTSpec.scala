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
import spray.http._
import spray.testkit._
import spray.httpx.SprayJsonSupport._
import xyz.wiedenhoeft.azas.controllers.JsonProtocol._
import xyz.wiedenhoeft.azas.controllers.{ RestService, MockDatabase }
import xyz.wiedenhoeft.azas.models.PartInfo
import xyz.wiedenhoeft.azas.views._

import scala.concurrent._
import scala.concurrent.duration._

class RESTSpec extends FlatSpec with Matchers with ScalatestRouteTest with RestService with BeforeAndAfterEach {

  implicit val db = new MockDatabase

  def actorRefFactory = system

  override def beforeEach: Unit = {
    db.reset
  }

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

  "Participants" should "be addable" in {
    Post("/v1/addpart", AddPartRequest("biel", testInfo)) ~> route ~> check {
      response.status should be (StatusCodes.OK)
      val participant = Await.result(db.findAllParticipants, 5.seconds).head
      participant.info should be (testInfo)
    }
  }

  they should "be editable" in {
    Post("/v1/addpart", AddPartRequest("biel", testInfo)) ~> route ~> check {
      Await.result(db.findAllParticipants, 5.seconds).length should be (1)
    }

    Post("/v1/editpart", EditPartRequest("1", "biel", testInfo.copy(firstName = "Rudolph"))) ~> route ~> check {
      Await.result(db.findParticipantByID("1"), 5.seconds).get.info.firstName should be ("Rudolph")
      Await.result(db.findAllParticipants, 5.seconds).length should be (1)
    }
  }

  they should "not be addable with invalid tokens" in {
    Post("/v1/addpart", AddPartRequest("nonexistant", testInfo)) ~> route ~> check {
      response.status should be (StatusCodes.Unauthorized)
      Await.result(db.findAllParticipants, 5.seconds).length should be (0)
    }
  }

  they should "not be editable with invalid tokens" in {
    Post("/v1/addpart", AddPartRequest("biel", testInfo)) ~> route ~> check {
      Await.result(db.findAllParticipants, 5.seconds).length should be (1)
    }

    Post("/v1/editpart", EditPartRequest("1", "nonexistant", testInfo.copy(firstName = "Rudolph"))) ~> route ~> check {
      response.status should be (StatusCodes.Unauthorized)
    }
  }

  they should "not be editable by other councils" in {
    Post("/v1/addpart", AddPartRequest("biel", testInfo)) ~> route ~> check {
      Await.result(db.findAllParticipants, 5.seconds).length should be (1)
    }

    Post("/v1/editpart", EditPartRequest("1", "jena", testInfo.copy(firstName = "Rudolph"))) ~> route ~> check {
      response.status should be (StatusCodes.Unauthorized)
    }
  }

  they should "be deletable" in {
    Post("/v1/addpart", AddPartRequest("biel", testInfo)) ~> route ~> check {
      Await.result(db.findAllParticipants, 5.seconds).length should be (1)
    }

    Post("/v1/delpart", DelPartRequest("1", "biel")) ~> route ~> check {
      response.status should be (StatusCodes.OK)
      Await.result(db.findAllParticipants, 5.seconds).isEmpty should be (true)
    }
  }

  they should "have priorities" in {
    Post("/v1/addpart", AddPartRequest("biel", testInfo)) ~> route ~> check {
      response.status should be(StatusCodes.OK)
      val inserted = Await.result(db.findAllParticipants, 5.seconds).head
      inserted.priority should be (0)
    }

    Post("/v1/setpriority", SetPriorityRequest("biel", "1", 50)) ~> route ~> check {
      response.status should be(StatusCodes.OK)
      val inserted = Await.result(db.findAllParticipants, 5.seconds).head
      inserted.priority should be (50)
    }
  }

  "Council information" should "be available" in {
    Post("/v1/addpart", AddPartRequest("biel", testInfo)) ~> route ~> check {
      response.status should be (StatusCodes.OK)
    }

    Post("/v1/getcouncil", GetCouncilRequest("biel")) ~> route ~> check {
      response.status should be (StatusCodes.OK)
      val info = responseAs[GetCouncilResponse]
      info.council.token should be ("biel")
      info.participants.isEmpty should be (false)
    }
  }
}
