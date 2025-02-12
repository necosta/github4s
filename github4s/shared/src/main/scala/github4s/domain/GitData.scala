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

package github4s.domain

sealed trait RefMetadata {
  def sha: String
  def url: String
}

final case class RefInfo(sha: String, url: String) extends RefMetadata

final case class RefObject(`type`: String, sha: String, url: String) extends RefMetadata

final case class Ref(ref: String, node_id: String, url: String, `object`: RefObject)

final case class RefCommit(
    sha: String,
    url: String,
    author: RefAuthor,
    committer: RefAuthor,
    message: String,
    tree: RefInfo,
    parents: List[RefInfo]
)

final case class Tag(
    tag: String,
    sha: String,
    url: String,
    message: String,
    tagger: RefAuthor,
    `object`: RefObject
)

final case class RefAuthor(date: String, name: String, email: String)

sealed abstract class TreeData extends Product with Serializable {
  def path: String
  def mode: String
  def `type`: String
}

final case class TreeDataSha(path: String, mode: String, `type`: String, sha: String)
    extends TreeData

final case class TreeDataBlob(path: String, mode: String, `type`: String, content: String)
    extends TreeData

final case class TreeResult(
    sha: String,
    url: String,
    tree: List[TreeDataResult],
    truncated: Option[Boolean] = None
) extends RefMetadata

final case class TreeDataResult(
    path: String,
    mode: String,
    `type`: String,
    sha: String,
    url: String,
    size: Option[Int] = None
)

final case class NewCommitRequest(
    message: String,
    tree: String,
    parents: List[String],
    author: Option[RefAuthor] = None
)

final case class BlobContent(
    url: String,
    sha: String,
    size: Int,
    content: Option[String] = None,
    encoding: Option[String] = None
)

final case class NewBlobRequest(content: String, encoding: Option[String] = None)

final case class NewTreeRequest(tree: List[TreeData], base_tree: Option[String] = None)

final case class CreateReferenceRequest(ref: String, sha: String)

final case class UpdateReferenceRequest(sha: String, force: Boolean)

final case class NewTagRequest(
    tag: String,
    message: String,
    `object`: String,
    `type`: String,
    tagger: Option[RefAuthor] = None
)
