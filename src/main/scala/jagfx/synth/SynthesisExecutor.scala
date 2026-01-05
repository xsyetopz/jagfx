package jagfx.synth

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

import jagfx.model.*
import javafx.application.Platform

/** Executes synthesis operations on background thread. */
object SynthesisExecutor:
  // Fields
  private val executor = Executors.newSingleThreadExecutor { r =>
    val synthThread = new Thread(r, "JagFX-Synth")
    synthThread.setDaemon(true)
    synthThread.setPriority(Thread.NORM_PRIORITY - 1)
    synthThread
  }

  private val generation = new AtomicLong(0)

  /** Synthesizes tone on background thread with auto-cancellation. */
  def synthesizeTone(tone: Tone)(onComplete: AudioBuffer => Unit): Unit =
    val thisGen = generation.incrementAndGet()
    executor.submit(
      (
          () =>
            if generation.get() == thisGen then
              val audio = ToneSynthesizer.synthesize(tone)
              if generation.get() == thisGen then
                Platform.runLater(() => onComplete(audio))
      ): Runnable
    )

  /** Synthesizes full track on background thread. */
  def synthesizeTrack(
      file: SynthFile,
      loopCount: Int,
      toneFilter: Int = -1
  )(onComplete: AudioBuffer => Unit): Unit =
    val thisGen = generation.incrementAndGet()
    executor.submit(
      (
          () =>
            if generation.get() == thisGen then
              val audio =
                TrackSynthesizer.synthesize(file, loopCount, toneFilter)
              if generation.get() == thisGen then
                Platform.runLater(() => onComplete(audio))
      ): Runnable
    )

  /** Cancels all pending synthesis operations. */
  def cancelPending(): Unit = generation.incrementAndGet()

  /** Shuts down executor. */
  def shutdown(): Unit = executor.shutdownNow()
