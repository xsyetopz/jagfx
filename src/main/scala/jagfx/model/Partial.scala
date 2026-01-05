package jagfx.model

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
    volume: Int,
    pitchOffset: Int,
    startDelay: Int
)
