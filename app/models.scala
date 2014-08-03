package models

import java.util.Date

case class User(
  id: String,
  fullname: String
)

case class Post(
  id: Long,
  title: String,
  content: String,
  postedAt: Date,
  comments: List[Comment],
  author_id: String
)

case class Comment(
  author: String,
  content: String,
  postedAt: Date,
  post_id: Long
)
