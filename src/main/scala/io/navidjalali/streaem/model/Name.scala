package io.navidjalali.streaem.model

import sttp.tapir.Schema
import zio.json.JsonCodec

opaque type Name = String

object Name:
  def apply(name: String): Name = name

  extension (name: Name) def value: String = name

  given JsonCodec[Name] = JsonCodec.string.transform(apply, _.value)
  given Schema[Name]    = Schema.string[Name]
