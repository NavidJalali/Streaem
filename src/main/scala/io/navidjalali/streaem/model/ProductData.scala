package io.navidjalali.streaem.model

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class ProductData(
  name: Option[Name],
  price: Option[Price],
  description: Option[Description],
  category: Option[Category]
)

object ProductData:
  given JsonCodec[ProductData] = DeriveJsonCodec.gen
