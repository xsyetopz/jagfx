package jagfx.ui.components

import javafx.scene.layout._
import javafx.scene.control.Label
import javafx.geometry.Pos
import jagfx.ui.viewmodel.ToneViewModel

/** Grouped container for Gate controls (Silence + Duration). Displays 2 mini
  * envelope cells in a horizontal strip.
  */
class JagGatePane extends VBox:
  getStyleClass.add("gate-pane")
  setSpacing(2)

  private val header = Label("GATE")
  header.getStyleClass.add("panel-head")
  header.setMaxWidth(Double.MaxValue)
  header.setAlignment(Pos.CENTER)

  private val row = HBox(2)
  row.setAlignment(Pos.CENTER)
  VBox.setVgrow(row, Priority.ALWAYS)

  private val silenceCell = JagCellPane("SILENCE")
  private val durationCell = JagCellPane("DURATION")

  // Compact mode for mini cells
  silenceCell.setFeatures(false, false)
  durationCell.setFeatures(false, false)

  HBox.setHgrow(silenceCell, Priority.ALWAYS)
  HBox.setHgrow(durationCell, Priority.ALWAYS)

  row.getChildren.addAll(silenceCell, durationCell)

  getChildren.addAll(header, row)

  def bind(tone: ToneViewModel): Unit =
    silenceCell.setViewModel(tone.gateSilence)
    durationCell.setViewModel(tone.gateDuration)

  def getCells: Seq[JagCellPane] =
    Seq(silenceCell, durationCell)

object JagGatePane:
  def apply(): JagGatePane = new JagGatePane()
