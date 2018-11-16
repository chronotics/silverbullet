package org.chronotics.silverbullet.scala.util

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

import com.microsoft.sqlserver.jdbc.SQLServerDataSource
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import com.typesafe.config.ConfigFactory
import oracle.jdbc.pool.OracleDataSource
import org.chronotics.pandora.java.log.LoggerFactory
import org.chronotics.silverbullet.scala.akka.util.EnvConfig
import org.chronotics.silverbullet.scala.model.{DBAlib, DBWorkFlow}

object DatabaseConnection {
  var _instance: DatabaseConnection = null

  var lockObject = new Object()

  def getInstance(databaseType: String) = {
    lockObject.synchronized {
      if (_instance == null) {
        _instance = new DatabaseConnection(databaseType)
      }
    }

    _instance
  }
}

class DatabaseConnection(databaseType: String) {
  val log = LoggerFactory.getLogger(getClass)
  var lockObj: Object = new Object()

  val config = ConfigFactory.parseFile(EnvConfig.getConfigFile("z2_conn")).resolve()
  val strPrefix = EnvConfig.getPrefix()
  var dbConnection: Connection = null
  var dbUrl: String = ""
  var dbDriver: String = ""
  var dbUser: String = ""
  var dbPass: String = ""

  def getConnection() = {

    databaseType match {
      case "oracle" => {
        dbUrl = config.getString("oracle.url")
        dbDriver = config.getString("oracle.driver")
        dbUser = config.getString("oracle.username")
        dbPass = config.getString("oracle.password")

        var oracleDataSource = new OracleDataSource()
        oracleDataSource.setURL(dbUrl)
        oracleDataSource.setUser(dbUser)
        oracleDataSource.setPassword(dbPass)

        dbConnection = oracleDataSource.getConnection
      }

      case "oracle12c" => {
        dbUrl = config.getString("oracle12c.url")
        dbDriver = config.getString("oracle12c.driver")
        dbUser = config.getString("oracle12c.username")
        dbPass = config.getString("oracle12c.password")

        var oracleDataSource = new OracleDataSource()
        oracleDataSource.setURL(dbUrl)
        oracleDataSource.setUser(dbUser)
        oracleDataSource.setPassword(dbPass)

        dbConnection = oracleDataSource.getConnection
      }

      case "sqlite" => {
        Class.forName("org.sqlite.JDBC")
        var strSQLiteURL = config.getString("sqlitedb.url").replace("[PREFIX]", strPrefix)
        dbConnection = DriverManager.getConnection(strSQLiteURL)
      }
      case "mysql" => {
        dbUrl = config.getString("mysql.url")
        dbDriver = config.getString("mysql.driver")
        dbUser = config.getString("mysql.username")
        dbPass = config.getString("mysql.password")

        var mysqlDataSource = new MysqlDataSource()
        mysqlDataSource.setURL(dbUrl)
        mysqlDataSource.setUser(dbUser)
        mysqlDataSource.setPassword(dbPass)

        dbConnection = mysqlDataSource.getConnection
      }
      case "sqlserver" => {
        dbUrl = config.getString("sqlserver.url")
        dbDriver = config.getString("sqlserver.driver")
        dbUser = config.getString("sqlserver.username")
        dbPass = config.getString("sqlserver.password")

        var sqlsrvDataSource = new SQLServerDataSource()
        sqlsrvDataSource.setURL(dbUrl)
        sqlsrvDataSource.setUser(dbUser)
        sqlsrvDataSource.setPassword(dbPass)

        dbConnection = sqlsrvDataSource.getConnection
      }
      case "db2" => {
        dbUrl = config.getString("db2.url")
        dbDriver = config.getString("db2.driver")
        dbUser = config.getString("db2.username")
        dbPass = config.getString("db2.password")

        Class.forName(dbDriver)
        dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPass)
      }
    }
  }

  def getAlibList(): Seq[DBAlib] = {
    var arrAlib: Seq[DBAlib] = Seq.empty

    try {
      getConnection()

      val objStmt = dbConnection.createStatement()

      try {
        val objResults = objStmt.executeQuery("select ID, EXEC_TYPE_ID, EXEC_XML, EXEC_LANG_CD from EXEC_M where USED_YN = 1")

        try {
          while (objResults.next()) {
            val curDBAlib = new DBAlib(objResults.getInt(1), objResults.getString(2), objResults.getString(3), objResults.getString(4))
            arrAlib = arrAlib :+ curDBAlib
            log.debug("Alib: " + curDBAlib.id + " - " + curDBAlib.alib_type)
          }
        } finally {
          objResults.close()
        }
      } catch {
        case ex: Throwable => log.error("ERR", ex)
      } finally {
        objStmt.close()
      }
    } catch {
      case ex: Throwable => log.error("ERR", ex)
    } finally {
      dbConnection.close()
    }

    arrAlib
  }

  def getWorkflowList(): Seq[DBWorkFlow] = {
    var arrWorkFlow: Seq[DBWorkFlow] = Seq.empty

    try {
      getConnection()

      val objStmt = dbConnection.createStatement()

      try {
        val objResults = objStmt.executeQuery("select ID, FLOW_XML from WORKFLOW_M where USED_YN = 1 and DEPLOY_YN = 1")

        try {
          while (objResults.next()) {
            var curDBWorkflow = new DBWorkFlow(objResults.getInt(1), objResults.getString(2))
            arrWorkFlow = arrWorkFlow :+ curDBWorkflow

            log.debug("Workflow: " + curDBWorkflow.id)
          }
        } finally {
          objResults.close()
        }
      } catch {
        case ex: Throwable => log.error("ERR", ex)
      } finally {
        objStmt.close()
      }
    } catch {
      case ex: Throwable => log.error("ERR", ex)
    } finally {
      dbConnection.close()
    }

    arrWorkFlow
  }

  def readWorkFlowJDBC(workflowId: Int): String = {
    var strXML: String = ""
    var objStmt: PreparedStatement = null;
    var objResults: ResultSet = null;
    try {
      getConnection()

      objStmt = dbConnection.prepareStatement("SELECT ID, FLOW_XML FROM WORKFLOW_M WHERE ID = ?");
      objStmt.setInt(1, workflowId);

      objResults = objStmt.executeQuery();

      if (objResults.next()) {
        strXML = objResults.getString("FLOW_XML");
      }
    } catch {
      case e: Throwable => {
        log.error("ERR", e)
      }
    } finally {
      if (objResults != null) { objResults.close(); }
      if (objStmt != null) { objStmt.close(); }
      if (dbConnection != null) { dbConnection.close(); }
    }

    strXML;
  }

  def readAlibJDBC(intAlibId: Int): (String, String) = {
    var strXML: String = ""
    var strLang: String = ""

    try {
      getConnection()

      val objStmt = dbConnection.createStatement()

      try {
        val objResults = objStmt.executeQuery("select ID, EXEC_TYPE_ID, EXEC_LANG_CD, EXEC_XML from EXEC_M where USED_YN = 1 AND ID = " + intAlibId)

        try {
          while (objResults.next()) {
            strLang = objResults.getString(3)
            strXML = objResults.getString(4)
          }
        } finally {
          objResults.close()
        }
      } catch {
        case ex: Throwable => {
          log.error("ERR", ex)
        }
      } finally {
        objStmt.close()
      }
    } catch {
      case ex: Throwable => {
        log.error("ERR", ex)
      }
    } finally {
      dbConnection.close()
    }

    (strLang, strXML)
  }

  def insertToResource(mapParam: Map[String, String]): Boolean = {
    var bIsSuccess: Boolean = false

    try {
      getConnection()

      var strQuery: String = "INSERT INTO RESOURCE_M(TYPE, SCRIPT, FILE_NM, FILE_PATH, CRE_USER, CRE_DT)"
      strQuery += " VALUES(?, ?, ?, ?, ?, DATETIME('NOW', 'LOCALTIME'))"

      val objStmt = dbConnection.prepareStatement(strQuery)
      var intParamIdx = 1

      for ((strKey, strVal) <- mapParam) {
        objStmt.setString(intParamIdx, strVal)
        intParamIdx += 1
      }

      objStmt.execute()

      bIsSuccess = true
    } catch {
      case ex: Throwable => log.error("ERR", ex)
    } finally {
      dbConnection.close()
    }

    bIsSuccess
  }
}
