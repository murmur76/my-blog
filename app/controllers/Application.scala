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

  def blog = Action {
    val front: Option[(Post, User)] = allposts headOption
    val olders: Seq[(Post, User)] = allposts tail

    Ok(html.blog_index(front, olders))
  }

  def writePost = Action {
    Ok(html.blog_form(postForm))
  }

  def post = Action { implicit request =>
    import play.api.libs.concurrent.Execution.Implicits._
    implicit val commentFormat = Json.format[Comment]
    implicit val postFormat = Json.format[Post]

    val postData: Map[String, String] = postForm.bindFromRequest.data
    val frontPost = Post(counter, postData("title"), postData("content"), new Date, Seq(), "celica212")

    collection.save(Json.toJson(frontPost))

    Redirect(routes.Application.blog)
  }

  def showPost(id: Long) = Action {
    BadRequest("No Such Post")
  }
}
