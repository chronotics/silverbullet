package org.chronotics.silverbullet.scala.akka.protocol

import akka.actor.ActorRef
import org.chronotics.silverbullet.scala.akka.model._
import org.chronotics.silverbullet.scala.akka.state.{ActorState, Working}
import org.chronotics.silverbullet.scala.akka.util.EnvConfig

import scala.collection.immutable.HashMap

object Message {
  //Ask workflow path from Router
  case class AskWorkFlowPath(userId: Int = 0, workFlowId: Int, strIdentifyId: String = "", intSimulation: Int = 0)
  //Send Workflow Message to Workflow Actor to execute
  case class SendWorkFlow(callback: String, userId: Int, workFlowId: Int, requestId: Int, mapLibParam: HashMap[Int, Seq[LibParam]], bIsStepByStep: Boolean,
                          parentTaskId: Int, parentWorkflowId: Int, parentRequest: UserRequest, arrTaskConnection: Seq[TaskConnection], arrOutConneciton: Seq[TaskConnection],
                          runInternal: Boolean = false)
  //Send Task to Library Actor to Execute
  case class SendTaskOfWorkFlow(callback: String, userId: Int, workFlowId: Int, requestId: Int, taskId: Int, modifiedParams: Seq[LibParam] = null)
  //Receive Workflow Actor Address from Workflow Router
  case class ReceiveWorkFlowPath(path: String, userId: Int, workFlowId: Int, strIdentifyId: String)
  //Cancel Workflow Request
  case class CancelledRequest(intRequestId: Int)
  //Update Workflow Actor Address to Workflow Router
  case class UpdateWorkFlowActorPath(path: String, workFlowId: Int)
  //Message that router to broadcast to all routees
  case class BroadcastWorkFlowActorPath(path: String, workFlowId: Int)
  //Remove workflow actor address from router
  case class RemoveWorkFlowActorPath(workFlowId: Int)
  //Read XML Metadata of Workflow from file
  case class ReadXMLWorkFlowFile(intWorkFlowId: Int)
  //Read XML Metadata of Workflow from DB
  case class ReadXMLWorkFlowDB(intWorkFlowId: Int)
  //Read XML Metadata of Library from DB
  case class ReadXMLAlibDB(intAlibId: Int)
  //Received XML Metadata of workflow
  case class ReceiveXMLWorkFlowData(objResult: WorkFlowTaskInfo)
  //Received XML Metadata of Library
  case class ReceiveXMLAlibData(objResult: LibMeta)
  //Ask library router about the library actor address
  case class AskAlibPath(libType: String)
  //Check library actor status from router
  case class CheckAlib(libType: String)
  //Received library actor address from router
  case class ReceiveAlibPath(path: String, libType: String)
  //Update library actor address to router
  case class UpdateAlibActorPath(path: String, libType: String)
  //Remove library actor address from router
  case class RemoveAlibActorPath(path: String, libType: String)
  //Kill a library actor
  case class KilledAlibActor(path: String, libType: String)
  //Check if library actor still has running instance
  case object CheckAlibActorRunning
  //Update metadata of library actor
  case class UpdateAlibMetaData(libId: Int)
  //Send task to library actor to execute
  case class SendTaskToAlib(iTaskId: Int, strType: String, arrParam: Seq[LibParam], objRequest: UserRequest, mapPrevResultKey: HashMap[String, HashMap[String, String]],
                            objTaskInfo: TaskInfo, bIsLastTask: Boolean = false, endLoopCondition: LoopTask = null, prevCondition: TaskPrevCondition = null, isMergeMode: Boolean = false)
  //Send task to lang engine actor to execute - Deprecated
  case class SendTaskToExecute(iTaskId: Int, strType: String, arrParam: Seq[LibParam], objRequest: UserRequest, objProcSender: ActorRef,
                               arrPrevResultKey: HashMap[String, HashMap[String, String]], libMeta: LibMeta, objTaskInfo: TaskInfo,
                               bIsLastTask: Boolean = false, endLoopCondition: LoopTask = null, prevCondition: TaskPrevCondition = null)
  //Send task to lang engine process to execute
  case class SendTaskToExecuteNoActorRef(iTaskId: Int, strType: String, arrParam: Seq[LibParam], objRequest: UserRequest,
                                         arrPrevResultKey: HashMap[String, HashMap[String, String]], libMeta: LibMeta, objTaskInfo: TaskInfo,
                                         bIsLastTask: Boolean = false, endLoopCondition: LoopTask = null, prevCondition: TaskPrevCondition = null, isMergeMode: Boolean = false)
  //Task is executed completely
  case class AlibTaskCompleted(iTaskId: Int, strType: String, objRequest: UserRequest, objProcSender: ActorRef, objStatus: ActorState = Working, isEndLoop: Boolean = false, isPrevConditionSatisfied: Boolean = true)
  //Send library status to manage actore
  case class UpdateActorStatusToManager(actorPath: String, status: ActorState, msg: String, timestamp: Long = EnvConfig.getCurrentTimestamp())
  case class UpdateSimpleStatusToManager(msg: String, timestamp: Long = EnvConfig.getCurrentTimestamp())
  case class UpdateSimpleArrayStatusToManager(msg: List[(String, Long)])
  case class Alive(strMsg: String)
  case class StopActor(id: String, strType: String)
  case class Failover(strMsg: String)
  case class FailoverAck(strMsg: String, strAck: String)
  case object Ping
  case object Pong
  case class AskNewPort(ip: String, processAddress: String)
  case class RemovePort(ip: String, port: Int)
  case class CheckPortStatus(ip: String, port: Int)
  case class RouterTerminatedPath(path: String)
  case object ListWorkflowPath
  case object ListAlibPath
}
