package models

import reactivemongo.bson.handlers.BSONWriter
import reactivemongo.bson.handlers.BSONReader
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONNull
import reactivemongo.bson.BSONDateTime


import play.api.libs.json.Format
import play.api.libs.json.JsValue
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import play.api.libs.json.JsString
import play.api.libs.json.JsNull
import play.api.libs.json.JsObject

import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin
import org.joda.time.format.ISODateTimeFormat

import java.util.Date

case class Post(
  id: Option[BSONObjectID],
  title: String,
  body: String,
  creationDate: Date
  ) {
  def toJson = Post.JSONFormatter.writes(this)
}

object Post {
  implicit object BSONFormatter extends BSONReader[Option[Post]] with BSONWriter[Post] {
    def fromBSON(doc: BSONDocument) = {
      val d = doc.toTraversable

      for {
        title <- d.getAs[BSONString]("title").map(_.value)
        body  <- d.getAs[BSONString]("body").map(_.value)
        date  <- d.getAs[BSONDateTime]("creationDate").map(x => new Date(x.value))
      } yield Post(d.getAs[BSONObjectID]("_id"), title, body, date)
    }

    def toBSON(n: Post) = {
      val news = n.id.map(_ => n).getOrElse(n.copy(id = Some(BSONObjectID.generate)))
      BSONDocument(
        "_id" -> news.id.getOrElse(BSONNull),
        "title" -> BSONString(news.title),
        "body" -> BSONString(news.body),
        "creationDate" -> BSONDateTime(news.creationDate.getTime)
      )
    }
  }


  implicit object JSONFormatter extends Format[Post]{
    def reads(j: JsValue) = {
      val maybePost = for {
        title <- (j \ "title").asOpt[String]
        body  <- (j \ "body" ).asOpt[String]
      } yield Post(None, title, body, new Date)

      maybePost.map(JsSuccess(_)).getOrElse(JsError("Does not look like a valid Post json object"))
    }

    val fmt = ISODateTimeFormat.dateTime();


    def writes(n: Post) = JsObject(Seq(
      "id" -> n.id.map(x => JsString(x.stringify)).getOrElse(JsNull),
      "title" -> JsString(n.title),
      "body"  -> JsString(n.body),
      "date"  -> JsString(fmt.print(n.creationDate.getTime))
    ))
  }

  val db = ReactiveMongoPlugin.db
  val collection = db.collection("posts")


}

