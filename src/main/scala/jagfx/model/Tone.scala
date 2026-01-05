package jagfx.model

/** Single instrument voice within `.synth` file. Contains all envelopes,
  * partials, and timing parameters needed to generate audio.
  *
  * @param pitchEnvelope
  *   Fundamental frequency trajectory
  * @param volumeEnvelope
  *   Amplitude trajectory
  * @param vibratoRate
  *   Optional: frequency modulation rate envelope
  * @param vibratoDepth
  *   Optional: frequency modulation depth envelope
  * @param tremoloRate
  *   Optional: amplitude modulation rate envelope
  * @param tremoloDepth
  *   Optional: amplitude modulation depth envelope
  * @param gateRelease
  *   Optional: gate off (release) envelope
  * @param gateAttack
  *   Optional: gate on (attack) envelope
  * @param partials
  *   Additive synthesis partials (overtones)
  * @param echoDelay
  *   Echo delay in milliseconds
  * @param echoMix
  *   Echo mix level `0-100`
  * @param duration
  *   Total tone length in milliseconds
  * @param start
  *   Offset from track start in milliseconds
  * @param filter
  *   Optional: IIR filter parameters
  */
case class Tone(
    pitchEnvelope: Envelope,
    volumeEnvelope: Envelope,
    vibratoRate: Option[Envelope],
    vibratoDepth: Option[Envelope],
    tremoloRate: Option[Envelope],
    tremoloDepth: Option[Envelope],
    gateRelease: Option[Envelope],
    gateAttack: Option[Envelope],
    partials: Vector[Partial],
    echoDelay: Int,
    echoMix: Int,
    duration: Int,
    start: Int,
    filter: Option[Filter] = None
)
