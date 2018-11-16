package org.chronotics.silverbullet.scala.model

import com.lucidchart.open.xtract.{ XmlReader, __ }
import com.lucidchart.open.xtract.XmlReader._
import play.api.libs.functional.syntax._

case class UserRequestLibraryParam(iId: Int, strValue: Option[String])

object UserRequestLibraryParam {
  implicit val reader: XmlReader[UserRequestLibraryParam] = (
    attribute[Int]("id") and
      (__ \ "value").read[String].optional)(apply _)
}

case class UserRequestLibParams(arrLibParam: Seq[UserRequestLibraryParam])

object UserRequestLibParams {
  implicit val reader: XmlReader[UserRequestLibParams] = (__ \ "param").read(seq[UserRequestLibraryParam]).map(apply _)
}

case class TaskMeta(iId: Int, libId: Int, arrParam: UserRequestLibParams)

object TaskMeta {
  implicit val reader: XmlReader[TaskMeta] = (
    attribute[Int]("id") and
      (__ \ "alib").read[Int] and
      (__ \ "params").read[UserRequestLibParams])(apply _)
}

case class TaskMetaList(arrTaskMeta: Seq[TaskMeta])

object TaskMetaList {
  implicit val reader: XmlReader[TaskMetaList] = (__ \ "task").read(seq[TaskMeta]).map(apply _)
}

case class UserRequestXML(iId: Int, arrTaskMetaList: TaskMetaList)

object UserRequestXML {
  implicit val reader: XmlReader[UserRequestXML] = (
    attribute[Int]("id") and
      (__ \ "tasks").read[TaskMetaList])(apply _)
}
