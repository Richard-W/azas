package xyz.wiedenhoeft.azas.views.v1

import spray.json.JsObject

case class MetaInfoResponse(
  title: String,
  participantType: String,
  types: JsObject
)
