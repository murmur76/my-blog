package controllers

import java.util.Date

import models._
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError
import views._
import play.modules.reactivemongo.json.BSONFormats._
import scala.concurrent._

object Application extends Controller with MongoController {
  private def collection: JSONCollection = db.collection[JSONCollection]("posts")
  import play.api.libs.concurrent.Execution.Implicits._
  implicit val commentFormat = Json.format[Comment]
  implicit val postFormat = Json.format[Post]

  val userID = "celica212"
  val userName = "dongju"

  val postForm = Form(
    tuple(
      "title" -> nonEmptyText,
      "content" -> nonEmptyText
    )
  )

  val commentForm = Form(
    tuple(
      "author" -> nonEmptyText,
      "content" -> nonEmptyText
    )
  )

  def index = Action {
    Logger.info("Index page has been requested")
    Ok(html.index("Your new application is ready."))
  }

  def homepage = Action {
    Ok(html.homepage_index("Homepage"))
  }

  def blog = Action.async {
    val cursor = collection.find(Json.obj()).sort(Json.obj("postedAt" -> -1)).cursor[Post]
    val result = cursor.collect[Seq]()

    result.map {
      posts => {
        if (posts.isEmpty) {
          Ok(html.blog_index(None, Seq()))
        } else {
          val front: Option[(Post, User)] = Some(posts.head, User(userID, userName))
          val olders: Seq[(Post, User)] = posts.tail.map { post => (post, User(userID, userName))}

          Ok(html.blog_index(front, olders))
        }
      }
    }
  }

  def writePost = Action {
    Ok(html.blog_form(postForm))
  }

  def post = Action.async { implicit request =>
    val postData: Map[String, String] = postForm.bindFromRequest.data
    val frontPost = Post(None, postData("title"), postData("content"), new Date, List(), "celica212")

    val futureResult = collection.save(Json.toJson(frontPost))
    futureResult.map {
      _ => Redirect(routes.Application.blog)
    }
  }

  def comment(postedAt: Long) = Action.async { implicit request =>
    val commentData: Map[String, String] = commentForm.bindFromRequest.data
    val comment = Comment(commentData("author"), commentData("content"), new Date)
    val selector = Json.obj("postedAt" -> postedAt)
    val modifier = Json.obj("$push" -> Json.obj("comments" -> comment))
    val futureResult: Future[LastError] = collection.update(selector, modifier)

    futureResult.map {
      lastError => {
        if (lastError.updated != 0) {
          Logger.info("Comment has been successfully updated")
          Redirect(routes.Application.showPost(postedAt))
        } else {
          Logger.debug("Comment update failed with LastError: " + lastError.code)
          BadRequest("Cannot find following post")
        }
      }
    }
  }

  def showPost(postedAt: Long) = Action.async {
    val futureResult: Future[Option[Post]] = collection.find(Json.obj("postedAt" -> postedAt)).one[Post]
    futureResult.map {
      case Some(post) => {
        Ok(html.show_post((post, User(userID, userName))))
      }
      case None => BadRequest("No such post")
    }
  }
}
