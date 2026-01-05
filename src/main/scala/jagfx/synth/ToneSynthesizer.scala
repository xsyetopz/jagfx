package jagfx.synth

import jagfx.Constants
import jagfx.Constants.Int16
import jagfx.Constants.NoisePhaseDiv
import jagfx.Constants.PhaseMask
import jagfx.Constants.PhaseScale
import jagfx.model.*
import jagfx.utils.MathUtils.clipInt16

/** Synthesizes single `Tone` into audio samples using FM/AM modulation. */
object ToneSynthesizer:
  /** Generates audio samples from `Tone` definition. */
  def synthesize(tone: Tone): AudioBuffer =
    val sampleCount = tone.duration * Constants.SampleRate / 1000
    if sampleCount <= 0 || tone.duration < 10 then return AudioBuffer.empty(0)

    val samplesPerStep = sampleCount.toDouble / tone.duration.toDouble
    val buffer = BufferPool.acquire(sampleCount)

    val state = initSynthState(tone, samplesPerStep)
    renderSamples(buffer, tone, state, sampleCount)

    applyGating(buffer, tone, sampleCount)
    applyEcho(buffer, tone, samplesPerStep, sampleCount)

    tone.filter.foreach { f =>
      FilterSynthesizer.apply(buffer, f, sampleCount)
    }

    clipInt16(buffer, sampleCount)

    val output = new Array[Int](sampleCount)
    System.arraycopy(buffer, 0, output, 0, sampleCount)
    BufferPool.release(buffer)
    AudioBuffer(output, Constants.SampleRate)

  // Types
  private case class SynthState(
      freqBaseEval: EnvelopeEvaluator,
      ampBaseEval: EnvelopeEvaluator,
      freqModRateEval: Option[EnvelopeEvaluator],
      freqModRangeEval: Option[EnvelopeEvaluator],
      ampModRateEval: Option[EnvelopeEvaluator],
      ampModRangeEval: Option[EnvelopeEvaluator],
      frequencyStart: Int,
      frequencyDuration: Int,
      amplitudeStart: Int,
      amplitudeDuration: Int,
      partialDelays: Array[Int],
      partialVolumes: Array[Int],
      partialSemitones: Array[Int],
      partialStarts: Array[Int]
  )

  private def initSynthState(tone: Tone, samplesPerStep: Double): SynthState =
    val freqBaseEval = EnvelopeEvaluator(tone.pitchEnvelope)
    val ampBaseEval = EnvelopeEvaluator(tone.volumeEnvelope)
    freqBaseEval.reset()
    ampBaseEval.reset()

    val (freqModRateEval, freqModRangeEval, vibratoLfoIncr, vibratoLfoBase) =
      initFrequencyModulation(tone, samplesPerStep)

    val (ampModRateEval, ampModRangeEval, amplitudeStart, amplitudeDuration) =
      initAmplitudeModulation(tone, samplesPerStep)

    val (delays, volumes, semitones, starts) =
      initPartials(tone, samplesPerStep)

    SynthState(
      freqBaseEval,
      ampBaseEval,
      freqModRateEval,
      freqModRangeEval,
      ampModRateEval,
      ampModRangeEval,
      vibratoLfoIncr,
      vibratoLfoBase,
      amplitudeStart,
      amplitudeDuration,
      delays,
      volumes,
      semitones,
      starts
    )

  private def initFrequencyModulation(
      tone: Tone,
      samplesPerStep: Double
  ): (Option[EnvelopeEvaluator], Option[EnvelopeEvaluator], Int, Int) =
    tone.vibratoRate match
      case Some(env) =>
        val rateEval = EnvelopeEvaluator(env)
        val rangeEval = tone.vibratoDepth.map(EnvelopeEvaluator(_))
        rateEval.reset()
        rangeEval.foreach(_.reset())
        val start = ((env.end - env.start) * PhaseScale / samplesPerStep).toInt
        val duration = (env.start * PhaseScale / samplesPerStep).toInt
        (Some(rateEval), rangeEval, start, duration)
      case None =>
        (None, None, 0, 0)

  private def initAmplitudeModulation(
      tone: Tone,
      samplesPerStep: Double
  ): (Option[EnvelopeEvaluator], Option[EnvelopeEvaluator], Int, Int) =
    tone.tremoloRate match
      case Some(env) =>
        val rateEval = EnvelopeEvaluator(env)
        val rangeEval = tone.tremoloDepth.map(EnvelopeEvaluator(_))
        rateEval.reset()
        rangeEval.foreach(_.reset())
        val start = ((env.end - env.start) * PhaseScale / samplesPerStep).toInt
        val duration = (env.start * PhaseScale / samplesPerStep).toInt
        (Some(rateEval), rangeEval, start, duration)
      case None =>
        (None, None, 0, 0)

  private def initPartials(
      tone: Tone,
      samplesPerStep: Double
  ): (Array[Int], Array[Int], Array[Int], Array[Int]) =
    val delays = new Array[Int](Constants.MaxPartials)
    val volumes = new Array[Int](Constants.MaxPartials)
    val semitones = new Array[Int](Constants.MaxPartials)
    val starts = new Array[Int](Constants.MaxPartials)

    for partial <- 0 until math.min(Constants.MaxPartials, tone.partials.length)
    do
      val height = tone.partials(partial)
      if height.volume.value != 0 then
        delays(partial) = (height.startDelay.value * samplesPerStep).toInt
        volumes(partial) = (height.volume.value << 14) / 100
        semitones(partial) =
          ((tone.pitchEnvelope.end - tone.pitchEnvelope.start) * PhaseScale *
            LookupTables.getPitchMultiplier(
              height.pitchOffset
            ) / samplesPerStep).toInt
        starts(partial) =
          (tone.pitchEnvelope.start * PhaseScale / samplesPerStep).toInt

    (delays, volumes, semitones, starts)

  private def renderSamples(
      buffer: Array[Int],
      tone: Tone,
      state: SynthState,
      sampleCount: Int
  ): Unit =
    val phases = new Array[Int](Constants.MaxPartials)
    var frequencyPhase = 0
    var amplitudePhase = 0

    for sample <- 0 until sampleCount do
      var frequency = state.freqBaseEval.evaluate(sampleCount)
      var amplitude = state.ampBaseEval.evaluate(sampleCount)

      val (newFreq, newFreqPhase) =
        applyVibrato(frequency, frequencyPhase, sampleCount, state, tone)
      frequency = newFreq
      frequencyPhase = newFreqPhase
      val (newAmp, newAmpPhase) =
        applyTremolo(amplitude, amplitudePhase, sampleCount, state, tone)
      amplitude = newAmp
      amplitudePhase = newAmpPhase

      renderPartials(
        buffer,
        tone,
        state,
        sample,
        sampleCount,
        frequency,
        amplitude,
        phases
      )

  private def applyVibrato(
      frequency: Int,
      phase: Int,
      sampleCount: Int,
      state: SynthState,
      tone: Tone
  ): (Int, Int) =
    (state.freqModRateEval, state.freqModRangeEval) match
      case (Some(rateEval), Some(rangeEval)) =>
        val rate = rateEval.evaluate(sampleCount)
        val range = rangeEval.evaluate(sampleCount)
        val mod =
          generateSample(range, phase, tone.vibratoRate.get.waveform) >> 1
        val nextPhase =
          phase + (rate * state.frequencyStart >> 16) + state.frequencyDuration
        (frequency + mod, nextPhase)
      case _ => (frequency, phase)

  private def applyTremolo(
      amplitude: Int,
      phase: Int,
      sampleCount: Int,
      state: SynthState,
      tone: Tone
  ): (Int, Int) =
    (state.ampModRateEval, state.ampModRangeEval) match
      case (Some(rateEval), Some(rangeEval)) =>
        val rate = rateEval.evaluate(sampleCount)
        val range = rangeEval.evaluate(sampleCount)
        val mod =
          generateSample(range, phase, tone.tremoloRate.get.waveform) >> 1
        val newAmp = amplitude * (mod + Constants.Int16.UnsignedMaxValue) >> 15
        val nextPhase =
          phase + (rate * state.amplitudeStart >> 16) + state.amplitudeDuration
        (newAmp, nextPhase)
      case _ => (amplitude, phase)

  private def renderPartials(
      buffer: Array[Int],
      tone: Tone,
      state: SynthState,
      sample: Int,
      sampleCount: Int,
      frequency: Int,
      amplitude: Int,
      phases: Array[Int]
  ): Unit =
    for partial <- 0 until math.min(Constants.MaxPartials, tone.partials.length)
    do
      if tone.partials(partial).volume.value != 0 then
        val position = sample + state.partialDelays(partial)
        if position >= 0 && position < sampleCount then
          buffer(position) += generateSample(
            amplitude * state.partialVolumes(partial) >> 15,
            phases(partial),
            tone.pitchEnvelope.waveform
          )
          phases(partial) += (frequency * state.partialSemitones(
            partial
          ) >> 16) +
            state.partialStarts(partial)

  private def generateSample(
      amplitude: Int,
      phase: Int,
      waveform: Waveform
  ): Int =
    waveform match
      case Waveform.Square =>
        if (phase & PhaseMask) < Int16.Quarter then amplitude else -amplitude
      case Waveform.Sine =>
        (LookupTables.sin(phase & PhaseMask) * amplitude) >> 14
      case Waveform.Saw =>
        (((phase & PhaseMask) * amplitude) >> 14) - amplitude
      case Waveform.Noise =>
        LookupTables.noise((phase / NoisePhaseDiv) & PhaseMask) * amplitude
      case Waveform.Off => 0

  private def applyGating(
      buffer: Array[Int],
      tone: Tone,
      sampleCount: Int
  ): Unit =
    (tone.gateSilence, tone.gateDuration) match
      case (Some(silence), Some(duration)) =>
        val silenceEval = EnvelopeEvaluator(silence)
        val durationEval = EnvelopeEvaluator(duration)
        silenceEval.reset()
        durationEval.reset()
        var counter = 0
        var muted = true
        for sample <- 0 until sampleCount do
          val stepOn = silenceEval.evaluate(sampleCount)
          val stepOff = durationEval.evaluate(sampleCount)
          val threshold =
            if muted then
              silence.start + ((silence.end - silence.start) * stepOn >> 8)
            else silence.start + ((silence.end - silence.start) * stepOff >> 8)
          counter += 256
          if counter >= threshold then
            counter = 0
            muted = !muted
          if muted then buffer(sample) = 0
      case _ => ()

  private def applyEcho(
      buffer: Array[Int],
      tone: Tone,
      samplesPerStep: Double,
      sampleCount: Int
  ): Unit =
    if tone.echoDelay > 0 && tone.echoMix > 0 then
      val start = (tone.echoDelay * samplesPerStep).toInt
      for sample <- start until sampleCount do
        buffer(sample) += buffer(sample - start) * tone.echoMix / 100
