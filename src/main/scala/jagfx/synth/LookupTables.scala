package jagfx.synth

import java.util.SplittableRandom

import jagfx.Constants.CircleSegments
import jagfx.Constants.Int16
import jagfx.Constants.SemitoneRange
import jagfx.Constants.SinTableDivisor
import jagfx.utils.MathUtils

/** Precomputed lookup tables for DSP and rendering operations. */
object LookupTables:
  // Constants
  final val DecicentRatio = 1.0057929410678534

  // Tables
  lazy val noise: Array[Int] =
    val rng = new SplittableRandom(0xdeadbeef)
    Array.tabulate(Int16.UnsignedMaxValue)(_ =>
      if rng.nextBoolean() then 1 else -1
    )

  lazy val sin: Array[Int] =
    Array.tabulate(Int16.UnsignedMaxValue)(i =>
      (math.sin(i / SinTableDivisor) * Int16.Quarter).toInt
    )

  lazy val unitCircleX: Array[Double] =
    Array.tabulate(CircleSegments + 1)(i =>
      math.cos(i * MathUtils.TwoPi / CircleSegments)
    )

  lazy val unitCircleY: Array[Double] =
    Array.tabulate(CircleSegments + 1)(i =>
      math.sin(i * MathUtils.TwoPi / CircleSegments)
    )

  private lazy val semitoneCache: Array[Double] =
    Array.tabulate(241)(i => math.pow(DecicentRatio, i - SemitoneRange))

  /** Returns multiplier for given decicent offset. */
  def getPitchMultiplier(decicents: Int): Double =
    if decicents >= -SemitoneRange && decicents <= SemitoneRange then
      semitoneCache(decicents + SemitoneRange)
    else math.pow(DecicentRatio, decicents.toDouble)
