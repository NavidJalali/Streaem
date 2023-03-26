package io.navidjalali.streaem.model

import sttp.tapir.Schema
import zio.json.JsonCodec

import java.util.UUID
import scala.util.Try

opaque type ProductId = UUID

object ProductId:
  def apply(uuid: UUID): ProductId = uuid

  extension (id: ProductId) def value: UUID = id

  given JsonCodec[ProductId] = JsonCodec.uuid.transform(apply, _.value)
  given Schema[ProductId] =
    Schema.schemaForUUID.map(raw => Some(apply(raw)))(_.value)
