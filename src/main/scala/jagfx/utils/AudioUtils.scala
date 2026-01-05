package jagfx.utils

import jagfx.Constants

object AudioUtils:
  import jagfx.types._
  import Constants._

  def msToSamples(ms: Millis): Samples =
    ms.toSamples

  def msToSamples(ms: Double): Samples =
    Samples((ms * SampleRate / 1000.0).toInt)
