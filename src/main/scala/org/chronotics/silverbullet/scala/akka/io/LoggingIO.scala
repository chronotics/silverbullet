package org.chronotics.silverbullet.scala.akka.io

import java.text.SimpleDateFormat
import java.util.Calendar

import org.chronotics.pandora.java.log.LoggerFactory
import org.chronotics.silverbullet.scala.akka.protocol.Message.{SendTaskToAlib, SendTaskToExecute, SendTaskToExecuteNoActorRef}
import org.chronotics.silverbullet.scala.akka.state.ActorState

object LoggingIO {
  var _instance: LoggingIO = null

  var lockObject = new Object()

  def getInstance() = {
    lockObject.synchronized {
      if (_instance == null) {
        _instance = new LoggingIO()
      }
    }

    _instance
  }
}

class LoggingIO {
  val logger = LoggerFactory.getLogger(getClass)

  //[Callback|UserID][Timestamp(yyyy-MM-dd HH:mm:ss)][NodeType|Request ID|Status|ParentID|ID|Task ID][Message]
  def generateLogMessage(strCallback: String, strUserId: String, strNodeType: String, intRequestID: Int, objStatus: ActorState, strParentObjectID: String, strObjectID: String, strTaskID: String, strMessage: String): String = {
    var strLogMesage: StringBuilder = StringBuilder.newBuilder
    val strTimeFormat = "yyyy-MM-dd HH:mm:ss"
    val objSimpleDateFormat: SimpleDateFormat = new SimpleDateFormat(strTimeFormat)
    var strCurTime = objSimpleDateFormat.format(Calendar.getInstance.getTime)

    strLogMesage = strLogMesage.append("[").append(strCallback).append("|").append(strUserId).append("][").append(strCurTime).append("][").append(strNodeType).append("|")
      .append(intRequestID.toString).append("|").append(objStatus.toString).append("|")
      .append(strParentObjectID).append("|").append(strObjectID).append("|")
      .append(strTaskID).append("]$").append(strMessage)

    strLogMesage.toString
  }

  def generateLogMesageTaskExecute(task: SendTaskToExecute, objStatus: ActorState, strMessage: String): String = {
    generateLogMessage(task.objRequest.strCallback, task.objRequest.iUserId.toString, "L", task.objRequest.iRequestId, objStatus, task.objRequest.iWorkFlowId.toString, task.objTaskInfo.iAlibId.toString, task.iTaskId.toString, strMessage)
  }

  def generateLogMesageTaskExecuteNoActor(task: SendTaskToExecuteNoActorRef, objStatus: ActorState, strMessage: String, strNodeType: String = "L"): String = {
    generateLogMessage(task.objRequest.strCallback, task.objRequest.iUserId.toString, strNodeType, task.objRequest.iRequestId, objStatus, task.objRequest.iWorkFlowId.toString, task.objTaskInfo.iAlibId.toString, task.iTaskId.toString, strMessage)
  }

  def generateScriptMesageTaskExecuteNoActor(task: SendTaskToExecuteNoActorRef, objStatus: ActorState, strMessage: String): String = {
    generateLogMessage(task.objRequest.strCallback, task.objRequest.iUserId.toString, "C", task.objRequest.iRequestId, objStatus, task.objRequest.iWorkFlowId.toString, task.objTaskInfo.iAlibId.toString, task.iTaskId.toString, strMessage)
  }

  def generateLogMesageTaskAlib(task: SendTaskToAlib, objStatus: ActorState, strMessage: String): String = {
    generateLogMessage(task.objRequest.strCallback, task.objRequest.iUserId.toString, "L", task.objRequest.iRequestId, objStatus, task.objRequest.iWorkFlowId.toString, task.objTaskInfo.iAlibId.toString, task.iTaskId.toString, strMessage)
  }
}
