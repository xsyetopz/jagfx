package jagfx.model

/** Waveform type for oscillators and LFOs. */
enum Waveform(val id: Int):
  case Off extends Waveform(0)
  case Square extends Waveform(1)
  case Sine extends Waveform(2)
  case Saw extends Waveform(3)
  case Noise extends Waveform(4)

/** Waveform companion with factory method. */
object Waveform:
  /** Creates `Waveform` from numeric ID (`0-4`). Unknown IDs return `Off`. */
  def fromId(id: Int): Waveform = id match
    case 1 => Square
    case 2 => Sine
    case 3 => Saw
    case 4 => Noise
    case _ => Off
