package jagfx.model

/** IIR Filter parameters.
  *
  * @param pairCounts
  *   Number of pairs for feedforward (`0`) and feedback (`1`) directions.
  * @param unity
  *   Unity gain values.
  * @param pairPhase
  *   Phase values `[direction][0/1][pair]`
  * @param pairMagnitude
  *   Magnitude values `[direction][0/1][pair]`
  * @param envelope
  *   Optional filter envelope (transition curve).
  */
case class Filter(
    pairCounts: IArray[Int], // [2]
    unity: IArray[Int], // [2]
    pairPhase: IArray[IArray[IArray[Int]]], // [2][2][pairCount]
    pairMagnitude: IArray[IArray[IArray[Int]]], // [2][2][pairCount]
    envelope: Option[Envelope]
)
