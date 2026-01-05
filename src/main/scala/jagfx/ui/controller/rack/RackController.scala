package jagfx.ui.controller.rack

import javafx.scene.layout._
import jagfx.ui.viewmodel._
import jagfx.ui.components.canvas._
import jagfx.ui.components.pane._
import jagfx.synth.SynthesisExecutor
import jagfx.ui.BindingManager
import jagfx.ui.controller.IController
import jagfx.ui.controller.inspector.InspectorController
import jagfx.utils.ColorUtils

class RackController(viewModel: SynthViewModel, inspector: InspectorController)
    extends IController[GridPane]:
  protected val view = GridPane()
  view.getStyleClass.add("rack")
  view.setHgap(1)
  view.setVgap(1)

  private val cells = new Array[JagCellPane](12)

  private val outputWaveformCanvas = JagWaveformCanvas()
  outputWaveformCanvas.setZoom(4)

  private val poleZeroCanvas = JagPoleZeroCanvas()
  private val freqResponseCanvas = JagFrequencyResponseCanvas()

  private val editor = new RackEditor(viewModel)
  private val factory = new RackCellFactory(
    viewModel,
    poleZeroCanvas,
    freqResponseCanvas,
    outputWaveformCanvas,
    selectCell,
    editor.toggleEditorMode
  )

  private val filterDisplay = new VBox(2):
    getChildren.addAll(poleZeroCanvas, freqResponseCanvas)
    VBox.setVgrow(freqResponseCanvas, Priority.ALWAYS)

  private val bindingManager = BindingManager()

  buildGrid()

  def bind(): Unit =
    bindActiveTone()

  bindingManager.listen(viewModel.activeToneIndexProperty)(_ =>
    bindActiveTone()
  )
  bindingManager.listen(viewModel.fileLoadedProperty)(_ => bindActiveTone())
  bindingManager.listen(viewModel.selectedCellIndex)(_ => updateSelection())

  for i <- 0 until viewModel.getTones.size do
    val toneIdx = i
    viewModel.getTones
      .get(i)
      .addChangeListener(() =>
        if viewModel.getActiveToneIndex == toneIdx then updateOutputWaveform()
      )

  def setPlayheadPosition(position: Double): Unit =
    outputWaveformCanvas.setPlayheadPosition(position)

  def hidePlayhead(): Unit =
    outputWaveformCanvas.hidePlayhead()

  private def buildGrid(): Unit =
    view.getChildren.clear()
    view.getColumnConstraints.clear()
    view.getRowConstraints.clear()

    setupGridConstraints()

    // (Cell Def Index, Col, Row)
    // 0:Pitch, 1:V.Rate, 2:V.Depth, 3:P/Z(Unused), 4:Vol, 5:T.Rate, 6:T.Depth,
    // 7:Filt, 8:Out(Unused), 9:G.Sil, 10:G.Dur, 11:Bode(Unused)

    createAndAddCell(0, 0, 0) // Pitch
    createAndAddCell(1, 0, 1) // V.Rate
    createAndAddCell(2, 0, 2) // V.Depth

    createAndAddCell(4, 1, 0) // Volume
    createAndAddCell(5, 1, 1) // T.Rate
    createAndAddCell(6, 1, 2) // T.Depth

    createAndAddCell(7, 2, 0) // Filter
    createAndAddCell(9, 2, 1) // G.Sil
    createAndAddCell(10, 2, 2) // G.Dur

    cells(8) = factory.createCell(8)
    view.add(cells(8), 0, 3, 3, 1)

    val filterCell = JagCellPane("FILTER DISPLAY")
    filterCell.setFeatures(false, false)
    filterCell.setShowZoomButtons(false)
    val fWrapper = filterCell.getCanvas.getParent.asInstanceOf[Pane]
    filterCell.getCanvas.setVisible(false)
    fWrapper.getChildren.clear()
    fWrapper.getChildren.add(filterDisplay)

    poleZeroCanvas.widthProperty.bind(fWrapper.widthProperty)
    poleZeroCanvas.heightProperty.bind(fWrapper.heightProperty.divide(2))
    freqResponseCanvas.widthProperty.bind(fWrapper.widthProperty)
    freqResponseCanvas.heightProperty.bind(fWrapper.heightProperty.divide(2))

    view.add(filterCell, 3, 0, 1, 4)

    // Editor Overlay
    view.add(editor.getView, 0, 0, 4, 4)
    editor.canvas.widthProperty.bind(view.widthProperty.subtract(20))
    editor.canvas.heightProperty.bind(view.heightProperty.subtract(60))

    updateSelection()

  private def setupGridConstraints(): Unit =
    val colConstraint = new ColumnConstraints()
    colConstraint.setPercentWidth(25)
    for _ <- 0 until 4 do view.getColumnConstraints.add(colConstraint)

    val rowConstraint = new RowConstraints()
    rowConstraint.setVgrow(Priority.ALWAYS)
    for _ <- 0 until 4 do view.getRowConstraints.add(rowConstraint)

  private def createAndAddCell(defIdx: Int, col: Int, row: Int): Unit =
    val cell = factory.createCell(defIdx)
    cells(defIdx) = cell
    view.add(cell, col, row)

  private def updateSelection(): Unit =
    val selectedIdx = viewModel.selectedCellIndex.get
    cells.zipWithIndex.foreach { case (cell, idx) =>
      if cell != null then
        val isSel = idx == selectedIdx
        if cell.selectedProperty.get != isSel then
          cell.selectedProperty.set(isSel)
    }
    if selectedIdx >= 0 && selectedIdx < cells.length then
      bindInspector(selectedIdx)

  private def selectCell(idx: Int): Unit =
    viewModel.selectedCellIndex.set(idx)

  private def bindInspector(idx: Int): Unit =
    val tone = viewModel.getActiveTone
    val cellDef = RackDefs.cellDefs(idx)
    cellDef.cellType match
      case CellType.Filter =>
        inspector.bindFilter(
          tone.filterViewMode,
          cellDef.title,
          cellDef.desc
        )
      case CellType.Envelope(getter, _) =>
        val env = getter(tone)
        inspector.bind(env, cellDef.title, cellDef.desc)
      case _ => inspector.hide()

  private def bindActiveTone(): Unit =
    val tone = viewModel.getActiveTone
    for idx <- cells.indices if cells(idx) != null do
      val cellDef = RackDefs.cellDefs(idx)
      cellDef.cellType match
        case CellType.Envelope(getter, _) =>
          cells(idx).setViewModel(getter(tone))
          if cellDef.title.startsWith("G.") then
            cells(idx).getCanvas match
              case c: JagEnvelopeCanvas =>
                c.setGraphColor(ColorUtils.Gating)
              case null =>
        case _ => // do nothing

    poleZeroCanvas.setViewModel(tone.filterViewMode)
    freqResponseCanvas.setViewModel(tone.filterViewMode)

    updateOutputWaveform()

  private def updateOutputWaveform(): Unit =
    viewModel.getActiveTone.toModel() match
      case Some(tone) =>
        SynthesisExecutor.synthesizeTone(tone) { audio =>
          outputWaveformCanvas.setAudioBuffer(audio)
        }
      case None =>
        outputWaveformCanvas.clearAudio()
