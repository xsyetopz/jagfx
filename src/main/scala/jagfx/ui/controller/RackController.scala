package jagfx.ui.controller

import javafx.scene.layout._
import jagfx.ui.viewmodel._
import jagfx.ui.components._
import jagfx.synth.ToneSynthesizer
import javafx.beans.value.ChangeListener

class RackController(viewModel: SynthViewModel, inspector: InspectorController)
    extends IController[GridPane]:
  protected val view = GridPane()
  view.getStyleClass.add("rack")
  view.setHgap(1)
  view.setVgap(1)

  private val cells = new Array[JagCellPane](12)

  private enum CellType:
    case Envelope(
        getter: ToneViewModel => EnvelopeViewModel,
        inspectorMode: Boolean = true
    )
    case Filter
    case Output

  private case class RackCellDef(
      title: String,
      cellType: CellType,
      enabled: Boolean = true
  )

  private val definitions = Vector(
    RackCellDef("PITCH", CellType.Envelope(_.pitch)), // 0
    RackCellDef("VIB RATE", CellType.Envelope(_.vibratoRate)), // 1
    RackCellDef("VIB DEPTH", CellType.Envelope(_.vibratoDepth)), // 2
    RackCellDef("FILT POLE", CellType.Filter), // 3
    RackCellDef("VOLUME", CellType.Envelope(_.volume)), // 4
    RackCellDef("TREM RATE", CellType.Envelope(_.tremoloRate)), // 5
    RackCellDef("TREM DEPTH", CellType.Envelope(_.tremoloDepth)), // 6
    RackCellDef("TRANSITION", CellType.Envelope(_.filterEnvelope)), // 7
    RackCellDef("OUTPUT", CellType.Output), // 8
    RackCellDef("SILENCE", CellType.Envelope(_.gateSilence)), // 9
    RackCellDef("DURATION", CellType.Envelope(_.gateDuration)), // 10
    RackCellDef("RESPONSE", CellType.Filter) // 11
  )

  private val outputWaveformCanvas = JagWaveformCanvas()
  outputWaveformCanvas.setZoom(4)

  private val poleZeroCanvas = JagPoleZeroCanvas()
  private val freqResponseCanvas = JagFrequencyResponseCanvas()

  definitions.zipWithIndex.foreach { case (defCell, idx) =>
    val cell = JagCellPane(defCell.title)
    if !defCell.enabled then cell.setDisable(true)
    else cell.setOnMouseClicked(_ => selectCell(idx))

    defCell.cellType match
      case CellType.Filter =>
        cell.setFeatures(false, false)
        val container = cell.getChildren.get(0).asInstanceOf[VBox]
        val wrapper = container.getChildren.get(1).asInstanceOf[Pane]
        cell.getCanvas.setVisible(false)
        val canvas =
          if idx == 3 then poleZeroCanvas else freqResponseCanvas
        if !wrapper.getChildren.contains(canvas) then
          wrapper.getChildren.add(canvas)
          canvas.widthProperty.bind(wrapper.widthProperty)
          canvas.heightProperty.bind(wrapper.heightProperty)
        cell.setAlternateCanvas(canvas)

      case CellType.Output =>
        val container = cell.getChildren.get(0).asInstanceOf[VBox]
        val wrapper = container.getChildren.get(1).asInstanceOf[Pane]
        cell.getCanvas.setVisible(false)
        if !wrapper.getChildren.contains(outputWaveformCanvas) then
          wrapper.getChildren.add(outputWaveformCanvas)
          outputWaveformCanvas.widthProperty.bind(wrapper.widthProperty)
          outputWaveformCanvas.heightProperty.bind(wrapper.heightProperty)
        cell.setAlternateCanvas(outputWaveformCanvas)

      case _ => // standard envelope cell

    GridPane.setHgrow(cell, Priority.ALWAYS)
    GridPane.setVgrow(cell, Priority.ALWAYS)
    cells(idx) = cell
  }

  viewModel.rackMode.addListener((_, _, _) => buildGrid())
  viewModel.selectedCellIndex.addListener((_, _, _) => updateSelection())
  buildGrid()

  def bind(): Unit =
    bindActiveTone()

  private val activeToneChangeListener: ChangeListener[Number] = (_, _, _) =>
    bindActiveTone()
  viewModel.activeToneIndexProperty.addListener(activeToneChangeListener)
  viewModel.fileLoadedProperty.addListener((_, _, _) => bindActiveTone())

  for i <- 0 until viewModel.getTones.size do
    viewModel.getTones.get(i).addChangeListener(() => updateOutputWaveform())

  private def buildGrid(): Unit =
    view.getChildren.clear()
    view.getColumnConstraints.clear()

    val mode = viewModel.rackMode.get
    val indices = mode match
      case RackMode.Main   => Vector(0, 1, 2, 4, 5, 6, 8, 9, 10)
      case RackMode.Filter => Vector(3, 7, 11)
      case RackMode.Both   => (0 to 11).toVector

    // Main/Filter use 3-col grid, Both uses 4-col
    val cols = if mode == RackMode.Both then 4 else 3

    val constraint = new ColumnConstraints()
    constraint.setPercentWidth(100.0 / cols)
    for _ <- 0 until cols do view.getColumnConstraints.add(constraint)

    indices.zipWithIndex.foreach { case (cellIdx, i) =>
      val cell = cells(cellIdx)
      val col = i % cols
      val row = i / cols

      view.add(cell, col, row)
    }
    updateSelection()

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

  /** Returns envelope for cell index, or `None` for disabled cells. */
  private def bindInspector(idx: Int): Unit =
    val tone = viewModel.getActiveTone
    val cellDef = definitions(idx)
    cellDef.cellType match
      case CellType.Filter => inspector.bindFilter(tone.filterViewModel)
      case CellType.Envelope(getter, _) =>
        val env = getter(tone)
        inspector.bind(env)
      case _ => inspector.hide()

  private def bindActiveTone(): Unit =
    val tone = viewModel.getActiveTone
    for idx <- cells.indices if cells(idx) != null do
      val cellDef = definitions(idx)
      cellDef.cellType match
        case CellType.Envelope(getter, _) =>
          cells(idx).setViewModel(getter(tone))
        case _ => // do nothing for other types

    poleZeroCanvas.setViewModel(tone.filterViewModel)
    freqResponseCanvas.setViewModel(tone.filterViewModel)

    updateOutputWaveform()

  private def updateOutputWaveform(): Unit =
    javafx.application.Platform.runLater(() =>
      val toneVm = viewModel.getActiveTone
      toneVm.toModel() match
        case Some(tone) =>
          val audio = ToneSynthesizer.synthesize(tone)
          outputWaveformCanvas.setAudioBuffer(audio)
        case None =>
          outputWaveformCanvas.clearAudio()
    )

  /** Set playhead position (`0.0` to `1.0`) on output waveform. */
  def setPlayheadPosition(position: Double): Unit =
    outputWaveformCanvas.setPlayheadPosition(position)

  /** Hide playhead on output waveform. */
  def hidePlayhead(): Unit =
    outputWaveformCanvas.hidePlayhead()
