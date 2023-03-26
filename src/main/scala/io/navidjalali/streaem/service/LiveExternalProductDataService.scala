package io.navidjalali.streaem.service

import io.navidjalali.streaem.configuration.ExternalProductDataConfig
import io.navidjalali.streaem.model.ProductData
import sttp.client3.*
import sttp.client3.httpclient.zio.SttpClient
import sttp.client3.ziojson.*
import sttp.tapir.ztapir.*
import zio.*

final case class LiveExternalProductDataService(
  config: ExternalProductDataConfig,
  backend: SttpClient
) extends ExternalProductDataService {
  override def getAll: Task[Vector[ProductData]] =
    basicRequest
      .get(uri"${config.scheme}://${config.host}:${config.port}/productdata")
      .response(asJson[Vector[ProductData]])
      .readTimeout(config.timeout)
      .send(backend)
      .flatMap(response => ZIO.fromEither(response.body))
      .onError(error =>
        ZIO.logError(s"Error while fetching product data: $error")
      )
}
