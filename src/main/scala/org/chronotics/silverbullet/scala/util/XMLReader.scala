package org.chronotics.silverbullet.scala.util

import com.lucidchart.open.xtract.XmlReader
import com.typesafe.config.{Config, ConfigFactory}
import org.chronotics.pandora.java.log.LoggerFactory
import org.chronotics.silverbullet.scala.akka.util.EnvConfig
import org.chronotics.silverbullet.scala.model._

import scala.xml.{Elem, XML}

class XMLReaderWorkflowFromDB(config: Config) {
  val log = LoggerFactory.getLogger(getClass());
  val strPrefix = EnvConfig.getPrefix()
  val dbConnection = DatabaseConnection.getInstance(config.getString("database.type"))

  def parseXMLData(intWorkFlowId: Int, strWorkFlowXML: String): WorkFlowMeta = {
    var xml = strWorkFlowXML

    if (xml == "") {
      xml = readWorkFlowJDBC(intWorkFlowId)
    }

    log.debug(xml)

    if (xml != "") {
      val xmlMeta: Elem = XML.loadString(xml)
      XmlReader.of[WorkFlowMeta].read(xmlMeta).getOrElse(null)
    }
    else {
      null
    }
  }

  /**
    * Get xml of an analytics flow from DB using PreparedStatement
    *
    */
  def readWorkFlowJDBC(workflowId: Int): String = {
    var strXML: String = dbConnection.readWorkFlowJDBC(workflowId)
    strXML
  }
}

class XMLReaderAlibFromDB(config: Config) {
  val log = LoggerFactory.getLogger(getClass());
  val strPrefix = EnvConfig.getPrefix()
  val dbConnection = DatabaseConnection.getInstance(config.getString("database.type"))

  def parseXMLData(intAlibId: Int, strParamXML: String): LibMeta = {
    var xml = strParamXML
    var lang = ""

    if (xml == "") {
      val result = readAlibJDBC(intAlibId)
      lang = result._1
      xml = result._2
    }

    log.debug(xml)

    if (xml != "") {
      val xmlMeta: Elem = XML.loadString(xml)

      var curMeta = XmlReader.of[LibMeta].read(xmlMeta).getOrElse(null)

      if (curMeta != null) {
        var newMeta = LibMeta.apply(intAlibId, curMeta.strType, curMeta.strName, curMeta.isSimulate, Some(lang), curMeta.arrParam)

        newMeta
      } else {
        null
      }
    }
    else {
      null
    }
  }

  def readAlibJDBC(intAlibId: Int): (String, String) = {
    var result = dbConnection.readAlibJDBC(intAlibId)
    (result._1, result._2)
  }
}

class XMLReaderWorkflowTaskDB(intWorkFlowId: Int) {
  val log = LoggerFactory.getLogger(getClass());
  val config: Config = ConfigFactory.parseFile(EnvConfig.getConfigFile("z2_conn")).resolve()

