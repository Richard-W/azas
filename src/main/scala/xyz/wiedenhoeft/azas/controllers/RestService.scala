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

import spray.http.StatusCodes
import spray.routing.HttpService

trait RestService extends HttpService {

  val route =
    path("v1" / "participate") {
      complete(StatusCodes.NotImplemented)
    } ~ path("v1" / "mascot") {
      complete(StatusCodes.NotImplemented)
    } ~ path("v1" / "getcouncil") {
      complete(StatusCodes.NotImplemented)
    } ~ path("v1" / "setpriorities") {
      complete(StatusCodes.NotImplemented)
    } ~ path("v1" / "deletepart") {
      complete(StatusCodes.NotImplemented)
    } ~ path("v1" / "deletemascot") {
      complete(StatusCodes.NotImplemented)
    } ~ path("v1" / "dumpdata") {
      complete(StatusCodes.NotImplemented)
    }
}
