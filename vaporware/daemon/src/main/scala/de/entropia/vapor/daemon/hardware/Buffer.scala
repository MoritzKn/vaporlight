package de.entropia.vapor.hardware

import scala.collection.mutable
import de.entropia.vapor.daemon.config.Settings
import com.typesafe.scalalogging.slf4j.Logging


/**
 * Buffer for LED states.
 *
 * Vaporlight LED modules require all of their LEDs to be updated at once.
 * This class caches the state of all LEDs so that even when only individual
 * LED states change, update messages can still be generated.
 */
class Buffer(val settings: Settings, val encoder: Encoder) extends Logging {
  val moduleLedStates = mutable.Map[Byte, mutable.Seq[Int]]()
  val dirtyModules = mutable.Set[Byte]()

  def set(module: Byte, position: Int, value: Int) {
    if (!moduleLedStates.contains(module)) {
      moduleLedStates(module) = Array.fill(settings.channelCounts(module)) {
        0
      }
    }
    moduleLedStates(module)(position) = value
    dirtyModules.add(module)

  }

  def strobe() {
    for ((module, values) <- moduleLedStates) {
      encoder.update(module, values)
    }
    encoder.strobe()
    dirtyModules.clear()
  }
}
