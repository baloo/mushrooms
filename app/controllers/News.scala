package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.mvc.EssentialAction
import play.api.mvc.AsyncResult
import play.api.mvc.QueryStringBindable

import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Done

import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import play.api.libs.json.JsString

import scala.util.control.Exception.catching
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import reactivemongo.api.QueryBuilder
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONInteger

import java.util.Date
import org.joda.time.format.ISODateTimeFormat

import models.Post

object Bindables {
  import java.net.URLEncoder

  type Iso8601Date = Date

  implicit def bindableIso8601 = new QueryStringBindable[Iso8601Date] {
    val fmt = ISODateTimeFormat.dateTime();

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Iso8601Date]] = {
      val parseDate = (ds: String) =>
        catching(classOf[IllegalArgumentException], classOf[UnsupportedOperationException])
          .either(fmt.parseDateTime(ds))
          .right.map(_.toDate)
          .left.map(_ => "Date doesn't look like iso8601")

      params.get(key)
        .flatMap(_.headOption)
        .map(parseDate)
    }

    override def unbind(key: String, value: Iso8601Date) = key + "=" + (URLEncoder.encode(fmt.print(value.getTime), "utf-8"))
  }
}

object News extends Controller{
  import Bindables._


  def index(by: Int = 3) = {
    searchReverse(new Date, by, true)
  }

  def searchForward(since: Iso8601Date, by: Int = 3, include: Boolean = true) =
      Action {
        val query = QueryBuilder(Some(BSONDocument(
          "creationDate" -> BSONDocument(
            if(include) "$gte" -> BSONDateTime(since.getTime)
            else "$gt" -> BSONDateTime(since.getTime)
          )
        )),
        Some(BSONDocument("creationDate" -> BSONInteger(1)))
        )

        val cursor = Post.collection.find[Option[Post]](query)

        val postList: Future[List[Post]] = cursor.toList(by + 1).map(_.flatten)

        AsyncResult{
          postList.map{postList =>
            val lastPost = postList.lift(by)
            val realList = if (postList.length >= by) postList.take(by) else postList

            if (realList.length == by) {
              // Okay, we got enough
              val pager = JsObject(
                postList.headOption.map{ first =>
                  "previous" -> JsString(routes.News.searchReverse(first.creationDate, by, false).url)
                }.toSeq ++
                lastPost.map{ post =>
                  // Ok, we do have one more in list
                  "next" -> JsString(routes.News.searchForward(post.creationDate, by, true).url)
                }.toSeq
              )

              Ok(
                JsObject(Seq(
                  "pager" -> pager,
                  "elements" -> JsArray(realList.map(_.toJson))
                ))
              )
            } else {
              // Too bad :( we didn't had enough :(
              // Need to redirect user
              val maybeLastPost = realList.lastOption

              maybeLastPost.map{ lastPost =>
                TemporaryRedirect(routes.News.searchReverse(lastPost.creationDate, by, true).url)
              }.getOrElse {
                TemporaryRedirect(
                  routes.News.searchReverse(since, by, true).url)
              }
            }
          }

        }
    }

  def searchReverse(upTo: Iso8601Date, by: Int = 3, include: Boolean = true) =
      Action {
        val query = QueryBuilder(Some(BSONDocument(
          "creationDate" -> BSONDocument(
            if(include) "$lte" -> BSONDateTime(upTo.getTime)
            else "$lt" -> BSONDateTime(upTo.getTime)
          )
        )),
        Some(BSONDocument("creationDate" -> BSONInteger(-1)))
        )

        val cursor = Post.collection.find[Option[Post]](query)

        val postList: Future[List[Post]] = cursor.toList(by + 1).map(_.flatten)

        AsyncResult{
          postList.map{postList =>
            val firstPost = postList.lift(by)
            val realList = if (postList.length >= by) postList.take(by) else postList

            val pager = JsObject(
              firstPost.map{ first =>
                "previous" -> JsString(routes.News.searchReverse(first.creationDate, by, true).url)
              }.toSeq ++
              postList.headOption.map{ post =>
                // Ok, we do have one more in list
                "next" -> JsString(routes.News.searchForward(post.creationDate, by, false).url)
              }.toSeq
            )

            Ok(
              JsObject(Seq(
                "pager" -> pager,
                "elements" -> JsArray(realList.map(_.toJson))
              ))
            )
          }

        }
    }

}

