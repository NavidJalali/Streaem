package io.navidjalali.streaem.model

import sttp.tapir.Schema
import zio.json.JsonCodec

opaque type Description = String

object Description:
  def apply(description: String): Description = description

  extension (description: Description) def value: String = description

  given JsonCodec[Description] = JsonCodec.string.transform(apply, _.value)
  given Schema[Description]    = Schema.string[Description]
