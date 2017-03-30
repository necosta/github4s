/*
 * Copyright 2016-2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package github4s

import scala.concurrent.Future
import fr.hmil.roshttp._
import fr.hmil.roshttp.body.{BodyPart, BulkBodyPart}
import java.nio.ByteBuffer

import cats.implicits._
import fr.hmil.roshttp.response.SimpleHttpResponse
import fr.hmil.roshttp.util.HeaderMap
import fr.hmil.roshttp.body.Implicits._
import fr.hmil.roshttp.exceptions.HttpException

import scala.concurrent.ExecutionContext.Implicits.global
import github4s.GithubResponses.{GHResponse, GHResult, JsonParsingException, UnexpectedException}
import github4s.GithubDefaultUrls._
import github4s.Decoders._
import github4s.HttpClient.HttpCode400
import io.circe.Decoder
import io.circe.parser._
import monix.reactive.Observable

import scala.util.{Failure, Success}

case class CirceJSONBody(value: String) extends BulkBodyPart {
  override def contentType: String = s"application/json; charset=utf-8"
  override def contentData: ByteBuffer =
    ByteBuffer.wrap(value.getBytes("utf-8"))
}

trait HttpRequestBuilderExtensionJS {

  import monix.execution.Scheduler.Implicits.global

  val userAgent = {
    val name    = github4s.BuildInfo.name
    val version = github4s.BuildInfo.version
    s"$name/$version"
  }

  implicit def extensionJS: HttpRequestBuilderExtension[SimpleHttpResponse, Future] =
    new HttpRequestBuilderExtension[SimpleHttpResponse, Future] {
      def run[A](rb: HttpRequestBuilder[SimpleHttpResponse, Future])(
          implicit D: Decoder[A]): Future[GHResponse[A]] = {

        val params = rb.params.map {
          case (key, value) => s"$key=$value"
        } mkString "&"

        val request = HttpRequest(rb.url)
          .withMethod(Method(rb.httpVerb.verb))
          .withQueryStringRaw(params)
          .withHeader("content-type", "application/json")
          .withHeaders(rb.authHeader.toList: _*)
          .withHeaders(rb.headers.toList: _*)

        rb.data
          .map(d => request.send(CirceJSONBody(d)))
          .getOrElse(request.send())
          .map(toEntity[A])
          .recoverWith {
            case e =>
              Future.successful(Either.left(UnexpectedException(e.getMessage)))
          }
      }
    }

  def toEntity[A](response: SimpleHttpResponse)(implicit D: Decoder[A]): GHResponse[A] =
    response match {
      case r if r.statusCode < HttpCode400.statusCode ⇒
        decode[A](r.body).fold(
          e ⇒ Either.left(JsonParsingException(e.getMessage, r.body)),
          result ⇒
            Either.right(
              GHResult(result, r.statusCode, rosHeaderMapToRegularMap(r.headers))
          )
        )
      case r ⇒
        Either.left(
          UnexpectedException(
            s"Failed invoking get with status : ${r.statusCode}, body : \n ${r.body}"))
    }

  private def rosHeaderMapToRegularMap(
      headers: HeaderMap[String]): Map[String, IndexedSeq[String]] =
    headers.flatMap(m => Map(m._1.toLowerCase -> IndexedSeq(m._2)))

}
