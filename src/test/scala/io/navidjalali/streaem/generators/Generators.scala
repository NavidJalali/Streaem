package io.navidjalali.streaem.generators

import zio.test.Gen
import io.navidjalali.streaem.model.*

object Generators:
  val name: Gen[Any, Name] =
    Gen.alphaNumericString.map(Name(_))

  val price: Gen[Any, Price] =
    Gen.bigDecimal(0, 1000).map(Price(_).toOption.get)

  val category: Gen[Any, Category] =
    Gen.alphaNumericString.map(Category(_))

  val description: Gen[Any, Description] =
    Gen.alphaNumericString.map(Description(_))

  val productData: Gen[Any, ProductData] =
    for
      n <- Gen.option(name)
      p <- Gen.option(price)
      c <- Gen.option(category)
      d <- Gen.option(description)
    yield ProductData(n, p, d, c)

  val quantity: Gen[Any, Quantity] =
    Gen.int(0, 1000).map(Quantity(_).toOption.get)

  val productId: Gen[Any, ProductId] = Gen.uuid.map(ProductId(_))

  val productInfo: Gen[Any, ProductInfo] =
    productId <*> productData <*> quantity map ProductInfo.apply.tupled
