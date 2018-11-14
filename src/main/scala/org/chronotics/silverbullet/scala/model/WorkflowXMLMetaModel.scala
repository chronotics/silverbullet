package org.chronotics.silverbullet.scala.model

import com.lucidchart.open.xtract.{ XmlReader, __ }
import com.lucidchart.open.xtract.XmlReader._
import play.api.libs.functional.syntax._

case class TaskSeq(iArrTaskId: Seq[Int])

object TaskSeq {
  implicit val reader: XmlReader[TaskSeq] = (__ \ "id").read(seq[Int]).map(apply _)
}

case class Task(iId: Int, iOrder: Int, iArrPrevTask: Option[TaskSeq], iArrNextTask: Option[TaskSeq], iWorkflowId: Option[Int], iAlibId: Int,
                connection: Option[Connection], loop: Option[Loop], prevCondition: Option[PrevCondition])

object Task {
  implicit val reader: XmlReader[Task] = (
    attribute[Int]("id") and
      (__ \ "order").read[Int] and
      (__ \ "prev").read[TaskSeq].optional and
      (__ \ "next").read[TaskSeq].optional and
      (__ \ "workflow").read[Int].optional and
      (__ \ "alib").read[Int] and
      (__ \ "connection").read[Connection].optional and
      (__ \ "loop").read[Loop].optional and
      (__ \ "prevcondition").read[PrevCondition].optional)(apply _)
}

case class Tasks(arrTask: Seq[Task])

object Tasks {
  implicit val reader: XmlReader[Tasks] = (__ \ "task").read(seq[Task]).map(apply _)
}

case class WorkFlowMeta(iId: Int, strName: String, tasks: Tasks)

object WorkFlowMeta {
  implicit val reader: XmlReader[WorkFlowMeta] = (
    attribute[Int]("id") and
      (__ \ "name").read[String] and
      (__ \ "tasks").read[Tasks])(apply _)
}

case class Connection(arrConnectionIn: Seq[ConnectionIn])

object Connection {
  implicit val reader: XmlReader[Connection] = (__ \ "in").read(seq[ConnectionIn]).map(apply _)
}

case class ConnectionIn(id: Int, from: Int, fromName: String, to: Int, toName: String,
                        from_sub_id: Option[Int], is_from_sub_task: Option[Int],
                        to_sub_id: Option[Int], is_to_sub_task: Option[Int])

object ConnectionIn {
  implicit val reader: XmlReader[ConnectionIn] = (
    attribute[Int]("id") and //previous parent task id
      attribute[Int]("from") and //previous output idx of from task: from_sub_id
      attribute[String]("from_name") and //previous output name from task: from_sub_id
      attribute[Int]("to") and //current input idx of task: to_sub_id
      attribute[String]("to_name") and //current input name of task: to_sub_id
      attribute[Int]("from_sub_id").optional and //if previous task contain a workflow, from_sub_id is the last tasks of this workflow; else from_sub_id = id or null
      attribute[Int]("is_from_sub_task").optional and //if previous task contain a workflow, is_from_sub_task = TRUE if from_sub_id is the last tasks of this workflow; else FALSE
      attribute[Int]("to_sub_id").optional and //if current task contain a workflow, to_sub_id is first tasks of this workflow; else to_sub_id = null or to_sub_id = current task id (id attribute in root tag)
      attribute[Int]("is_to_sub_task").optional)(apply _) //if current task contain a workflow, is_to_sub_task = TRUE if to_sub_id is the first tasks of this workflow; else FALSE
}

case class Loop(arrLoopItem: Seq[LoopItem])

object Loop {
  implicit val reader: XmlReader[Loop] = (__ \ "item").read(seq[LoopItem]).map(apply _)
}

case class LoopItem(from: Int, to: Int, numLoop: Int, counter: Option[String], endLoopCondition: Option[String])

object LoopItem {
  implicit val reader: XmlReader[LoopItem] = (
    attribute[Int]("fromTask") and
      attribute[Int]("toTask") and
      attribute[Int]("numLoop") and
      attribute[String]("counter").optional and
      (__ \ "end").read[String].optional
    )(apply _)
}

case class PrevCondition(arrItem: Seq[String])

object PrevCondition {
  implicit val reader: XmlReader[PrevCondition] = (__ \ "item").read(seq[String]).map(apply _)
}
