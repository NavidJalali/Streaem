package io.navidjalali.streaem.api

import io.navidjalali.streaem.generators.Generators
import io.navidjalali.streaem.service.*
import zio.*
import zio.test.*
import zio.test.Assertion.*
import org.http4s.*
import org.http4s.implicits.*
import zio.interop.catz.*
import io.navidjalali.streaem.model.*
import io.navidjalali.streaem.service.*
import sttp.tapir.json.zio.*
import sttp.client3.httpclient.zio.HttpClientZioBackend
import io.navidjalali.streaem.syntax.Http4sZioJsonInteropSyntax.*

import scala.language.postfixOps

object GetAllRouteSpec extends ZIOSpecDefault:

  def spec =
    suite("Get all products route")(
      test("succeeds with 200") {
        check(Gen.vectorOf(Generators.productData)) { products =>

          val app = for
            service <- ZIO.service[Routes].map(_.app)
            request =
              Request[Task](Method.GET, Uri.unsafeFromString("/products"))
            response <- service.run(request)
            body     <- response.decode[Vector[ProductInfo]]
          yield assertTrue(response.status == Status.Ok) &&
            assert(body.map(_.data))(hasSameElements(products)) &&
            assertTrue(body.map(_.quantity) == Vector.fill(products.size)(0))

          app
            .provide(
              Routes.live,
              ProductInfoService.fromExternal,
              InMemoryExternalProductDataService.make(products)
            )
        }
      },
      test("can filter by name when product exists") {
        val gen = for
          name <- Gen.alphaNumericString.map(Name(_))
          special <-
            Generators.productInfo.map(info =>
              info.copy(data = info.data.copy(name = Some(name)))
            )
          others <- Gen.vectorOf(Generators.productInfo)
        yield (name, special, special +: others)

        check(gen) { case (name, special, all) =>
          val app = for
            service <- ZIO.service[Routes].map(_.app)
            request =
              Request[Task](
                Method.GET,
                Uri.unsafeFromString(s"/products?name=$name")
              )
            response <- service.run(request)
            body     <- response.decode[Vector[ProductInfo]]
          yield assertTrue(response.status == Status.Ok) &&
            assertTrue(body.length > 0) &&
            assertTrue(body.forall(_.data.name.contains(name)))

          app
            .provide(
              Routes.live,
              ProductInfoService.static(all)
            )
        }
      },
      test("can filter by name when product does not exist") {
        check(Gen.vectorOf(Generators.productInfo)) { all =>
          val app = for
            service <- ZIO.service[Routes].map(_.app)
            request =
              Request[Task](
                Method.GET,
                Uri.unsafeFromString(s"/products?name=doesnotexist")
              )
            response <- service.run(request)
            body     <- response.decode[Vector[ProductInfo]]
          yield assertTrue(response.status == Status.Ok) &&
            assertTrue(body.length == 0)

          app
            .provide(
              Routes.live,
              ProductInfoService.static(all)
            )
        }
      },
      test("can filter by minimum stock quantity") {
        check(Generators.quantity, Gen.vectorOf(Generators.productInfo)) {
          case (quantity, all) =>
            val app = for
              service <- ZIO.service[Routes].map(_.app)
              request =
                Request[Task](
                  Method.GET,
                  Uri.unsafeFromString(s"/products?minQuantity=$quantity")
                )
              response <- service.run(request)
              body     <- response.decode[Vector[ProductInfo]]
            yield assertTrue(response.status == Status.Ok) &&
              assertTrue(body.forall(_.quantity >= quantity))

            app
              .provide(
                Routes.live,
                ProductInfoService.static(all)
              )
        }
      },
      test("400 if minQuantity is negative") {
        val app = for
          service <- ZIO.service[Routes].map(_.app)
          request =
            Request[Task](
              Method.GET,
              Uri.unsafeFromString(s"/products?minQuantity=-1")
            )
          response <- service.run(request)
        yield assertTrue(response.status == Status.BadRequest)

        app
          .provide(
            Routes.live,
            ProductInfoService.static(Vector.empty)
          )
      },
      test("can filter by maximum stock quantity") {
        check(Generators.quantity, Gen.vectorOf(Generators.productInfo)) {
          case (quantity, all) =>
            val app = for
              service <- ZIO.service[Routes].map(_.app)
              request =
                Request[Task](
                  Method.GET,
                  Uri.unsafeFromString(s"/products?maxQuantity=$quantity")
                )
              response <- service.run(request)
              body     <- response.decode[Vector[ProductInfo]]
            yield assertTrue(response.status == Status.Ok) &&
              assertTrue(body.forall(_.quantity <= quantity))

            app
              .provide(
                Routes.live,
                ProductInfoService.static(all)
              )
        }
      },
      test("400 if maxQuantity is negative") {
        val app = for
          service <- ZIO.service[Routes].map(_.app)
          request =
            Request[Task](
              Method.GET,
              Uri.unsafeFromString(s"/products?maxQuantity=-1")
            )
          response <- service.run(request)
        yield assertTrue(response.status == Status.BadRequest)

        app
          .provide(
            Routes.live,
            ProductInfoService.static(Vector.empty)
          )
      },
      test("can filter by minimum price") {
        check(Generators.price, Gen.vectorOf(Generators.productInfo)) {
          case (price, all) =>
            val app = for
              service <- ZIO.service[Routes].map(_.app)
              request =
                Request[Task](
                  Method.GET,
                  Uri.unsafeFromString(s"/products?minPrice=$price")
                )
              response <- service.run(request)
              body     <- response.decode[Vector[ProductInfo]]
            yield assertTrue(response.status == Status.Ok) &&
              assertTrue(body.forall(_.data.price.forall(_ >= price)))

            app
              .provide(
                Routes.live,
                ProductInfoService.static(all)
              )
        }
      },
      test("400 if minPrice is negative") {
        val app = for
          service <- ZIO.service[Routes].map(_.app)
          request =
            Request[Task](
              Method.GET,
              Uri.unsafeFromString(s"/products?minPrice=-1")
            )
          response <- service.run(request)
        yield assertTrue(response.status == Status.BadRequest)

        app
          .provide(
            Routes.live,
            ProductInfoService.static(Vector.empty)
          )
      },
      test("can filter by maximum price") {
        check(Generators.price, Gen.vectorOf(Generators.productInfo)) {
          case (price, all) =>
            val app = for
              service <- ZIO.service[Routes].map(_.app)
              request =
                Request[Task](
                  Method.GET,
                  Uri.unsafeFromString(s"/products?maxPrice=$price")
                )
              response <- service.run(request)
              body     <- response.decode[Vector[ProductInfo]]
            yield assertTrue(response.status == Status.Ok) &&
              assertTrue(body.forall(_.data.price.forall(_ <= price)))

            app
              .provide(
                Routes.live,
                ProductInfoService.static(all)
              )
        }
      },
      test("400 if maxPrice is negative") {
        val app = for
          service <- ZIO.service[Routes].map(_.app)
          request =
            Request[Task](
              Method.GET,
              Uri.unsafeFromString(s"/products?maxPrice=-1")
            )
          response <- service.run(request)
        yield assertTrue(response.status == Status.BadRequest)

        app
          .provide(
            Routes.live,
            ProductInfoService.static(Vector.empty)
          )
      },
      test("can filter by category") {
        check(Gen.vectorOf(Generators.productInfo)) { all =>

          val allCategories = all.flatMap(_.data.category)

          val app =
            for
              randomCategory <-
                if allCategories.isEmpty then ZIO.succeed("Electronics")
                else
                  Random
                    .nextIntBetween(0, allCategories.length)
                    .map(allCategories(_))
              service <- ZIO.service[Routes].map(_.app)
              request =
                Request[Task](
                  Method.GET,
                  Uri.unsafeFromString(s"/products?category=$randomCategory")
                )
              response <- service.run(request)
              body     <- response.decode[Vector[ProductInfo]]
            yield assertTrue(response.status == Status.Ok) &&
              assertTrue(body.forall(_.data.category.contains(randomCategory)))

          app
            .provide(
              Routes.live,
              ProductInfoService.static(all)
            )
        }
      }
    )
