package jagfx.ui

import javafx.scene.layout._
import javafx.scene.control.Label
import javafx.geometry.Pos
import jagfx.ui.viewmodel._
import jagfx.ui.components._

enum InspectorMode:
  case Envelope, Filter

class InspectorController(viewModel: SynthViewModel) extends IController[VBox]:
  protected val view = VBox()
  view.getStyleClass.add("inspector")
  view.setSpacing(8)
  view.setPrefWidth(170)

  private var currentMode: InspectorMode = InspectorMode.Envelope

  // Components
  private val envelopeInspector = EnvelopeInspector()
  private val filterInspector = FilterInspector()

  // Help section
  private val helpPane = VBox()
  helpPane.setSpacing(4)
  helpPane.getStyleClass.add("help-pane")
  helpPane.setAlignment(Pos.TOP_LEFT)

  private val helpLabel = Label("INFO")
  helpLabel.getStyleClass.addAll("label", "h-head")

  private val helpText = Label("")
  helpText.getStyleClass.add("help-text")
  helpText.setWrapText(true)
  helpText.setStyle("-fx-text-fill: #666; -fx-font-size: 9px;")
  helpText.setAlignment(Pos.TOP_LEFT)

  helpPane.getChildren.addAll(helpLabel, helpText)

  // Layout
  private val topPane = VBox()
  topPane.setSpacing(8)
  topPane.setAlignment(Pos.TOP_LEFT)
  topPane.getChildren.addAll(envelopeInspector, filterInspector)
  VBox.setVgrow(topPane, Priority.ALWAYS)

  private val separator = new Region()
  separator.setMinHeight(1)
  separator.setMaxHeight(1)
  separator.setStyle("-fx-background-color: #333;")

  view.getChildren.addAll(topPane, separator, helpPane)
  view.setVisible(false)
  filterInspector.setVisible(false)
  filterInspector.setManaged(false)

  def bind(envelope: EnvelopeViewModel): Unit =
    currentMode = InspectorMode.Envelope
    view.setVisible(true)
    envelopeInspector.setVisible(true)
    envelopeInspector.setManaged(true)
    filterInspector.setVisible(false)
    filterInspector.setManaged(false)
    envelopeInspector.bind(envelope)
    helpText.setText(envelopeInspector.getHelpText)

  def bindFilter(filter: FilterViewModel): Unit =
    currentMode = InspectorMode.Filter
    view.setVisible(true)
    envelopeInspector.setVisible(false)
    envelopeInspector.setManaged(false)
    filterInspector.setVisible(true)
    filterInspector.setManaged(true)
    filterInspector.bind(filter)
    helpText.setText(filterInspector.getHelpText)

  def hide(): Unit =
    view.setVisible(false)
