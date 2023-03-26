package io.navidjalali.streaem.syntax

import org.http4s.*
import zio.json.*
import zio.*
import zio.interop.catz.*

object Http4sZioJsonInteropSyntax:
  extension (response: Response[Task])
    def decode[A: JsonDecoder] =
      response
        .as[String]
        .flatMap(raw =>
          ZIO.fromEither(raw.fromJson[A]).mapError(new RuntimeException(_))
        )

  extension [A: JsonEncoder](a: A)
    def toEntity: EntityBody[Task] =
      fs2.Stream.emits(a.toJson.getBytes)
