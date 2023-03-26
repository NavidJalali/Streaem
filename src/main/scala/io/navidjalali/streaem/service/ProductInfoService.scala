package io.navidjalali.streaem.service

import io.navidjalali.streaem.api.Filter
import io.navidjalali.streaem.model.*
import zio.stm.TMap
import zio.*

trait ProductInfoService {
  def getAll(filter: Filter[ProductInfo]): UIO[Seq[ProductInfo]]
  def get(id: ProductId): UIO[Option[ProductInfo]]
  def update(id: ProductId, patch: UpdateProduct): UIO[Option[ProductInfo]]
  def setQuantity(id: ProductId, quantity: Quantity): UIO[Option[ProductInfo]]
}

object ProductInfoService:
  def static(
    products: Seq[ProductInfo]
  ): ULayer[ProductInfoService] =
    ZLayer {
      for data <- TMap.fromIterable(products.map(p => p.id -> p)).commit
      yield InMemoryProductInfoService(data)
    }

  val fromExternal: ZLayer[
    ExternalProductDataService,
    Throwable,
    ProductInfoService
  ] = ZLayer.fromZIO(
    for
      productDataService <- ZIO.service[ExternalProductDataService]
      externalProducts   <- productDataService.getAll
      ids <- Random.nextUUID
               .map(ProductId(_))
               .replicateZIO(externalProducts.size)
      products =
        externalProducts
          .zip(ids)
          .map((data, id) => id -> ProductInfo(id, data, Quantity.empty))
      tmap <- TMap.fromIterable(products).commit
    yield InMemoryProductInfoService(tmap)
  )
