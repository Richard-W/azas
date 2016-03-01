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
package xyz.wiedenhoeft.azas.models

case class Address(
    street: String,
    zipCode: String,
    city: String,
    country: String
) {
  def stringify(): String = {
    def escape(str: String): String = {
      str.replace(',', '.')
    }
    escape(street) + ", " + escape(zipCode) + ", " + escape(city) + ", " + escape(country)
  }
}

object Address extends ((String, String, String, String) ⇒ Address) {
  def fromString(addressString: String): Address = {
    val split = addressString.split(", ")
    if (split.length != 4) {
      Address("","","","")
    } else {
      Address(
        split(0),
        split(1),
        split(2),
        split(3)
      )
    }
  }
}
