package io.navidjalali.streaem.api

import sttp.tapir.*
import sttp.tapir.CodecFormat.TextPlain

/**
 * An abstraction for filtering objects of type A
 */
final case class Filter[A](apply: A => Boolean) {
  infix def and(other: Filter[A]): Filter[A] =
    Filter(a => apply(a) && other.apply(a))
  infix def or(other: Filter[A]): Filter[A] =
    Filter(a => apply(a) || other.apply(a))
}

object Filter {

  /**
   * A filter that accepts all objects
   */
  private def allPass[A]: Filter[A] = Filter(_ => true)

  /**
   * Make a filter for A from a query parameter of type Raw along with a
   * validator Raw => Either[String, Typed] Lets you filter objects of type A,
   * given a value of type Typed
   */
  def queryParamValidated[Raw, Typed, A](
    paramName: String,
    paramValidate: Raw => Either[String, Typed]
  )(filterBy: (Typed, A) => Boolean)(using
    Codec[List[String], Option[Raw], TextPlain]
  ): EndpointInput.Query[Filter[A]] =
    query[Option[Raw]](paramName)
      .mapDecode[Filter[A]](
        _.fold(DecodeResult.Value(allPass[A])) { raw =>
          DecodeResult.fromEitherString(
            raw.toString,
            paramValidate(raw).map(typed => Filter(filterBy(typed, _)))
          )
        }
      )(_ => throw new RuntimeException("Good old should not happen"))

  /**
   * Make a filter for A from a query parameter of type Raw along with a
   * transformer of type Raw => Typed Lets you filter objects of type A, given a
   * value of type Typed
   */
  def queryParam[Raw, Typed, A](
    paramName: String,
    transform: Raw => Typed
  )(filterBy: (Typed, A) => Boolean)(using
    Codec[List[String], Option[Raw], TextPlain]
  ): EndpointInput.Query[Filter[A]] =
    queryParamValidated[Raw, Typed, A](paramName, Right(_).map(transform))(
      filterBy
    )
}
