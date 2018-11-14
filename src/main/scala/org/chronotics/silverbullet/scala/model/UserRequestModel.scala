package org.chronotics.silverbullet.scala.model

import scala.collection.immutable.HashMap

case class UserRequestEntity(callback: String, id: Int, workflow_id: Int, param_xml: String, run_date: String, run_user: String, status: String, task_id: Int, is_step_by_step: Int, is_run_internal: Int)
case class DebugRequestEntity(debugId: Int, debugType: String)

case class UserRequest(strCallback: String, iUserId: Int, iWorkFlowId: Int, iRequestId: Int, iParentTaskId: Int, iParentWorkflow: Int,
                       var mapLibParam: HashMap[Int, Seq[LibParam]],
                       seqConnection: Seq[TaskParam], seqNextConnection: Seq[TaskParam],
                       strActorRefPath: String, intTaskId: Int = 0, bIsStepByStep: Boolean = false, intSimulation: Int = 0, intWorkflowEntity: Int = 0, runInternal: Boolean = false) {
  override def equals(obj: Any): Boolean = obj match {
    case obj: UserRequest => obj.canEqual(this) && obj.hashCode == this.hashCode
    case _ => false
  }

  override def hashCode(): Int = {
    val prime = 31
    var result = 1

    result = prime * result + iUserId
    result = prime * result + iWorkFlowId
    result = prime * result + iRequestId
    result = prime * result + iParentTaskId
    result = prime * result + intTaskId
    result = prime * result + bIsStepByStep.hashCode

    result
  }
}

case class UserRequestStatus(objUserRequest: UserRequest, var mapTaskStatus: HashMap[Int, Seq[Int]])
case class LoopTask(intTaskId: Int, endLoopCondition: String, numLoop: Int, counter: String)
