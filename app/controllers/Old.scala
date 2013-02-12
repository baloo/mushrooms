package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.mvc.AsyncResult

import reactivemongo.api.QueryBuilder
import reactivemongo.api.QueryOpts
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import scala.concurrent.ExecutionContext.Implicits.global
import models.Post

import scala.annotation.tailrec
import scala.concurrent.Future

import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import play.api.libs.json.JsString

import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.data.Form

import java.util.Date

object Old extends Controller{
  @tailrec
  def queryStringToMongoQuery(queryString: Map[String, Seq[String]], query: QueryBuilder): QueryBuilder = {
    queryString.headOption match {
      case Some(e) => queryStringToMongoQuery(queryString.tail, query)
      case _ => query
    }
  }

  def index(page:Int = 0, by:Int = 3) = Action {request =>
    val query = queryStringToMongoQuery(request.queryString, QueryBuilder())

    val cursor = if (page > 0) {
      Post.collection.find[Option[Post]](query, QueryOpts(by * page))
    } else {
      Post.collection.find[Option[Post]](query)
    }

    val futurePosts:Future[Seq[Post]] = cursor.toList(by).map(_.flatten)

    AsyncResult {
      futurePosts.map { posts =>
        val pager = Seq() ++ (if (page > 0) {
          Seq("previous" -> JsString(routes.Old.index(page - 1, by).url))
        } else {
          Seq()
        }) ++ Seq("next" -> JsString(routes.Old.index(page + 1, by).url))

        Ok(
          JsObject(Seq(
            "pager" -> JsObject(pager),
            "elements" -> JsArray(posts.map(_.toJson))
          ))
        )
      }
    }
  }



  val postForm = Form(
    tuple(
      "title" -> text,
      "body" -> text
    )
  )

  def create = Action{ implicit request =>

    val maybeContent = postForm.bindFromRequest.value

    maybeContent.map{
      case (title, body) => {
        val post = Post(None, title, body, new Date)

        AsyncResult {
          Post.collection.insert(post).map{ _ =>
            println("inserted")
            Ok("inserted")
          }
        }
      }
    }.getOrElse(BadRequest("Errors in form"))
  }

}

