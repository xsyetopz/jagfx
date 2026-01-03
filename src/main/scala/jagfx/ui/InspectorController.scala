package jagfx.ui

import javafx.scene.layout._
import javafx.scene.control.Label
import javafx.geometry.Pos
import jagfx.ui.viewmodel._
import jagfx.ui.components._
import jagfx.model.WaveForm

enum InspectorMode:
  case Envelope, Filter

class InspectorController(viewModel: SynthViewModel) extends IController[VBox]:
  protected val view = VBox()
  view.getStyleClass.add("inspector")
  view.setSpacing(8)
  view.setPrefWidth(170)

  private var currentMode: InspectorMode = InspectorMode.Envelope
  private var currentEnvelope: Option[EnvelopeViewModel] = None
  private var currentFilter: Option[FilterViewModel] = None

  // === ENVELOPE MODE ===
  private val envelopePane = VBox()
  envelopePane.setSpacing(8)

  private val waveLabel = Label("WAVE")
  waveLabel.getStyleClass.addAll("label", "h-head")

  private val waveGrid = JagToggleGroup(
    ("Off", "mdi2v-volume-off"),
    ("Sq", "mdi2s-square-wave"),
    ("Sin", "mdi2s-sine-wave"),
    ("Saw", "mdi2s-sawtooth-wave"),
    ("Nse", "mdi2w-waveform")
  )
  waveGrid.setAlignment(Pos.CENTER)

  waveGrid.selectedProperty.addListener((_, _, newVal) =>
    currentEnvelope.foreach { env =>
      val form = newVal match
        case "Off" => WaveForm.Off
        case "Sqr" => WaveForm.Square
        case "Sin" => WaveForm.Sine
        case "Saw" => WaveForm.Saw
        case "Nse" => WaveForm.Noise
        case _     => WaveForm.Off
      env.form.set(form)
    }
  )

  // Range section - inline S/E
  private val rangeLabel = Label("RANGE")
  rangeLabel.getStyleClass.addAll("label", "h-head")

  private val startField = JagNumericField(-999999, 999999, 0)
  startField.setPrefWidth(55)
  startField.valueProperty.addListener((_, _, nv) =>
    currentEnvelope.foreach(_.start.set(nv.intValue))
  )

  private val endField = JagNumericField(-999999, 999999, 0)
  endField.setPrefWidth(55)
  endField.valueProperty.addListener((_, _, nv) =>
    currentEnvelope.foreach(_.end.set(nv.intValue))
  )

  private val rangeRow = HBox(4)
  rangeRow.setAlignment(Pos.CENTER_LEFT)
  rangeRow.getChildren.addAll(
    Label("S"),
    startField,
    new Region() { HBox.setHgrow(this, Priority.ALWAYS) },
    Label("E"),
    endField
  )

  // Segments section - info display
  private val segLabel = Label("SEGMENTS")
  segLabel.getStyleClass.addAll("label", "h-head")

  private val segCountLabel = Label("0 pts")
  segCountLabel.getStyleClass.add("label")
  segCountLabel.setStyle("-fx-text-fill: #888;")

  private val segRow = HBox(4)
  segRow.setAlignment(Pos.CENTER_LEFT)
  segRow.getChildren.addAll(
    segLabel,
    new Region() { HBox.setHgrow(this, Priority.ALWAYS) },
    segCountLabel
  )

  envelopePane.getChildren.addAll(
    waveLabel,
    waveGrid,
    rangeLabel,
    rangeRow,
    segRow
  )

  // === FILTER MODE ===
  private val filterPane = VBox()
  filterPane.setSpacing(8)

  // Pairs section - inline FF/FB
  private val pairsLabel = Label("PAIRS")
  pairsLabel.getStyleClass.addAll("label", "h-head")

  private val ffField = JagNumericField(0, 4, 0)
  ffField.setPrefWidth(28)
  ffField.valueProperty.addListener((_, _, nv) =>
    currentFilter.foreach(_.pairCount0.set(nv.intValue))
  )

  private val fbField = JagNumericField(0, 4, 0)
  fbField.setPrefWidth(28)
  fbField.valueProperty.addListener((_, _, nv) =>
    currentFilter.foreach(_.pairCount1.set(nv.intValue))
  )

  private val pairsRow = HBox(4)
  pairsRow.setAlignment(Pos.CENTER_LEFT)
  pairsRow.getChildren.addAll(
    Label("FF"),
    ffField,
    new Region() { HBox.setHgrow(this, Priority.ALWAYS) },
    Label("FB"),
    fbField
  )

  // Unity section - inline S/E
  private val unityLabel = Label("UNITY")
  unityLabel.getStyleClass.addAll("label", "h-head")

  private val unity0Field = JagNumericField(0, 65535, 0)
  unity0Field.setPrefWidth(55)
  unity0Field.valueProperty.addListener((_, _, nv) =>
    currentFilter.foreach(_.unity0.set(nv.intValue))
  )

  private val unity1Field = JagNumericField(0, 65535, 0)
  unity1Field.setPrefWidth(55)
  unity1Field.valueProperty.addListener((_, _, nv) =>
    currentFilter.foreach(_.unity1.set(nv.intValue))
  )

  private val unityRow = HBox(4)
  unityRow.setAlignment(Pos.CENTER_LEFT)
  unityRow.getChildren.addAll(
    Label("S"),
    unity0Field,
    new Region() { HBox.setHgrow(this, Priority.ALWAYS) },
    Label("E"),
    unity1Field
  )

  // Poles info
  private val polesLabel = Label("POLES")
  polesLabel.getStyleClass.addAll("label", "h-head")

  private val polesInfoLabel = Label("0 active")
  polesInfoLabel.getStyleClass.add("label")
  polesInfoLabel.setStyle("-fx-text-fill: #888;")

  private val polesRow = HBox(4)
  polesRow.setAlignment(Pos.CENTER_LEFT)
  polesRow.getChildren.addAll(
    polesLabel,
    new Region() { HBox.setHgrow(this, Priority.ALWAYS) },
    polesInfoLabel
  )

  filterPane.getChildren.addAll(
    pairsLabel,
    pairsRow,
    unityLabel,
    unityRow,
    polesRow
  )

  // === LAYOUT ===
  view.getChildren.addAll(envelopePane, filterPane)
  view.setVisible(false)
  filterPane.setVisible(false)

  def bind(envelope: EnvelopeViewModel): Unit =
    currentMode = InspectorMode.Envelope
    currentEnvelope = Some(envelope)
    currentFilter = None
    view.setVisible(true)
    envelopePane.setVisible(true)
    filterPane.setVisible(false)

    val formStr = envelope.form.get match
      case WaveForm.Square => "Sqr"
      case WaveForm.Sine   => "Sin"
      case WaveForm.Saw    => "Saw"
      case WaveForm.Noise  => "Nse"
      case _               => "Off"

    waveGrid.setSelected(formStr)
    startField.setValue(envelope.start.get)
    endField.setValue(envelope.end.get)
    segCountLabel.setText(s"${envelope.getSegments.length} pts")

  def bindFilter(filter: FilterViewModel): Unit =
    currentMode = InspectorMode.Filter
    currentFilter = Some(filter)
    currentEnvelope = None
    view.setVisible(true)
    envelopePane.setVisible(false)
    filterPane.setVisible(true)

    ffField.setValue(filter.pairCount0.get)
    fbField.setValue(filter.pairCount1.get)
    unity0Field.setValue(filter.unity0.get)
    unity1Field.setValue(filter.unity1.get)

    val totalPoles = filter.pairCount0.get + filter.pairCount1.get
    polesInfoLabel.setText(s"$totalPoles active")

  def hide(): Unit =
    currentEnvelope = None
    currentFilter = None
    view.setVisible(false)
