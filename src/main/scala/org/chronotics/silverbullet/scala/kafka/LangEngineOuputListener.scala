package org.chronotics.silverbullet.scala.kafka

import java.io.OutputStream

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.chronotics.pandora.java.exception.ExceptionUtil
import org.chronotics.pandora.java.log.LoggerFactory
import org.chronotics.pithos.ext.redis.scala.adaptor.RedisPoolConnection
import org.chronotics.silverbullet.scala.akka.io.LoggingIO
import org.chronotics.silverbullet.scala.akka.util.EnvConfig

class LangEngineOutputListener(strCallback: String, strUserId: String, strRequestId: String, strWorkflowId: String, strTaskId: String, strLibId: String,
                               kafkaProducer: KafkaProducer[Array[Byte], String]) extends OutputStream {
  val log = LoggerFactory.getLogger(getClass())
  val config = ConfigFactory.parseFile(EnvConfig.getConfigFile("z2_conn")).resolve()
  val topic = config.getString("kafka.topic")
  val redisHost = config.getString("redis.host")
  val redisPort = config.getInt("redis.port")
  val redisDb = config.getInt("redis.db")
  var redisPass = config.getString("redis.pass")
  val redisCli = RedisPoolConnection.getInstance(redisHost, redisPort, redisDb, redisPass, 60000, 60000)
  var arrMessage: List[String] = List.empty
  var arrKafkaMessage: List[String] = List.empty
  var intCurIndex: Int = 0
  val loggingIO = LoggingIO.getInstance()
  var propsKafka = new java.util.Properties()
  var strCurRequest = ""
  val partition = 0
  val key = null
  var objStrBuilder: StringBuilder = StringBuilder.newBuilder

  override def write(intByte: Int) {
    try {
      if (intByte != 10) {
        objStrBuilder.append(intByte.toChar)
      } else {
        try {
          if (!objStrBuilder.toString().isEmpty()) {
            var strMessage = objStrBuilder.toString()

            sendKafkaMessage(strMessage)
          }
        } catch {
          case ex: Throwable => {
            log.error("ERR: ", ExceptionUtil.getStrackTrace(ex))
          }
        }

        objStrBuilder = StringBuilder.newBuilder
      }
    } catch {
      case ex: Throwable =>
        log.warn("WARN: " + ExceptionUtil.getStrackTrace(ex))
    }
  }

  def sendKafkaMessage(message: String) {
    try {
      var lCurTimestamp = EnvConfig.getCurrentTimestamp
      var record = new ProducerRecord[Array[Byte], String](topic, partition, lCurTimestamp, key, strCurRequest + message)

      kafkaProducer.send(record, new Callback {
        override def onCompletion(recordMetadata: RecordMetadata, ex: Exception): Unit = {
        }
      })

      kafkaProducer.flush
    } catch {
      case ex: Throwable => {
        log.error("ERR: " + ExceptionUtil.getStrackTrace(ex))
      }
    }
  }
}
