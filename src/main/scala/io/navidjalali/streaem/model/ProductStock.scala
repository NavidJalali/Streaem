package io.navidjalali.streaem.model

import sttp.tapir.Schema
import zio.json.JsonCodec

opaque type Quantity = Int

object Quantity:
  def apply(value: Int): Either[String, Quantity] =
    Either.cond(value >= 0, value, "Quantity must be positive")

  def empty: Quantity = 0

  extension (quantity: Quantity)
    def value: Int                         = quantity
    infix def >=(other: Quantity): Boolean = quantity.value >= other.value
    infix def >(other: Quantity): Boolean  = quantity.value > other.value
    infix def <=(other: Quantity): Boolean = quantity.value <= other.value
    infix def <(other: Quantity): Boolean  = quantity.value < other.value

  given JsonCodec[Quantity] =
    JsonCodec.int.transformOrFail(apply, _.value)

  given Schema[Quantity] =
    Schema.schemaForInt.map(apply(_).toOption)(_.value)
