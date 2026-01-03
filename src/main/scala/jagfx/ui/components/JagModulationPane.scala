package jagfx.ui.components

import javafx.scene.layout._
import javafx.scene.control.Label
import javafx.geometry.Pos
import jagfx.ui.viewmodel.ToneViewModel

/** Grouped container for modulation controls (`Vibrato` + `Tremolo`). Displays
  * `4` mini envelope cells in a 2x2 grid.
  */
class JagModulationPane extends VBox:
  getStyleClass.add("modulation-pane")
  setSpacing(2)

  private val header = Label("MODULATION")
  header.getStyleClass.add("panel-head")
  header.setMaxWidth(Double.MaxValue)
  header.setAlignment(Pos.CENTER)

  private val grid = GridPane()
  grid.setHgap(2)
  grid.setVgap(2)
  VBox.setVgrow(grid, Priority.ALWAYS)

  // 2x2 grid constraints
  for _ <- 0 until 2 do
    val col = new ColumnConstraints()
    col.setPercentWidth(50)
    grid.getColumnConstraints.add(col)
    val row = new RowConstraints()
    row.setPercentHeight(50)
    grid.getRowConstraints.add(row)

  private val vibratoRateCell = JagCellPane("VIB RATE")
  private val vibratoDepthCell = JagCellPane("VIB DEPTH")
  private val tremoloRateCell = JagCellPane("TREM RATE")
  private val tremoloDepthCell = JagCellPane("TREM DEPTH")

  // compact mode for mini cells
  vibratoRateCell.setFeatures(false, false)
  vibratoDepthCell.setFeatures(false, false)
  tremoloRateCell.setFeatures(false, false)
  tremoloDepthCell.setFeatures(false, false)

  GridPane.setHgrow(vibratoRateCell, Priority.ALWAYS)
  GridPane.setVgrow(vibratoRateCell, Priority.ALWAYS)
  GridPane.setHgrow(vibratoDepthCell, Priority.ALWAYS)
  GridPane.setVgrow(vibratoDepthCell, Priority.ALWAYS)
  GridPane.setHgrow(tremoloRateCell, Priority.ALWAYS)
  GridPane.setVgrow(tremoloRateCell, Priority.ALWAYS)
  GridPane.setHgrow(tremoloDepthCell, Priority.ALWAYS)
  GridPane.setVgrow(tremoloDepthCell, Priority.ALWAYS)

  grid.add(vibratoRateCell, 0, 0)
  grid.add(vibratoDepthCell, 1, 0)
  grid.add(tremoloRateCell, 0, 1)
  grid.add(tremoloDepthCell, 1, 1)

  getChildren.addAll(header, grid)

  def bind(tone: ToneViewModel): Unit =
    vibratoRateCell.setViewModel(tone.vibratoRate)
    vibratoDepthCell.setViewModel(tone.vibratoDepth)
    tremoloRateCell.setViewModel(tone.tremoloRate)
    tremoloDepthCell.setViewModel(tone.tremoloDepth)

  def getCells: Seq[JagCellPane] =
    Seq(vibratoRateCell, vibratoDepthCell, tremoloRateCell, tremoloDepthCell)

object JagModulationPane:
  def apply(): JagModulationPane = new JagModulationPane()
