package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import java.util.Date
import models._
import views._
import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection


object Application extends Controller {
  val olderPost = Post(2, "Second Post", "test", new Date)
  val olderUser = User("celica212", "dongju")
  val olderComment1 = Comment("who", "what is it", new Date, 2)
  val olderComment2 = Comment("who", "what is it", new Date, 2)
  val olderComments = Seq(olderComment1, olderComment2)
  val older = (olderPost, olderUser, olderComments)

  val frontPost = Post(1, "First Post", "test", new Date)
  val frontUser = User("celica212", "dongju")
  val frontComment1 = Comment("who", "what is it", new Date, 1)
  val frontComment2 = Comment("who", "what is it", new Date, 1)
  val frontComments = Seq(frontComment1, frontComment2)
  val front: (Post, User, Seq[Comment]) = (frontPost, frontUser, frontComments)

  val olders: Seq[(Post, User, Seq[Comment])] = Seq(older, older, older)
  var allposts: Seq[(Post, User, Seq[Comment])] = front +: olders
  var count: Long = 3

  def connect() {
    import reactivemongo.api._
    import scala.concurrent.ExecutionContext.Implicits.global

    // gets an instance of the driver
    // (creates an actor system)
    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))

    // Gets a reference to the database "plugin"
    val db = connection("plugin")

    // Gets a reference to the collection "acoll"
    // By default, you get a BSONCollection.
    val collection = db("acoll")
  }

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
    val front: Option[(Post, User, Seq[Comment])] = allposts headOption
    val olders: Seq[(Post, User, Seq[Comment])] = allposts tail

    Ok(html.blog_index(front, olders))
  }

  def writePost = Action {
    Ok(html.blog_form(postForm))
  }

  def post = Action { implicit request =>
    val postData: Map[String, String] = postForm.bindFromRequest.data
    val frontPost = Post(counter, postData("title"), postData("content"), new Date)
    val front = (frontPost, frontUser, frontComments)
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
