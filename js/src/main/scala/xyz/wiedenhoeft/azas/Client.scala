package xyz.wiedenhoeft.azas

import scala.scalajs.js
import rxjs._

import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExport

@JSExport
class Client(url: String) {

  @JSExport
  def addParticipant(token: String, participant: js.Object, priority: Int): Observable = {
    val request = js.Dynamic.literal("token" -> token, "info" -> participant, "priority" -> priority)
    DOM.post(url + "/v1/addpart", JSON.stringify(request))
  }

  @JSExport
  def editParticipant(token: String, participant: js.Object, priority: Int, id: String): Observable = {
    val request = js.Dynamic.literal("token" -> token, "info" -> participant, "priority" -> priority, "id" -> id)
    DOM.post(url + "/v1/editpart", JSON.stringify(request))
  }

  @JSExport
  def deleteParticipant(token: String, id: String): Observable = {
    val request = js.Dynamic.literal("token" -> token, "id" -> id)
    DOM.post(url + "/v1/delpart", JSON.stringify(request))
  }

  @JSExport
  def getCouncil(token: String): Observable = {
    val request = js.Dynamic.literal("token" -> token)
    DOM.post(url + "/v1/getcouncil", JSON.stringify(request))
  }

  @JSExport
  def addMascot(token: String, fullName: String, nickName: String) = {
    val request = js.Dynamic.literal("token" -> token, "fullName" -> fullName, "nickName" -> nickName)
    DOM.post(url + "/v1/addmascot", JSON.stringify(request))
  }

  @JSExport
  def editMascot(token: String, fullName: String, nickName: String, id: String) = {
    val request = js.Dynamic.literal("token" -> token, "fullName" -> fullName, "nickName" -> nickName, "id" -> id)
    DOM.post(url + "/v1/editmascot", JSON.stringify(request))
  }

  @JSExport
  def deleteMascot(token: String, id: String) = {
    val request = js.Dynamic.literal("token" -> token, "id" -> id)
    DOM.post(url + "/v1/delmascot", JSON.stringify(request))
  }

  @JSExport
  def dumpData(password: String) = {
    val request = js.Dynamic.literal("password" -> password)
    DOM.post(url + "/v1/dumpdata", JSON.stringify(request))
  }
}
