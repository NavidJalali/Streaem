package io.navidjalali.streaem.api

import io.navidjalali.streaem.model.*
import io.navidjalali.streaem.service.*
import org.http4s.{HttpApp, HttpRoutes}
import sttp.capabilities.zio.ZioStreams
import sttp.model.StatusCode
import sttp.tapir.ztapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.json.*
import zio.*
import zio.interop.catz.*
import java.util.UUID

final case class Routes(productInfoService: ProductInfoService):
  private val getAllRoute: ZServerEndpoint[Any, ZioStreams] =
    endpoint.get
      .in("products")
      .in(ProductInfo.Filters.id)
      .in(ProductInfo.Filters.name)
      .in(ProductInfo.Filters.maxPrice)
      .in(ProductInfo.Filters.minPrice)
      .in(ProductInfo.Filters.category)
      .in(ProductInfo.Filters.minQuantity)
      .in(ProductInfo.Filters.maxQuantity)
      .out(jsonBody[Seq[ProductInfo]])
      .description("Get all products")
      .serverLogicSuccess(filters =>
        productInfoService.getAll(
          filters.productIterator
            .asInstanceOf[Iterator[Filter[ProductInfo]]]
            .reduce(_ and _)
        )
      )

  private val getByIdRoute: ZServerEndpoint[Any, ZioStreams] =
    endpoint.get
      .in("products" / path[UUID]("id"))
      .out(jsonBody[ProductInfo])
      .description("Get product by id")
      .errorOut(statusCode(StatusCode.NotFound))
      .serverLogicOption(id => productInfoService.get(ProductId(id)))

  private val updateProductRoute: ZServerEndpoint[Any, ZioStreams] =
    endpoint.put
      .in("products" / path[UUID]("id"))
      .in(jsonBody[UpdateProduct])
      .out(jsonBody[ProductInfo])
      .description(
        "Update product data fields by id, fields not present will be set to null"
      )
      .errorOut(statusCode(StatusCode.NotFound))
      .serverLogicOption { case (id, productInfo) =>
        productInfoService.update(ProductId(id), productInfo)
      }

  private val setQuantityRoute: ZServerEndpoint[Any, ZioStreams] =
    endpoint.put
      .in("products" / path[UUID]("id") / "quantity")
      .in(jsonBody[Quantity])
      .out(jsonBody[ProductInfo])
      .description("Update product quantity by id")
      .errorOut(statusCode(StatusCode.NotFound))
      .serverLogicOption { case (id, quantity) =>
        productInfoService.setQuantity(ProductId(id), quantity)
      }

  val all =
    List(getAllRoute, getByIdRoute, updateProductRoute, setQuantityRoute)

  val routes: HttpRoutes[Task] =
    ZHttp4sServerInterpreter[Any]().from(all).toRoutes

  val app: HttpApp[Task] = routes.orNotFound

object Routes:
  val live = ZLayer.fromFunction(apply)
