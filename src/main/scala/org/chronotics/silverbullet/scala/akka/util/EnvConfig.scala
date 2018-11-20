package org.chronotics.silverbullet.scala.akka.util

import java.io.File
import java.nio.file.Paths
import java.util.Calendar

import com.typesafe.config.ConfigFactory

object EnvConfig {
  def getOS(): Int = {
    var strOSName = System.getProperty("os.name").toLowerCase();

    if(strOSName.indexOf("win") >= 0) {
      2
    }
    else if(strOSName.indexOf("nix") >= 0 || strOSName.indexOf("nux") >= 0 || strOSName.indexOf("aix") >= 0) {
      1
    }
    else {
      0
    }
  }

  def getCurrentDir(): String = {
    var strConfigDir = System.getProperty("user.dir")

    strConfigDir
  }

  def getCurrentHomeDir(): String = {
    var strConfigDir = System.getenv("CRON_HOME")

    if (strConfigDir == null || strConfigDir.isEmpty()) {
      strConfigDir = System.getProperty("user.home")

      if (strConfigDir == null || strConfigDir.isEmpty()) {
        strConfigDir = "/opt/ide/workspace/chronotics/chron-app-config"
      } else {
        strConfigDir = strConfigDir + "/openDAP"
      }
    }

    strConfigDir
  }

  def getRuntimeMode(): String = {
    var strRuntimeMode = System.getenv("AKKA_MODE")

    if (strRuntimeMode != null && !strRuntimeMode.isEmpty()) {
      "." + strRuntimeMode
    } else {
      ""
    }
  }

  def getConfigFile(fileConfig: String, lang: String = "scala"): File = {
    var config_file = Paths.get(getCurrentHomeDir(), "resources", lang, fileConfig + EnvConfig.getRuntimeMode() + ".conf").toString

    new File(config_file)
  }

  def getPrefix(): String = {
    val connConfig = ConfigFactory.parseFile(getConfigFile("z2_conn")).resolve()
    var strPrefix = ""

    getOS() match {
      case 1 => strPrefix = connConfig.getString("java_exec.prefix_linux")
      case 2 => strPrefix = connConfig.getString("java_exec.prefix_windows")
      case _ => strPrefix = connConfig.getString("java_exec.prefix_linux")
    }

    strPrefix
  }

  def isUsedHDFS(): Boolean = {
    val connConfig = ConfigFactory.parseFile(getConfigFile("z2_conn")).resolve()

    connConfig.getInt("hdfs.is_used") match {
      case 1 => true
      case _ => false
    }
  }

  def getCurrentTimestamp(): Long = {
    return Calendar.getInstance.getTimeInMillis
  }
}
