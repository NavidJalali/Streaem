package io.navidjalali.streaem.api

import io.navidjalali.streaem.generators.Generators
import io.navidjalali.streaem.model.*
import io.navidjalali.streaem.service.*
import org.http4s.{ProductId => *, *}
import zio.*
import zio.test.*
import zio.test.Assertion.*
import io.navidjalali.streaem.syntax.Http4sZioJsonInteropSyntax.*

object GetByIdRouteSpec extends ZIOSpecDefault:
  def spec = suite("Get products by id route")(
    test("succeeds with 200") {
      check(Gen.vectorOfBounded(1, 50)(Generators.productInfo)) { products =>
        val app =
          for
            service  <- ZIO.service[Routes].map(_.app)
            expected <- Random.nextIntBounded(products.size).map(products(_))
            request =
              Request[Task](
                Method.GET,
                Uri.unsafeFromString(s"/products/${expected.id}")
              )
            response <- service.run(request)
            body     <- response.decode[ProductInfo]
          yield assertTrue(body == expected)

        app.provide(
          Routes.live,
          ProductInfoService.static(products)
        )
      }
    },
    test("fails with 404") {
      val app =
        for
          service       <- ZIO.service[Routes].map(_.app)
          nonExistentId <- Random.nextUUID.map(ProductId(_))
          request = Request[Task](
                      Method.GET,
                      Uri.unsafeFromString(s"/products/$nonExistentId")
                    )
          response <- service.run(request)
        yield assertTrue(response.status == Status.NotFound)

      app.provide(Routes.live, ProductInfoService.static(Nil))
    }
  )