  def readXMLInfo(): WorkFlowTaskInfo = {
    val objWorkFlowData = new XMLReaderWorkflowFromDB(config).parseXMLData(intWorkFlowId, "")
    var objWorkFlowTaskInfo: WorkFlowTaskInfo = WorkFlowTaskInfo.apply(0, "", Seq.empty)

    if (objWorkFlowData != null) {
      println("xml-data: " + objWorkFlowData)
      objWorkFlowTaskInfo.iId = objWorkFlowData.iId
      objWorkFlowTaskInfo.strName = objWorkFlowData.strName

      objWorkFlowData.tasks.arrTask.foreach { objTask =>
        var objTaskInfo: TaskInfo = TaskInfo.apply(0, 0, None, None, 0, 0, "", Seq.empty, Seq.empty, Seq.empty, None)

        objTaskInfo.iId = objTask.iId
        objTaskInfo.iAlibId = objTask.iAlibId
        objTaskInfo.iOrder = objTask.iOrder
        objTaskInfo.iWorkflowId = objTask.iWorkflowId match {
          case Some(x) => x
          case None => 0
        }

        val objLibMeta = new XMLReaderAlibFromDB(config).parseXMLData(objTask.iAlibId, "")

        if (objLibMeta != null) {
          objTaskInfo.strType = objLibMeta.strType

          //var iId: Int, var strName: String, var strDesc: String, var intParent: Int,
          //var strDataType: String, var dMinValue: String, var dMaxValue: String, var dDefaultValue: String,
          //var strShow: String, var strDefinedVals: String

          objLibMeta.arrParam.arrLibParam.foreach { param =>
            var objParamInfo: TaskParam = TaskParam.apply(0, "", "", 0, "", "", "", "", "", "")

            objParamInfo.iId = param.iId
            objParamInfo.strName = param.strName
            objParamInfo.strDesc = param.strDesc match {
              case Some(x) => x
              case None => ""
            }
            objParamInfo.intParent = param.iParent match {
              case Some(x) => x
              case None => 0
            }
            objParamInfo.strDataType = param.strDataType
            objParamInfo.dMinValue = param.dMinValue match {
              case Some(x) => x
              case None => ""
            }
            objParamInfo.dMaxValue = param.dMaxValue match {
              case Some(x) => x
              case None => ""
            }
            objParamInfo.dDefaultValue = param.dDefaultValue match {
              case Some(x) => x
              case None => ""
            }
            objParamInfo.strShow = param.strShow match {
              case Some(x) => x
              case None => ""
            }
            objParamInfo.strDefinedVals = param.strDefinedVals match {
              case Some(x) => x
              case None => ""
            }

            objTaskInfo.arrParams = objTaskInfo.arrParams :+ objParamInfo
          }
        }

        var arrPrevTaskOrder: Seq[Int] = Seq.empty

        objTask.iArrPrevTask match {
          case Some(value: TaskSeq) => value.iArrTaskId.foreach { item =>
            arrPrevTaskOrder = arrPrevTaskOrder :+ item
          }
          case None => objTaskInfo.iArrPrevTask = None
        }

        val objPrevTaskOrder = TaskOrder(arrPrevTaskOrder)
        objTaskInfo.iArrPrevTask = Some(objPrevTaskOrder)

        var arrNextTaskOrder: Seq[Int] = Seq.empty

        objTask.iArrNextTask match {
          case Some(value: TaskSeq) => value.iArrTaskId.foreach { item =>
            arrNextTaskOrder = arrNextTaskOrder :+ item
          }
          case None => objTaskInfo.iArrNextTask = None
        }

        val objNextTaskOrder = TaskOrder(arrNextTaskOrder)
        objTaskInfo.iArrNextTask = Some(objNextTaskOrder)

        var arrPrevCondition: Seq[String] = Seq.empty
        objTask.prevCondition match {
          case Some(value: PrevCondition) => value.arrItem.foreach { item =>
            arrPrevCondition = arrPrevCondition :+ item
          }
          case None => objTaskInfo.arrPrevCondition = None
        }

        val objPrevCondition = TaskPrevCondition(arrPrevCondition)
        objTaskInfo.arrPrevCondition = Some(objPrevCondition)

        var arrTaskConnection: Seq[TaskConnection] = Seq.empty

        objTask.connection match {
          case Some(arrConnection) => {
            println("xml-connection: " + arrConnection)

            for (curConnect <- arrConnection.arrConnectionIn) {
              var isFromSubTask: Boolean = curConnect.is_from_sub_task match {
                case Some(x) => x match {
                  case 1 => true
                  case _ => false
                }
                case None => false
              }

              var frombSubTaskId = curConnect.from_sub_id match {
                case Some(x) => x
                case None => 0
              }

              var isToSubTask: Boolean = curConnect.is_to_sub_task match {
                case Some(x) => x match {
                  case 1 => true
                  case _ => false
                }
                case None => false
              }

              var toSubTaskId = curConnect.to_sub_id match {
                case Some(x) => x
                case None => 0
              }

              val tmpConnect: TaskConnection = TaskConnection.apply(curConnect.id, curConnect.from, curConnect.fromName, curConnect.to, curConnect.toName,
                frombSubTaskId, isFromSubTask, toSubTaskId, isToSubTask)
              arrTaskConnection = arrTaskConnection :+ tmpConnect
            }
          }
          case None =>
        }

        var arrTaskLoop: Seq[TaskLoop] = Seq.empty
        objTask.loop match {
          case Some(arrLoop) => {
            for (curLoop <- arrLoop.arrLoopItem) {
              val curEndloop: String = curLoop.endLoopCondition match {
                case Some(str) => str
                case None => ""
              }

              val curCounter: String = curLoop.counter match {
                case Some(str) => str
                case None => ""
              }

              var tmpLoop: TaskLoop = TaskLoop.apply(curLoop.from, curLoop.to, curLoop.numLoop, curEndloop, curCounter)
              arrTaskLoop = arrTaskLoop :+ tmpLoop
            }
          }
          case None =>
        }

        objTaskInfo.arrConnection = arrTaskConnection
        objTaskInfo.arrLoop = arrTaskLoop

        objWorkFlowTaskInfo.arrTask = objWorkFlowTaskInfo.arrTask :+ objTaskInfo
      }
    }

    objWorkFlowTaskInfo
  }
}

