package jagfx.ui.controller.inspector

import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.layout._
import javafx.geometry.Pos
import jagfx.ui.viewmodel._
import jagfx.ui.controller.IController
import jagfx.ui.components.button.JagButton

/** Inspector panel for editing envelope or filter parameters. */
class InspectorController(viewModel: SynthViewModel)
    extends IController[ScrollPane]:
  protected val view = ScrollPane()
  view.getStyleClass.add("inspector-scroll")
  view.setFitToWidth(true)
  view.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER)

  private val content = VBox()
  content.getStyleClass.add("inspector-content")
  content.setSpacing(8)
  view.setContent(content)

  private val envInspector = EnvelopeInspector()
  private val fltInspector = FilterInspector()
  private val timingInspector = TimingInspector()

  // Sections
  private val topSection = VBox(8)
  topSection.setAlignment(Pos.TOP_LEFT)

  private val midSection = VBox(8)
  midSection.setAlignment(Pos.TOP_LEFT)

  private val infoSection = VBox(8)
  infoSection.setAlignment(Pos.TOP_LEFT)

  // Separators
  private val sep1 = new Separator()
  private val sep2 = new Separator()

  // Info Text
  private val infoLabel = Label()
  infoLabel.getStyleClass.add("help-text")
  infoLabel.setWrapText(true)
  infoSection.getChildren.add(infoLabel)

  // Assemble
  topSection.getChildren.addAll(envInspector, fltInspector)
  midSection.getChildren.add(timingInspector)

  content.getChildren.addAll(topSection, sep1, midSection, sep2, infoSection)

  // Initial State
  envInspector.setVisible(false); envInspector.setManaged(false)
  fltInspector.setVisible(false); fltInspector.setManaged(false)
  timingInspector.setVisible(false); timingInspector.setManaged(false)

  view.setVisible(false)

  private def show(): Unit =
    view.setVisible(true)

  def bind(envelope: EnvelopeViewModel, title: String, desc: String): Unit =
    show()

    envInspector.setVisible(true); envInspector.setManaged(true)
    fltInspector.setVisible(false); fltInspector.setManaged(false)

    envInspector.bind(envelope)

    timingInspector.setVisible(true); timingInspector.setManaged(true)
    timingInspector.bind(viewModel.getActiveTone)

    infoLabel.setText(s"$desc")

  def bindFilter(filter: FilterViewModel, title: String, desc: String): Unit =
    show()

    envInspector.setVisible(false); envInspector.setManaged(false)
    fltInspector.setVisible(true); fltInspector.setManaged(true)

    fltInspector.bind(filter)

    timingInspector.setVisible(true); timingInspector.setManaged(true)
    timingInspector.bind(viewModel.getActiveTone)

    infoLabel.setText(s"$desc")

  def hide(): Unit =
    view.setVisible(false)
    envInspector.setVisible(false)
    envInspector.setManaged(false)
    fltInspector.setVisible(false)
    fltInspector.setManaged(false)
    timingInspector.setVisible(false)
    timingInspector.setManaged(false)
