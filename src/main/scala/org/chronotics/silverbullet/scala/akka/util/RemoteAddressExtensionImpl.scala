package org.chronotics.silverbullet.scala.akka.util

import akka.actor.{ExtendedActorSystem, Extension, ExtensionKey}

class RemoteAddressExtensionImpl(system: ExtendedActorSystem) extends Extension {
  def address = system.provider.getDefaultAddress
}

object RemoteAddressExtension extends ExtensionKey[RemoteAddressExtensionImpl]
