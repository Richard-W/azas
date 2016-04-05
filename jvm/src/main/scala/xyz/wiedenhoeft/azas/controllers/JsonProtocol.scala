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

import spray.json._
import xyz.wiedenhoeft.azas.models._
import xyz.wiedenhoeft.azas.views._

object JsonProtocol extends DefaultJsonProtocol {

  implicit val address = jsonFormat4(Address)
  implicit val council = jsonFormat5(Council)
  implicit val participant = jsonFormat5(Participant)
  implicit val mascot = jsonFormat4(Mascot)

  implicit val addPartRequest = jsonFormat3(AddPartRequest)
  implicit val editPartRequest = jsonFormat4(EditPartRequest)
  implicit val delPartRequest = jsonFormat2(DelPartRequest)
  implicit val getCouncilRequest = jsonFormat1(GetCouncilRequest)
  implicit val getCouncilResponse = jsonFormat3(GetCouncilResponse)
  implicit val addMascotRequest = jsonFormat3(AddMascotRequest)
  implicit val editMascotRequest = jsonFormat4(EditMascotRequest)
  implicit val delMascotRequest = jsonFormat2(DelMascotRequest)
  implicit val dumpDataRequest = jsonFormat1(DumpDataRequest)
  implicit val dumpDataResponse = jsonFormat3(DumpDataResponse)
  implicit val genericResponse = jsonFormat1(GenericResponse)
}
