package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.mvc.EssentialAction
import play.api.mvc.AsyncResult

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

object News extends Controller{
  val fmt = ISODateTimeFormat.dateTime();

  def index(by: Int = 3) = {
    searchReverse(fmt.print((new Date()).getTime), by, true)
  }

  def searchForward(since: String, by: Int = 3, include: Boolean = true) =
    stringToDate(since) { sinceDate => 
      Action {
        val query = QueryBuilder(Some(BSONDocument(
          "creationDate" -> BSONDocument(
            if(include) "$gte" -> BSONDateTime(sinceDate.getTime)
            else "$gt" -> BSONDateTime(sinceDate.getTime)
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
                  "previous" -> JsString(
                    routes.News.searchReverse(
                      fmt.print(first.creationDate.getTime), by, false).url)
                }.toSeq ++
                lastPost.map{ post =>
                  // Ok, we do have one more in list
                  "next" -> JsString(
                    routes.News.searchForward(
                      fmt.print(post.creationDate.getTime), by, true).url)
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
                TemporaryRedirect(
                  routes.News.searchReverse(
                    fmt.print(lastPost.creationDate.getTime), by, true).url)
              }.getOrElse {
                TemporaryRedirect(
                  routes.News.searchReverse(
                    fmt.print(sinceDate.getTime), by, true).url)
              }
            }
          }

        }
      }
    }

  def searchReverse(upTo: String, by: Int = 3, include: Boolean = true) =
    stringToDate(upTo) { upToDate =>
      Action {
        val query = QueryBuilder(Some(BSONDocument(
          "creationDate" -> BSONDocument(
            if(include) "$lte" -> BSONDateTime(upToDate.getTime)
            else "$lt" -> BSONDateTime(upToDate.getTime)
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
                "previous" -> JsString(
                  routes.News.searchReverse(
                    fmt.print(first.creationDate.getTime), by, true).url)
              }.toSeq ++
              postList.headOption.map{ post =>
                // Ok, we do have one more in list
                "next" -> JsString(
                  routes.News.searchForward(
                    fmt.print(post.creationDate.getTime), by, false).url)
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




  def stringToDate(dateString: String)(f: Date => EssentialAction) = EssentialAction{ request => {
      val fmt = ISODateTimeFormat.dateTime();
      val maybeDate = catching(classOf[IllegalArgumentException], classOf[UnsupportedOperationException]).opt(fmt.parseDateTime(dateString))

      maybeDate.map{ datetime =>
        f(datetime.toDate)(request)
      }.getOrElse{
        Done(BadRequest("Date doesn't look like iso8601"))
      }
    }
  }
}

