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

import com.typesafe.config.{ ConfigFactory, ConfigObject, ConfigRenderOptions }

import scala.collection.JavaConversions._

object Config {
  private val config = ConfigFactory.load()

  def hasPath = config.hasPath _

  def getString = config.getString _
  def getInt = config.getInt _
  def getBool = config.getBoolean _

  lazy val projectVersion = getClass.getPackage.getImplementationVersion

  lazy val allowAdd = !hasPath("azas.api.allowAdd") || getBool("azas.api.allowAdd")
  lazy val allowEdit = !hasPath("azas.api.allowEdit") || getBool("azas.api.allowEdit")

  lazy val types: Map[String, Map[String, String]] = {
    def helper(ty: ConfigObject): Map[String, String] =
      (ty.keySet map { key ⇒
        (key, ty.get(key).render().replace("\"", ""))
      }).toSeq.toMap

    val types = config.getObject("azas.scheme.types")
    (types.keySet map { key ⇒
      (key, helper(config.getObject("azas.scheme.types." + key)))
    }).toSeq.toMap
  }
  lazy val participantType = config.getString("azas.scheme.participantType")
}