class XMLReaderWorkflow(strFileName: String, bResource: Boolean) {
  val log = LoggerFactory.getLogger(getClass());
  val config: Config = ConfigFactory.parseFile(EnvConfig.getConfigFile("z2_conn")).resolve()

  def parseXMLFile(): WorkFlowMeta = {
    var xmlFile: Elem = null

    if (bResource) {
      xmlFile = XML.load(getClass.getResourceAsStream(strFileName))
    }
    else {
      xmlFile = XML.loadFile(strFileName)
    }

    XmlReader.of[WorkFlowMeta].read(xmlFile).getOrElse(null)
  }

  def readXMLInfo(strDir: String): WorkFlowTaskInfo = {
    val objWorkFlowData = parseXMLFile();
    var objWorkFlowTaskInfo: WorkFlowTaskInfo = WorkFlowTaskInfo.apply(0, "", Seq.empty)

    if (objWorkFlowData != null) {
      objWorkFlowTaskInfo.iId = objWorkFlowData.iId
      objWorkFlowTaskInfo.strName = objWorkFlowData.strName

      objWorkFlowData.tasks.arrTask.foreach { objTask =>
        var objTaskInfo: TaskInfo = TaskInfo.apply(0, 0, None, None, 0, 0, "", Seq.empty, Seq.empty, Seq.empty, None)

        objTaskInfo.iId = objTask.iId
        objTaskInfo.iAlibId = objTask.iAlibId
        objTaskInfo.iOrder = objTask.iOrder
        objTaskInfo.iWorkflowId = objTask.iWorkflowId match {
          case Some(x) => x
          case None => 0
        }

        val objLibMeta = if (!bResource) new XMLReaderLibMeta(strDir + "/" + config.getString("configdir.alib") + objTask.iAlibId + ".xml", bResource).parseXMLFile() else new XMLReaderLibMeta("z2_alib_" + objTask.iAlibId, bResource).parseXMLFile()

        if (objLibMeta != null) {
          objTaskInfo.strType = objLibMeta.strType

          objLibMeta.arrParam.arrLibParam.foreach { param =>
            var objParamInfo: TaskParam = TaskParam.apply(0, "", "", 0, "", "", "", "", "", "")

            objParamInfo.iId = param.iId
            objParamInfo.strName = param.strName
            objParamInfo.strDesc = param.strDesc match {
              case Some(x) => x
              case None => ""
            }
            objParamInfo.intParent = param.iParent match {
              case Some(x) => x
              case None => 0
            }
            objParamInfo.strDataType = param.strDataType
            objParamInfo.dMinValue = param.dMinValue match {
              case Some(x) => x
              case None => ""
            }
            objParamInfo.dMaxValue = param.dMaxValue match {
              case Some(x) => x
              case None => ""
            }
            objParamInfo.dDefaultValue = param.dDefaultValue match {
              case Some(x) => x
              case None => ""
            }
            objParamInfo.strShow = param.strShow match {
              case Some(x) => x
              case None => ""
            }
            objParamInfo.strDefinedVals = param.strDefinedVals match {
              case Some(x) => x
              case None => ""
            }

            objTaskInfo.arrParams = objTaskInfo.arrParams :+ objParamInfo
          }
        }

        var arrPrevTaskOrder: Seq[Int] = Seq.empty

        objTask.iArrPrevTask match {
          case Some(value: TaskSeq) => value.iArrTaskId.foreach { item =>
            arrPrevTaskOrder = arrPrevTaskOrder :+ item
          }
          case None => objTaskInfo.iArrPrevTask = None
        }

        val objPrevTaskOrder = TaskOrder(arrPrevTaskOrder)
        objTaskInfo.iArrPrevTask = Some(objPrevTaskOrder)

        var arrNextTaskOrder: Seq[Int] = Seq.empty

        objTask.iArrNextTask match {
          case Some(value: TaskSeq) => value.iArrTaskId.foreach { item =>
            arrNextTaskOrder = arrNextTaskOrder :+ item
          }
          case None => objTaskInfo.iArrNextTask = None
        }

        val objNextTaskOrder = TaskOrder(arrNextTaskOrder)
        objTaskInfo.iArrNextTask = Some(objNextTaskOrder)

        var arrPrevCondition: Seq[String] = Seq.empty
        objTask.prevCondition match {
          case Some(value: PrevCondition) => value.arrItem.foreach { item =>
            arrPrevCondition = arrPrevCondition :+ item
          }
          case None => objTaskInfo.arrPrevCondition = None
        }

        val objPrevCondition = TaskPrevCondition(arrPrevCondition)
        objTaskInfo.arrPrevCondition = Some(objPrevCondition)

        var arrTaskConnection: Seq[TaskConnection] = Seq.empty

        objTask.connection match {
          case Some(arrConnection) => {
            for (curConnect <- arrConnection.arrConnectionIn) {
              var isFromSubTask: Boolean = curConnect.is_from_sub_task match {
                case Some(x) => x match {
                  case 1 => true
                  case _ => false
                }
                case None => false
              }

              var frombSubTaskId = curConnect.from_sub_id match {
                case Some(x) => x
                case None => 0
              }

              var isToSubTask: Boolean = curConnect.is_to_sub_task match {
                case Some(x) => x match {
                  case 1 => true
                  case _ => false
                }
                case None => false
              }

              var toSubTaskId = curConnect.to_sub_id match {
                case Some(x) => x
                case None => 0
              }

              val tmpConnect: TaskConnection = TaskConnection.apply(curConnect.id, curConnect.from, curConnect.fromName, curConnect.to, curConnect.toName,
                frombSubTaskId, isFromSubTask, toSubTaskId, isToSubTask)
              arrTaskConnection = arrTaskConnection :+ tmpConnect
            }
          }
          case None =>
        }

        var arrTaskLoop: Seq[TaskLoop] = Seq.empty
        objTask.loop match {
          case Some(arrLoop) => {
            for (curLoop <- arrLoop.arrLoopItem) {
              val curEndloop: String = curLoop.endLoopCondition match {
                case Some(str) => str
                case None => ""
              }

              val curCounter: String = curLoop.counter match {
                case Some(str) => str
                case None => ""
              }

              var tmpLoop: TaskLoop = TaskLoop.apply(curLoop.from, curLoop.to, curLoop.numLoop, curEndloop, curCounter)
              arrTaskLoop = arrTaskLoop :+ tmpLoop
            }
          }
          case None =>
        }

        objTaskInfo.arrConnection = arrTaskConnection
        objTaskInfo.arrLoop = arrTaskLoop

        objWorkFlowTaskInfo.arrTask = objWorkFlowTaskInfo.arrTask :+ objTaskInfo
      }
    }

    objWorkFlowTaskInfo
  }
}

class XMLReaderLibMeta(strFileName: String, bResource: Boolean) {
  def parseXMLFile(): LibMeta = {
    var xmlFile: Elem = null

    if (bResource) {
      xmlFile = XML.load(getClass.getResourceAsStream(strFileName))
    }
    else {
      xmlFile = XML.loadFile(strFileName)
    }

    XmlReader.of[LibMeta].read(xmlFile).getOrElse(null)
  }
}