package io.navidjalali.streaem

import io.navidjalali.streaem.api.Routes
import io.navidjalali.streaem.configuration.ExternalProductDataConfig
import io.navidjalali.streaem.service.*
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.*
import zio.stream.*
import zio.interop.catz.*
import sttp.apispec.openapi.circe.yaml.*
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter

object Main extends ZIOAppDefault:
  private def writeSwagger(routes: Routes): Task[Unit] = ZStream
    .fromIterable(
      OpenAPIDocsInterpreter()
        .toOpenAPI(
          routes.all.map(_.endpoint),
          "Streaem Home Assignment",
          "0.0.1"
        )
        .toYaml
        .getBytes
    )
    .run(ZSink.fromFileName("api.yaml"))
    .unit

  val server = for
    executor <- ZIO.executor
    routes   <- ZIO.service[Routes]
    _ <- BlazeServerBuilder[Task]
           .withExecutionContext(executor.asExecutionContext)
           .bindHttp(8080, "0.0.0.0")
           .withHttpApp(routes.app)
           .serve
           .compile
           .drain
  yield ()

  def run =
    server
      .provide(
        Routes.live,
        ProductInfoService.fromExternal,
        ExternalProductDataConfig.live,
        ExternalProductDataService.live,
        HttpClientZioBackend.layer()
      )
