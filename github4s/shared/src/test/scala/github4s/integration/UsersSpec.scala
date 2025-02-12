/*
 * Copyright 2016-2022 47 Degrees Open Source <https://www.47deg.com>
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

package github4s.integration

import cats.effect.IO
import github4s.GHError
import github4s.Github
import github4s.domain._
import github4s.utils.{BaseIntegrationSpec, Integration}

trait UsersSpec extends BaseIntegrationSpec {

  "Users >> Get" should "return the expected login for a valid username" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).users
          .get(validUsername, headerUserAgent)
      }
      .unsafeRunSync()

    testIsRight[User](response, r => r.login shouldBe validUsername)
    response.statusCode shouldBe okStatusCode
  }

  it should "return error on Left for invalid username" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).users
          .get(invalidUsername, headerUserAgent)
      }
      .unsafeRunSync()

    testIsLeft[GHError.NotFoundError, User](response)
    response.statusCode shouldBe notFoundStatusCode
  }

  "Users >> GetAuth" should "return error on Left when no accessToken is provided" taggedAs Integration in {
    val response =
      clientResource
        .use(client => Github[IO](client).users.getAuth(headerUserAgent))
        .unsafeRunSync()

    testIsLeft[GHError.UnauthorizedError, User](response)
    response.statusCode shouldBe unauthorizedStatusCode
  }

  "Users >> GetUsers" should "return users for a valid since value" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).users
          .getUsers(validSinceInt, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsRight[List[User]](response, r => r.nonEmpty shouldBe true)
    response.statusCode shouldBe okStatusCode
  }

  it should "return an empty list when a invalid since value is provided" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).users
          .getUsers(invalidSinceInt, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsRight[List[User]](response, r => r.isEmpty shouldBe true)
    response.statusCode shouldBe okStatusCode
  }

  "Users >> GetFollowing" should "return the expected following list for a valid username" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).users
          .getFollowing(validUsername, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsRight[List[User]](response, r => r.nonEmpty shouldBe true)
    response.statusCode shouldBe okStatusCode
  }

  it should "return error on Left for invalid username" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).users
          .getFollowing(invalidUsername, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsLeft[GHError.NotFoundError, List[User]](response)
    response.statusCode shouldBe notFoundStatusCode
  }

}
