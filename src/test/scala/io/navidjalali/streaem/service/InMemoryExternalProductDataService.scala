package io.navidjalali.streaem.service
import io.navidjalali.streaem.model.ProductData
import zio.*

final case class InMemoryExternalProductDataService(data: Vector[ProductData])
    extends ExternalProductDataService:
  override def getAll: Task[Vector[ProductData]] =
    ZIO.succeed(data)

object InMemoryExternalProductDataService:
  def make(
    initial: Vector[ProductData]
  ): ULayer[ExternalProductDataService] =
    ZLayer.succeed(InMemoryExternalProductDataService(initial))
