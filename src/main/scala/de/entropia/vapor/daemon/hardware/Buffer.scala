package de.entropia.vapor.hardware

import scala.collection.mutable
import grizzled.slf4j.Logging
import de.entropia.vapor.config.Config


/**
 * Buffer for LED states.
 *
 * Vaporlight LED modules require all of their LEDs to be updated at once.
 * This class caches the state of all LEDs so that even when only individual
 * LED states change, update messages can still be generated.
 */
class Buffer(val config: Config, val encoder: Encoder) extends Logging {
  val channelCounts = config.getModuleChannelCounts()
  val updates = mutable.Map[Byte, mutable.Seq[Byte]]()

  def set(module: Byte, position: Int, value: Byte) {
    if (!updates.contains(module)) {
      updates(module) = Array.fill(channelCounts(module)) {
        0.toByte
      }
    }
    updates(module)(position) = value
  }

  def strobe() {
    for ((module, values) <- updates) {
      encoder.update(module, values)
    }
    encoder.strobe()
    updates.clear()
  }
}
