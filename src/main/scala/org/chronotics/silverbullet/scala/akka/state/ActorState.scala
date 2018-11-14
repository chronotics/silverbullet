package org.chronotics.silverbullet.scala.akka.state

trait ActorState
case object Initializing extends ActorState
case object Started extends ActorState
case object Stopped extends ActorState
case object Restarted extends ActorState
case object Working extends ActorState
case object Finished extends ActorState
case object NotSatisfied extends ActorState
case object Failed extends ActorState
case object Waiting extends ActorState

