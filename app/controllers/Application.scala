package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import java.util.Date
import models._
import views._
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent.Future
import scala.util.Properties


object Application extends Controller with MongoController {
  val userID = "celica212"
  val userName = "dongju"
  var count: Long = 3

  private def collection: JSONCollection = db.collection[JSONCollection]("posts")

  def counter: Long = {
    count += 1
    count
  }

  var postForm = Form(
    tuple(
      "title" -> nonEmptyText,
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
    import play.api.libs.concurrent.Execution.Implicits._
    implicit val commentFormat = Json.format[Comment]
    implicit val postFormat = Json.format[Post]

    val cursor = collection.find(Json.obj()).cursor[Post]
    val result = cursor.collect[Seq]()

    result.map {
      posts => {
        val front: Option[(Post, User)] = posts.headOption.map { post => (post, User(userID, userName)) }
        val olders: Seq[(Post, User)] = posts.tail.map { post => (post, User(userID, userName)) }

        Ok(html.blog_index(front, olders))
      }
    }
  }

  def writePost = Action {
    Ok(html.blog_form(postForm))
  }

  def post = Action.async { implicit request =>
    import play.api.libs.concurrent.Execution.Implicits._
    implicit val commentFormat = Json.format[Comment]
    implicit val postFormat = Json.format[Post]

    val postData: Map[String, String] = postForm.bindFromRequest.data
    val frontPost = Post(counter, postData("title"), postData("content"), new Date, List(), "celica212")

    val futureResult = collection.save(Json.toJson(frontPost))
    futureResult.map {
      _ => Redirect(routes.Application.blog)
    }
  }

  def showPost(id: Long) = Action {
    BadRequest("No Such Post")
  }
}
