package models

import java.util.Date

import reactivemongo.bson.BSONObjectID

case class User(
  id: String,
  fullname: String
)

case class Post(
  _id: Option[BSONObjectID],
  title: String,
  content: String,
  postedAt: Date,
  var comments: List[Comment],
  author_id: String
)

case class Comment(
  author: String,
  content: String,
  commentedAt: Date
)
