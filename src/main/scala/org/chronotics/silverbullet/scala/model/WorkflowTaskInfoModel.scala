package org.chronotics.silverbullet.scala.model

case class TaskParam(var iId: Int, var strName: String, var strDesc: String, var intParent: Int,
                     var strDataType: String, var dMinValue: String, var dMaxValue: String, var dDefaultValue: String,
                     var strShow: String, var strDefinedVals: String)
case class TaskOrder(var iArrTaskId: Seq[Int])
case class TaskPrevCondition(var arrCondition: Seq[String])
case class TaskInfo(var iId: Int, var iOrder: Int, var iArrPrevTask: Option[TaskOrder], var iArrNextTask: Option[TaskOrder], var iWorkflowId: Int, var iAlibId: Int, var strType: String,
                    var arrParams: Seq[TaskParam], var arrConnection: Seq[TaskConnection], var arrLoop: Seq[TaskLoop], var arrPrevCondition: Option[TaskPrevCondition])
case class WorkFlowTaskInfo(var iId: Int, var strName: String, var arrTask: Seq[TaskInfo])
case class TaskConnection(var fromTaskId: Int, var fromOutputId: Int, var fromOutputName: String, var toInputId: Int, var toInputName: String,
                          var fromSubTaskId: Int, var isFromSubTask: Boolean, var toSubTaskId: Int, var isToSubTask: Boolean)
case class TaskLoop(var fromTaskId: Int, var toTaskId: Int, var numOfLoop: Int, var endLoopCondition: String, var counter: String)