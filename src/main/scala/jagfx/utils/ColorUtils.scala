package jagfx.utils

/** Colors synced with `_variables.scss`. */
object ColorUtils:
  // Backgrounds (from SCSS)
  val BgRoot: Int = 0xff121212
  val BgCell: Int = 0xff202020
  val BgCellSel: Int = 0xff252525
  val BgBlack: Int = 0xff000000

  // Borders
  val BorderDim: Int = 0xff333333
  val BorderAccent: Int = 0xff33bbee

  // Text
  val White: Int = 0xfff0f0f0
  val TextDim: Int = 0xff888888

  // Accent
  val Accent: Int = 0xff33bbee // $bg-fill, $border-accent
  val Graph: Int = 0xffee7733 // $bg-graph (envelope curves)

  // Filter
  val FilterPole: Int = 0xff009988 // teal for poles
  val FilterZero: Int = 0xffcc3311 // red-orange for zeros
  val FilterResponse: Int = 0xff33bb99 // frequency response curve

  // Grid
  val GridLineFaint: Int = 0xff2a2a2a
