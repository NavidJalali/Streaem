package io.navidjalali.streaem.model

import sttp.tapir.Schema
import zio.json.JsonCodec

opaque type Category = String

object Category:
  def apply(value: String): Category = value

  extension (category: Category) def value: String = category

  given JsonCodec[Category] = JsonCodec.string.transform(apply, _.value)
  given Schema[Category]    = Schema.string[Category]
