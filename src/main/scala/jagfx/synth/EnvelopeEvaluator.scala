package jagfx.synth

import jagfx.model._
import jagfx.Constants.Int16

/** Stateful envelope evaluator that interpolates between segments over time.
  * Call `reset()` before each synthesis pass, then `evaluate()` for each
  * sample.
  *
  * @param envelope
  *   `Envelope` definition to evaluate
  */
class EnvelopeEvaluator(envelope: Envelope):
  private var threshold: Int = 0
  private var position: Int = 0
  private var delta: Int = 0
  private var amplitude: Int = 0
  private var ticks: Int = 0

  /** Resets evaluator state for new synthesis pass. */
  def reset(): Unit =
    threshold = 0
    position = 0
    delta = 0
    amplitude = envelope.start << 15
    ticks = 0

  /** Evaluates envelope at current tick, advancing internal state.
    *
    * Returns interpolated value scaled to `0-65535` range.
    */
  def evaluate(period: Int): Int =
    if envelope.segments.isEmpty then return envelope.start

    if ticks >= threshold then
      amplitude = envelope.segments(position).peak << 15
      position += 1

      if position >= envelope.segments.length then
        position = envelope.segments.length - 1

      threshold = ((envelope
        .segments(position)
        .duration
        .toDouble / Int16.Range) * period).toInt

      if threshold > ticks then
        delta = ((envelope
          .segments(position)
          .peak << 15) - amplitude) / (threshold - ticks)
      else delta = 0

    amplitude += delta
    ticks += 1
    (amplitude - delta) >> 15
