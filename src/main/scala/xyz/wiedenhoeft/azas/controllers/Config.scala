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

import com.typesafe.config._

import scala.collection.JavaConversions._

/**
 * Singleton object that provides easy access to configuration parameters
 */
object Config {
  private lazy val config = ConfigFactory.load().getConfig("azas")

  /**
   * Version of this project
   */
  lazy val projectVersion = getClass.getPackage.getImplementationVersion

  /**
   * Values regulating database access
   */
  object database {
    private lazy val config = Config.config.getConfig("database")

    /**
     * Database driver
     */
    lazy val driver = config.getString("driver")

    /**
     * JDBC config
     */
    object jdbc {
      private lazy val config = database.config.getConfig("jdbc")

      /**
       * JDBC driver
       */
      lazy val driver = config.getString("driver")

      /**
       * Database URI
       */
      lazy val url = config.getString("url")

      /**
       * Database user
       */
      lazy val user = if (config.hasPath("user")) Some(config.getString("user")) else None

      /**
       * Database password
       */
      lazy val pass = if (config.hasPath("pass")) Some(config.getString("pass")) else None
    }
  }

  /**
   * Values regulating API access
   */
  object api {
    private lazy val config = Config.config.getConfig("api")

    /**
     * Whether it is allowed to add or delete using the API
     */
    lazy val allowAdd = config.getBoolean("allowAdd")

    /**
     * Whether it is allowed to edit objects using the API
     */
    lazy val allowEdit = config.getBoolean("allowEdit")

    /**
     * Master password for privileged API access
     */
    lazy val masterPassword = config.getString("masterPassword")
  }

  /**
   * Values for http
   */
  object http {
    private lazy val config = Config.config.getConfig("http")

    /**
     * Port spray listens on
     */
    lazy val port = config.getInt("port")
  }

  /**
   * Values defining the database scheme
   */
  object scheme {
    private lazy val config = Config.config.getConfig("scheme")

    case class Field(
      name: String,
      field: String,
      ty: String,
      options: Option[Seq[String]]
    )

    /**
     * Defined types for composing participant info
     */
    lazy val types: Map[String, Seq[Field]] = {
      (config.getObject("types").keySet map { key ⇒
        val fields = config.getConfig("types").getConfigList(key) map { list ⇒
          Field(
            name = list.getString("name"),
            field = list.getString("field"),
            ty = list.getString("type"),
            options = if (list.hasPath("options")) Some(list.getStringList("options")) else None
          )
        }
        (key, fields)
      }).toMap
    }

    /**
     * Root type for participant info
     */
    lazy val participantType = config.getString("participantType")
  }

  /**
   * Meta info about the deployment
   */
  object meta {
    lazy val config = Config.config.getConfig("meta")

    lazy val title = config.getString("title")
    lazy val numDisplayedParticipantFields = config.getInt("numDisplayedParticipantFields")
  }
}
