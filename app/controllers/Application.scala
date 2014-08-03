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
  val olderComment1 = Comment("who", "what is it", new Date, 2)
  val olderComment2 = Comment("who", "what is it", new Date, 2)
  val olderComments = Seq(olderComment1, olderComment2)
  val olderPost = Post(2, "Second Post", "test", new Date, olderComments, "celica212")
  val olderUser = User("celica212", "dongju")

  val older = (olderPost, olderUser)

  val frontComment1 = Comment("who", "what is it", new Date, 1)
  val frontComment2 = Comment("who", "what is it", new Date, 1)
  val frontComments = Seq(frontComment1, frontComment2)
  val frontPost = Post(1, "First Post", "test", new Date, frontComments, "celica212")
  val frontUser = User("celica212", "dongju")

  val front: (Post, User) = (frontPost, frontUser)

  val olders: Seq[(Post, User)] = Seq(older, older, older)
  var allposts: Seq[(Post, User)] = front +: olders
  var count: Long = 3
  var posts = getAllPosts

  private def postCollection: JSONCollection = db.collection[JSONCollection]("posts")
  private def commentCollection: JSONCollection = db.collection[JSONCollection]("comments")


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

  def getAllPosts = {
    import play.api.libs.concurrent.Execution.Implicits._
    implicit val Format = Json.format[Comment]
    implicit val postFormat = Json.format[Post]

    val cursor = postCollection.find(Json.obj()).cursor[Post]
    val result = cursor.collect[Seq]()

    val alls = result.map {
      postOne => (User(userID, userName), postOne.head)
    }
    alls
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
    implicit val Format = Json.format[Comment]
    implicit val postFormat = Json.format[Post]

    val postData: Map[String, String] = postForm.bindFromRequest.data
    val frontPost = Post(counter, postData("title"), postData("content"), new Date, Seq(), "celica212")

    postCollection.save(Json.toJson(frontPost))

    val front = (frontPost, frontUser)
    allposts = front +: allposts
    Redirect(routes.Application.blog)
  }

  def showPost(id: Long) = Action {
    val post = allposts.find(post => post._1.id == id)
    if(post.isDefined) {
      Ok(html.show_post(post.get))
    } else {
      BadRequest("No Such Post")
    }
  }
}
