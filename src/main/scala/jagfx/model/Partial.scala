package jagfx.model

import jagfx.types.*

/** Additive synthesis partial (partial overtone).
  *
  * @param volume
  *   Amplitude `0-100`
  * @param pitchOffset
  *   Pitch offset in decicents (`10` = `1` semitone)
  * @param startDelay
  *   Start delay in milliseconds
  */
case class Partial(
    volume: Percent,
    pitchOffset: Int,
    startDelay: Millis
)
