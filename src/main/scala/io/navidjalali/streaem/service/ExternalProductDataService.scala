package io.navidjalali.streaem.service

import io.navidjalali.streaem.configuration.ExternalProductDataConfig
import io.navidjalali.streaem.model.ProductData
import sttp.client3.httpclient.zio.SttpClient
import zio.*

trait ExternalProductDataService {
  def getAll: Task[Vector[ProductData]]
}

object ExternalProductDataService:

  def getAll: ZIO[ExternalProductDataService, Throwable, Vector[ProductData]] =
    ZIO.serviceWithZIO[ExternalProductDataService](_.getAll)

  val live: ZLayer[
    ExternalProductDataConfig & SttpClient,
    Nothing,
    LiveExternalProductDataService
  ] =
    ZLayer.fromFunction(LiveExternalProductDataService.apply)
