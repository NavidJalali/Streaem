package io.navidjalali.streaem.service

import io.navidjalali.streaem.api.Filter
import io.navidjalali.streaem.model.*
import zio.{UIO, ZIO}
import zio.stm.{TMap, ZSTM}

final case class InMemoryProductInfoService(
  products: TMap[ProductId, ProductInfo]
) extends ProductInfoService:
  override def get(id: ProductId): UIO[Option[ProductInfo]] =
    products.get(id).commit

  override def getAll(filter: Filter[ProductInfo]): UIO[Seq[ProductInfo]] =
    products.values
      .map(_.filter(filter.apply))
      .commit

  override def update(
    id: ProductId,
    patch: UpdateProduct
  ): UIO[Option[ProductInfo]] =
    products
      .get(id)
      .flatMap {
        case Some(product) =>
          val updated = product.copy(data = patch(product.data))
          products
            .put(id, updated)
            .as(updated)
            .asSome
        case None => ZSTM.none
      }
      .commit

  override def setQuantity(
    id: ProductId,
    quantity: Quantity
  ): UIO[Option[ProductInfo]] =
    products
      .get(id)
      .flatMap {
        case Some(product) =>
          val updated = product.copy(quantity = quantity)
          products
            .put(id, updated)
            .as(updated)
            .asSome
        case None => ZSTM.none
      }
      .commit
