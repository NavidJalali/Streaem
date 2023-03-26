package io.navidjalali.streaem.api

import io.navidjalali.streaem.generators.Generators
import io.navidjalali.streaem.model.*
import io.navidjalali.streaem.service.*
import org.http4s.{ProductId => *, *}
import zio.*
import zio.test.*
import zio.test.Assertion.*
import io.navidjalali.streaem.syntax.Http4sZioJsonInteropSyntax.*

object SetQuantityRoutes extends ZIOSpecDefault:
  def spec = suite("Set product quantity by id route")(
    test("succeeds with 200") {
      check(
        Gen.vectorOfBounded(1, 50)(Generators.productInfo),
        Generators.quantity
      ) { (products, quantity) =>
        val app =
          for
            service <- ZIO.service[Routes].map(_.app)
            random  <- Random.nextIntBounded(products.size).map(products(_))
            request =
              Request[Task](
                Method.PUT,
                Uri.unsafeFromString(s"/products/${random.id}/quantity"),
                body = quantity.toEntity
              )
            response <- service.run(request)
            body     <- response.decode[ProductInfo]
            expected  = random.copy(quantity = quantity)
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
          quantity      <- Random.nextIntBounded(1000).map(Quantity(_).toOption.get)
          request =
            Request[Task](
              Method.PUT,
              Uri.unsafeFromString(s"/products/$nonExistentId/quantity"),
              body = quantity.toEntity
            )
          response <- service.run(request)
        yield assertTrue(response.status == Status.NotFound)

      app.provide(Routes.live, ProductInfoService.static(Nil))
    }
  )
