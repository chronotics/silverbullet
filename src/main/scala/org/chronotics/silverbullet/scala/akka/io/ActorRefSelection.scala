package org.chronotics.silverbullet.scala.akka.io

import akka.actor.{ActorContext, ActorRef}
import akka.event.LoggingAdapter
import akka.util.Timeout
import org.chronotics.pandora.java.exception.ExceptionUtil

import scala.concurrent.Await
import scala.concurrent.duration._

object ActorRefSelection {
  def getActorRefOfSelection(strPath: String, context: ActorContext, log: LoggingAdapter, timeout: Int = 5): ActorRef = {
    implicit val resolveTimeout = Timeout(timeout seconds)

    try {
      val actorRef = Await.result(context.actorSelection(strPath).resolveOne(), resolveTimeout.duration)
      actorRef
    } catch {
      case ex: Throwable => {
        log.error("ERR: " + ExceptionUtil.getStrackTrace(ex))
        null
      }
    }
  }
}
