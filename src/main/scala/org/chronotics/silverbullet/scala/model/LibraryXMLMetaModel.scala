package org.chronotics.silverbullet.scala.model

import com.lucidchart.open.xtract.{ XmlReader, __ }
import com.lucidchart.open.xtract.XmlReader._
import play.api.libs.functional.syntax._

case class LibParam(iId: Int, paramType: String, strName: String, strDesc: Option[String], iParent: Option[Int], strDataType: String, dMinValue: Option[String], dMaxValue: Option[String],
                    dDefaultValue: Option[String], strShow: Option[String], strDefinedVals: Option[String], intIsResource: Option[Int])

object LibParam {

  implicit val reader: XmlReader[LibParam] = (
    attribute[Int]("id") and
      attribute[String]("type") and
      (__ \ "name").read[String] and
      (__ \ "desc").read[String].optional and
      (__ \ "parent").read[Int].optional and
      (__ \ "datatype").read[String] and
      (__ \ "min").read[String].optional and
      (__ \ "max").read[String].optional and
      (__ \ "default").read[String].optional and
      (__ \ "show").read[String].optional and
      (__ \ "vals").read[String].optional and
      (__ \ "isres").read[Int].optional
    )(apply _)
}

case class LibParams(arrLibParam: Seq[LibParam])

object LibParams {
  implicit val reader: XmlReader[LibParams] = (__ \ "param").read(seq[LibParam]).map(apply _)
}

case class LibMeta(iId: Int, strType: String, strName: String, isSimulate: Option[Boolean], strLang: Option[String], arrParam: LibParams)

object LibMeta {
  implicit val reader: XmlReader[LibMeta] = (
    attribute[Int]("id") and
      (__ \ "type").read[String] and
      (__ \ "name").read[String] and
      (__ \ "simulate").read[Boolean].optional and
      (__ \ "lang").read[String].optional and
      (__ \ "params").read[LibParams]
    )(apply _)
}
