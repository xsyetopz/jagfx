package jagfx.ui.components.canvas

import jagfx.ui.viewmodel.EnvelopeViewModel
import jagfx.utils.ColorUtils._
import jagfx.utils.DrawingUtils._
import jagfx.Constants.Int16

/** Canvas rendering envelope segments with grid. */
class JagEnvelopeCanvas extends JagBaseCanvas:
  private var viewModel: Option[EnvelopeViewModel] = None

  getStyleClass.add("jag-envelope-canvas")

  private var graphColor: Int = Graph

  def setGraphColor(color: Int): Unit =
    graphColor = color
    requestRedraw()

  def setViewModel(vm: EnvelopeViewModel): Unit =
    viewModel = Some(vm)
    vm.addChangeListener(() =>
      javafx.application.Platform.runLater(() => requestRedraw())
    )
    requestRedraw()

  override protected def drawContent(buffer: Array[Int], w: Int, h: Int): Unit =
    drawGrid(buffer, w, h)
    drawCenterLine(buffer, w, h)
    viewModel.foreach(vm => drawEnvelope(buffer, w, h, vm))

  private def drawGrid(buffer: Array[Int], w: Int, h: Int): Unit =
    // 8 cols
    for i <- 1 until 8 do
      val x = i * w / 8
      line(buffer, w, h, x, 0, x, h, GridLineFaint)
    // 4 rows
    for i <- 1 until 4 do
      val y = i * h / 4
      line(buffer, w, h, 0, y, w, y, GridLineFaint)

  private def drawEnvelope(
      buffer: Array[Int],
      w: Int,
      h: Int,
      vm: EnvelopeViewModel
  ): Unit =
    val segments = vm.getSegments
    if segments.nonEmpty then
      val zoomedWidth = w * zoomLevel
      val step = zoomedWidth.toDouble / math.max(1, segments.length - 1)

      // Y calc: 0 at bottom, 1 at top, 0.5 at center
      var prevX = 0 - panOffset
      val range = Int16.Range.toDouble
      var prevY = ((1.0 - segments(0) / range) * h).toInt
      if prevX >= 0 && prevX < w then
        fillRect(buffer, w, h, prevX - 1, prevY - 1, 3, 3, graphColor)

      for i <- 1 until segments.length do
        val x = (i * step).toInt - panOffset
        val y = ((1.0 - segments(i) / range) * h).toInt
        // only draw if visible
        if x >= -w && x < w * 2 && prevX >= -w && prevX < w * 2 then
          line(buffer, w, h, prevX, prevY, x, y, graphColor)
          if x >= 0 && x < w then
            fillRect(buffer, w, h, x - 1, y - 1, 3, 3, graphColor)
        prevX = x
        prevY = y

object JagEnvelopeCanvas:
  def apply(): JagEnvelopeCanvas = new JagEnvelopeCanvas()
