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
import spray.json._
import xyz.wiedenhoeft.azas.controllers.JsonProtocol._
import xyz.wiedenhoeft.azas.controllers.{Config, RestService, Validator}
import xyz.wiedenhoeft.azas.models.{Mascot, Participant}
import xyz.wiedenhoeft.azas.views._
import xyz.wiedenhoeft.azas.views.v1._

import scala.concurrent._
import scala.concurrent.duration._

class RESTSpec extends FlatSpec with Matchers with ScalatestRouteTest with RestService with BeforeAndAfterEach {

  implicit val db = new MockDatabase

  def actorRefFactory = system
  val participantValidator = Validator.get(Config.scheme.participantType)

  override def beforeEach: Unit = {
    db.reset
  }

  val testInfo = JsObject(
    "name" -> JsString("Testity test"),
    "email" -> JsString("testity@example.org")
  )

  "Participants" should "be addable" in {
    Post("/v1/addpart", AddPartRequest("biel", testInfo, Some(5))) ~> route ~> check {
      response.status should be (StatusCodes.OK)
      val participant = Await.result(db.findAllParticipants, 5.seconds).head
      participant.priority should be (5)
      participant.info should be (testInfo)
    }
  }

  they should "be editable" in {
    val inserted = Await.result(Participant(
      "",
      "1",
      0,
      false,
      testInfo
    ).insert, 5.seconds)

    val editInfo = testInfo.copy(fields = testInfo.fields + ("name" -> JsString("Testity2")))
    Post("/v1/editpart", EditPartRequest(inserted.id, "biel", 5, editInfo)) ~> route ~> check {
      response.status should be (StatusCodes.OK)
      Await.result(db.findAllParticipants, 5.seconds).length should be (1)
      val fetched = Await.result(db.findParticipantByID("1"), 5.seconds).get
      fetched.info should be (editInfo)
      fetched.priority should be (5)
    }
  }

  they should "not be addable with invalid tokens" in {
    Post("/v1/addpart", AddPartRequest("nonexistant", testInfo, Some(5))) ~> route ~> check {
      response.status should be (StatusCodes.Forbidden)
      Await.result(db.findAllParticipants, 5.seconds).length should be (0)
    }
  }

  they should "not be editable with invalid tokens" in {
    Post("/v1/addpart", AddPartRequest("biel", testInfo, Some(5))) ~> route ~> check {
      Await.result(db.findAllParticipants, 5.seconds).length should be (1)
    }

    Post("/v1/editpart", EditPartRequest("1", "nonexistant", 0, testInfo.copy(fields = testInfo.fields + ("name" -> JsString("Rudolph"))))) ~> route ~> check {
      response.status should be (StatusCodes.Forbidden)
    }
  }

  they should "not be editable by other councils" in {
    Post("/v1/addpart", AddPartRequest("biel", testInfo, Some(5))) ~> route ~> check {
      Await.result(db.findAllParticipants, 5.seconds).length should be (1)
    }

    Post("/v1/editpart", EditPartRequest("1", "jena", 0, testInfo.copy(fields = testInfo.fields + ("name" -> JsString("Rudolph"))))) ~> route ~> check {
      response.status should be (StatusCodes.Forbidden)
    }
  }

  they should "be deletable" in {
    Post("/v1/addpart", AddPartRequest("biel", testInfo, Some(5))) ~> route ~> check {
      Await.result(db.findAllParticipants, 5.seconds).length should be (1)
    }

    Post("/v1/delpart", DelPartRequest("1", "biel")) ~> route ~> check {
      response.status should be (StatusCodes.OK)
      Await.result(db.findAllParticipants, 5.seconds).isEmpty should be (true)
    }
  }

  "Council information" should "be available" in {
    Post("/v1/getcouncil", GetCouncilRequest("biel")) ~> route ~> check {
      response.status should be (StatusCodes.OK)
      val council = responseAs[GetCouncilResponse]
      council.info.token should be("biel")
    }
  }

  "Mascots" should "be insertable" in {
    Post("/v1/addmascot", AddMascotRequest("biel", "irgendwas viel längeres als Kwawak", "Kwawak")) ~> route ~> check {
      response.status should be (StatusCodes.OK)
      Await.result(db.findAllMascots, 5.seconds).head.nickName should be ("Kwawak")
    }
  }

  they should "be editable" in {
    val council = Await.result(db.findCouncilByToken("biel"), 5.seconds).get
    val inserted = Await.result(Mascot(
      "",
      council.id,
      "Bla",
      "Blub"
    ).insert, 5.seconds)
    Post("/v1/editmascot", EditMascotRequest(inserted.id, council.token, "irgendwas viel längeres als Kwawak", "Kwawak")) ~> route ~> check {
      response.status should be (StatusCodes.OK)
      Await.result(db.findMascotByID(inserted.id), 5.seconds).get.nickName should be ("Kwawak")
    }
  }

  they should "be deletable" in {
    val council = Await.result(db.findCouncilByToken("biel"), 5.seconds).get
    val inserted = Await.result(Mascot(
      "",
      council.id,
      "Bla",
      "Blub"
    ).insert, 5.seconds)
    Post("/v1/delmascot", DelMascotRequest(council.token, inserted.id)) ~> route ~> check {
      response.status should be (StatusCodes.OK)
      Await.result(db.findMascotByID(inserted.id), 5.seconds) should be (None)
    }
  }

  "Data" should "be dumpable with the right password" in {
    Post("/v1/dumpdata", DumpDataRequest("testpass")) ~> route ~> check {
      response.status should be (StatusCodes.OK)
      responseAs[DumpDataResponse] shouldBe a[DumpDataResponse]
    }
  }

  it should "not be dumpable with a wrong password" in {
    Post("/v1/dumpdata", DumpDataRequest("wrongpass")) ~> route ~> check {
      response.status should be (StatusCodes.Forbidden)
    }
  }

  "Invalid participant requests" should "fail" in {
    Post("/v1/addpart", AddPartRequest("biel", JsObject("invalid" -> JsString("value")), Some(5))) ~> route ~> check {
      response.status should be (StatusCodes.BadRequest)
    }

    Post("/v1/editpart", EditPartRequest("1", "biel", 0, JsObject("invalid" -> JsString("value")))) ~> route ~> check {
      response.status should be (StatusCodes.BadRequest)
    }
  }
}
