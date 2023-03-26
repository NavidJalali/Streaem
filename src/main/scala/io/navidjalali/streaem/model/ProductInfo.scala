package io.navidjalali.streaem.model

import zio.json.{DeriveJsonCodec, JsonCodec}
import io.navidjalali.streaem.api.Filter
import io.navidjalali.streaem.model.*

import java.util.UUID
import scala.util.Try

final case class ProductInfo(
  id: ProductId,
  data: ProductData,
  quantity: Quantity
)

object ProductInfo:
  given JsonCodec[ProductInfo] = DeriveJsonCodec.gen

  object Filters:
    val name =
      Filter.queryParam[String, Name, ProductInfo]("name", Name.apply)(
        (name, productInfo) => productInfo.data.name.contains(name)
      )

    val maxPrice =
      Filter.queryParamValidated[BigDecimal, Price, ProductInfo](
        "maxPrice",
        Price.apply
      )((price, productInfo) => productInfo.data.price.exists(_ <= price))

    val minPrice =
      Filter.queryParamValidated[BigDecimal, Price, ProductInfo](
        "minPrice",
        Price.apply
      )((price, productInfo) => productInfo.data.price.exists(_ >= price))

    val category =
      Filter.queryParam[String, Category, ProductInfo](
        "category",
        Category.apply
      )((category, productInfo) => productInfo.data.category.contains(category))

    val minQuantity =
      Filter.queryParamValidated[Int, Quantity, ProductInfo](
        "minQuantity",
        Quantity.apply
      )((quantity, productInfo) => productInfo.quantity >= quantity)

    val maxQuantity =
      Filter.queryParamValidated[Int, Quantity, ProductInfo](
        "maxQuantity",
        Quantity.apply
      )((quantity, productInfo) => productInfo.quantity <= quantity)

    val id =
      Filter.queryParam[UUID, ProductId, ProductInfo](
        "id",
        ProductId.apply
      )((id, productInfo) => productInfo.id == id)
