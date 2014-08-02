package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import java.util.Date
import models._
import views._

object Application extends Controller {
  val olderPost = Post(2, "Second Post", "test", new Date)
  val olderUser = User("celica212", "dongju")
  val olderComment1 = Comment("who", "what is it", new Date, 2)
  val olderComment2 = Comment("who", "what is it", new Date, 2)
  val olderComments = Seq(olderComment1, olderComment2)
  val older = Option(olderPost, olderUser, olderComments)

  val frontPost = Post(1, "First Post", "test", new Date)
  val frontUser = User("celica212", "dongju")
  val frontComment1 = Comment("who", "what is it", new Date, 1)
  val frontComment2 = Comment("who", "what is it", new Date, 1)
  val frontComments = Seq(frontComment1, frontComment2)
  val front: Option[(Post, User, Seq[Comment])] = Option(frontPost, frontUser, frontComments)

  val olders: Seq[Option[(Post, User, Seq[Comment])]] = Seq(older, older, older)
  var allposts: Seq[Option[(Post, User, Seq[Comment])]] = front +: olders


  var postForm = Form(
    mapping(
      "id" -> longNumber,
      "title" -> nonEmptyText,
      "content" -> nonEmptyText,
      "postedAt" -> date
    ) (Post.apply) (Post.unapply)
  )

  def index = Action {
    Logger.info("Index page has been requested")
    Ok(html.index("Your new application is ready."))
  }

  def homepage = Action {
    Ok(html.homepage_index("Homepage"))
  }

  def blog = Action {
    val front: Option[(Post, User, Seq[Comment])] = allposts head
    val olders: Seq[Option[(Post, User, Seq[Comment])]] = allposts tail

    Ok(html.blog_index(front, olders))
  }

  def post = Action {
    Ok(html.postform(postForm))
  }

  def getPost = Action { implicit request =>
    postForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.homepage_index("invalid post")),
      postData => {
        val frontUser = User("celica212", "dongju")
        val frontComment1 = Comment("who", "what is it", new Date, 1)
        val frontComment2 = Comment("who", "what is it", new Date, 1)
        val frontComments = Seq(frontComment1, frontComment2)
        val front: Option[(Post, User, Seq[Comment])] = Option(postData, frontUser, frontComments)
        val olders: Seq[Option[(Post, User, Seq[Comment])]] = allposts
        allposts = front +: allposts
        Ok(html.blog_index(front, olders))
      }
    )
  }

  def show(id: Long) = {
    allposts map(
      (postObject: Option[(Post, User, Seq[Comment])]
    )=> postObject(0)(0).filter((post : Post) => post.id == id)
      ).map(post => html.blog_display(post))
  }
}