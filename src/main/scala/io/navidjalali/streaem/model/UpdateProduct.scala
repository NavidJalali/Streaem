package io.navidjalali.streaem.model

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class UpdateProduct private (
  name: Option[Name],
  description: Option[Description],
  price: Option[Price],
  category: Option[Category]
) {
  def apply(data: ProductData): ProductData =
    data.copy(
      name = name,
      description = description,
      price = price,
      category = category
    )
}

object UpdateProduct:
  given JsonCodec[UpdateProduct] =
    DeriveJsonCodec
      .gen[UpdateProduct]
