package io.navidjalali.streaem.model

import sttp.tapir.Schema
import zio.json.JsonCodec

import scala.annotation.targetName

opaque type Price = BigDecimal

object Price:
  def apply(value: BigDecimal): Either[String, Price] =
    if value >= 0 then Right(value) else Left("Price must be positive or zero")

  extension (price: Price)
    def value: BigDecimal               = price
    infix def >=(other: Price): Boolean = price.value >= other.value
    infix def <=(other: Price): Boolean = price.value <= other.value
    infix def >(other: Price): Boolean  = price.value > other.value
    infix def <(other: Price): Boolean  = price.value < other.value

  given JsonCodec[Price] =
    JsonCodec.scalaBigDecimal.transformOrFail(apply, _.value)

  given Schema[Price] =
    Schema.schemaForBigDecimal.map(apply(_).toOption)(_.value)
