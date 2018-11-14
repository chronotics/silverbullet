package org.chronotics.silverbullet.scala.akka.state

trait TaskState
case object Idle extends TaskState
case object Busy extends TaskState
