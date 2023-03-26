package io.navidjalali.streaem.api

import io.navidjalali.streaem.generators.Generators
import io.navidjalali.streaem.model.*
import io.navidjalali.streaem.service.*
import org.http4s.{ProductId => *, *}
import zio.*
import zio.test.*
import zio.test.Assertion.*
import io.navidjalali.streaem.syntax.Http4sZioJsonInteropSyntax.*

object UpdateRouteSpec extends ZIOSpecDefault:
  def spec = suite("Update products by id route")(
    test("succeeds with 200") {
      check(
        Gen.vectorOfBounded(1, 50)(Generators.productInfo),
        Generators.productData
      ) { (products, data) =>
        val app =
          for
            service <- ZIO.service[Routes].map(_.app)
            random <-
              Random.nextIntBounded(products.size).map(products(_))
            request =
              Request[Task](
                Method.PUT,
                Uri.unsafeFromString(s"/products/${random.id}"),
                body = data.toEntity
              )
            response <- service.run(request)
            body     <- response.decode[ProductInfo]
            expected  = random.copy(data = data)
          yield assertTrue(body == expected)

        app.provide(
          Routes.live,
          ProductInfoService.static(products)
        )
      }
    },
    test("fail with 404") {
      check(
        Gen.vectorOfBounded(1, 50)(Generators.productInfo),
        Generators.productData
      ) { (products, data) =>
        val app =
          for
            service       <- ZIO.service[Routes].map(_.app)
            nonExistingId <- Random.nextUUID.map(ProductId(_))
            request =
              Request[Task](
                Method.PUT,
                Uri.unsafeFromString(s"/products/$nonExistingId"),
                body = data.toEntity
              )
            response <- service.run(request)
          yield assertTrue(response.status == Status.NotFound)

        app.provide(
          Routes.live,
          ProductInfoService.static(products)
        )
      }
    }
  )
