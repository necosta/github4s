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
import github4s.GHError.NotFoundError
import github4s.Github
import github4s.domain._
import github4s.utils.{BaseIntegrationSpec, Integration}

trait IssuesSpec extends BaseIntegrationSpec {

  "Issues >> List" should "return a list of issues" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .listIssues(validRepoOwner, validRepoName, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsRight[List[Issue]](response, r => r.nonEmpty shouldBe true)
    response.statusCode shouldBe okStatusCode
  }

  "Issues >> Get" should "return an issue which is a PR" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .getIssue(validRepoOwner, validRepoName, validPullRequestNumber, headerUserAgent)
      }
      .unsafeRunSync()

    testIsRight[Issue](response, r => r.pull_request.isDefined shouldBe true)
    response.statusCode shouldBe okStatusCode
  }

  "Issues >> Search" should "return at least one issue for a valid query" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .searchIssues(validSearchQuery, validSearchParams, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsRight[SearchIssuesResult](
      response,
      { r =>
        r.total_count > 0 shouldBe true
        r.items.nonEmpty shouldBe true
      }
    )
    response.statusCode shouldBe okStatusCode
  }

  it should "not regress github #569" taggedAs Integration in {
    clientResource
      .use { client =>
        Github[IO](client, accessToken).issues.searchIssues(
          "",
          List(
            OwnerParamInRepository("47degrees/github4s"),
            IssueTypePullRequest,
            LabelParam("bug"),
            IssueStateOpen
          )
        )
      }
      .map { response =>
        response.statusCode shouldBe okStatusCode
      }
  }

  it should "return an empty result for a non existent query string" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .searchIssues(nonExistentSearchQuery, validSearchParams, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsRight[SearchIssuesResult](
      response,
      { r =>
        r.total_count shouldBe 0
        r.items.nonEmpty shouldBe false
      }
    )
    response.statusCode shouldBe okStatusCode
  }

  "Issues >> Edit" should "edit the specified issue" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .editIssue(
            validRepoOwner,
            validRepoName,
            validIssueNumber,
            validIssueState,
            validIssueTitle,
            validIssueBody,
            None,
            validIssueLabel,
            validAssignees,
            headerUserAgent
          )
      }
      .unsafeRunSync()

    testIsRight[Issue](
      response,
      { r =>
        r.state shouldBe validIssueState
        r.title shouldBe validIssueTitle
      }
    )
    response.statusCode shouldBe okStatusCode
  }

  "Issues >> ListLabels" should "return a list of labels" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .listLabels(validRepoOwner, validRepoName, validIssueNumber, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsRight[List[Label]](response, r => r.nonEmpty shouldBe true)
    response.statusCode shouldBe okStatusCode
  }

  "Issues >> listLabelsRepository" should "return a list of labels" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .listLabelsRepository(validRepoOwner, validRepoName, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsRight[List[Label]](response, r => r.nonEmpty shouldBe true)
    response.statusCode shouldBe okStatusCode
  }

  it should "return error for an invalid repo owner" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .listLabelsRepository(invalidRepoOwner, validRepoName, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsLeft[NotFoundError, List[Label]](response)
    response.statusCode shouldBe notFoundStatusCode
  }

  it should "return error for an invalid repo name" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .listLabelsRepository(validRepoOwner, invalidRepoName, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsLeft[NotFoundError, List[Label]](response)
    response.statusCode shouldBe notFoundStatusCode
  }

  "Issues >> RemoveLabel" should "return a list of removed labels" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .removeLabel(
            validRepoOwner,
            validRepoName,
            validIssueNumber,
            validIssueLabel.head,
            headerUserAgent
          )
      }
      .unsafeRunSync()

    testIsRight[List[Label]](response, r => r.nonEmpty shouldBe true)
    response.statusCode shouldBe okStatusCode
  }

  "Issues >> CreateLabel" should "return a created label" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .createLabel(
            validRepoOwner,
            validRepoName,
            validRepoLabel,
            headerUserAgent
          )
      }
      .unsafeRunSync()

    testIsRight[Label](response, r => r.name shouldBe validRepoLabel.name)
    response.statusCode shouldBe createdStatusCode
  }

  "Issues >> UpdateLabel" should "return a updated label" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .updateLabel(
            validRepoOwner,
            validRepoName,
            validRepoLabel,
            headerUserAgent
          )
      }
      .unsafeRunSync()

    testIsRight[Label](response, r => r.name shouldBe validRepoLabel.name)
    response.statusCode shouldBe okStatusCode
  }

  "Issues >> DeleteLabel" should "return a valid status code" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .deleteLabel(
            validRepoOwner,
            validRepoName,
            validRepoLabel.name,
            headerUserAgent
          )
      }
      .unsafeRunSync()

    response.statusCode shouldBe noContentStatusCode
  }

  "Issues >> AddLabels" should "return a list of labels" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .addLabels(
            validRepoOwner,
            validRepoName,
            validIssueNumber,
            validIssueLabel,
            headerUserAgent
          )
      }
      .unsafeRunSync()

    testIsRight[List[Label]](response, r => r.nonEmpty shouldBe true)
    response.statusCode shouldBe okStatusCode
  }

  "GHIssues >> ListAvailableAssignees" should "return a list of users" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .listAvailableAssignees(validRepoOwner, validRepoName, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsRight[List[User]](response, r => r.nonEmpty shouldBe true)
    response.statusCode shouldBe okStatusCode
  }

  it should "return error for an invalid repo owner" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .listAvailableAssignees(invalidRepoOwner, validRepoName, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsLeft[NotFoundError, List[User]](response)
    response.statusCode shouldBe notFoundStatusCode
  }

  it should "return error for an invalid repo name" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .listAvailableAssignees(validRepoOwner, invalidRepoName, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsLeft[NotFoundError, List[User]](response)
    response.statusCode shouldBe notFoundStatusCode
  }

  "GHIssues >> ListMilestones" should "return a list of milestones" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .listMilestones(
            validRepoOwner,
            validRepoName,
            None,
            None,
            None,
            None,
            headerUserAgent
          )
      }
      .unsafeRunSync()

    testIsRight[List[Milestone]](response, r => r.nonEmpty shouldBe true)
    response.statusCode shouldBe okStatusCode
  }

  it should "return error for an invalid repo owner" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .listMilestones(
            invalidRepoOwner,
            validRepoName,
            None,
            None,
            None,
            None,
            headerUserAgent
          )
      }
      .unsafeRunSync()

    testIsLeft[NotFoundError, List[Milestone]](response)
    response.statusCode shouldBe notFoundStatusCode
  }

  it should "return error for an invalid repo name" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .listMilestones(validRepoOwner, invalidRepoName, None, None, None, None, headerUserAgent)
      }
      .unsafeRunSync()

    testIsLeft[NotFoundError, List[Milestone]](response)
    response.statusCode shouldBe notFoundStatusCode
  }

  "GHIssues >> GetMilestone" should "return a milestone for a valid number" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .getMilestone(
            validRepoOwner,
            validRepoName,
            validMilestoneNumber,
            headerUserAgent
          )
      }
      .unsafeRunSync()

    testIsRight[Milestone](response, r => r.number shouldBe validMilestoneNumber)
    response.statusCode shouldBe okStatusCode
  }

  it should "return error for an invalid number" taggedAs Integration in {
    val response = clientResource
      .use { client =>
        Github[IO](client, accessToken).issues
          .getMilestone(
            validRepoOwner,
            validRepoName,
            invalidMilestoneNumber,
            headerUserAgent
          )
      }
      .unsafeRunSync()

    testIsLeft[NotFoundError, Milestone](response)
    response.statusCode shouldBe notFoundStatusCode
  }

}
