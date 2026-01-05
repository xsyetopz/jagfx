package jagfx.ui.controller.inspector

import javafx.scene.layout._
import javafx.scene.control._
import javafx.geometry.Pos
import jagfx.ui.viewmodel.EnvelopeViewModel
import jagfx.ui.components.field._
import jagfx.ui.components.group._
import jagfx.ui.components.slider._
import jagfx.ui.components.pane._
import jagfx.model.Waveform

private val _RangeFieldSize = 48

/** Inspector panel for envelope parameters. */
class EnvelopeInspector extends VBox:
  private var _currentEnvelope: Option[EnvelopeViewModel] = None

  setSpacing(8)

  // Wave section
  private val _waveLabel = Label("WAVE")
  _waveLabel.getStyleClass.addAll("label", "h-head")

  private val _waveGrid = JagToggleGroup(
    ("Off", "mdi2v-volume-off"),
    ("Sqr", "mdi2s-square-wave"),
    ("Sin", "mdi2s-sine-wave"),
    ("Saw", "mdi2s-sawtooth-wave"),
    ("Nse", "mdi2w-waveform")
  )
  _waveGrid.setAlignment(Pos.CENTER)

  _waveGrid.selectedProperty.addListener((_, _, newVal) =>
    _currentEnvelope.foreach { env =>
      val waveform = newVal match
        case "Off" => Waveform.Off
        case "Sqr" => Waveform.Square
        case "Sin" => Waveform.Sine
        case "Saw" => Waveform.Saw
        case "Nse" => Waveform.Noise
        case _     => Waveform.Off
      env.waveform.set(waveform)
    }
  )

  // Range section
  private val _rangeLabel = Label("RANGE")
  _rangeLabel.getStyleClass.addAll("label", "h-head")

  private val _maxRangeValue = 999999

  private val _startField = JagNumericField(-_maxRangeValue, _maxRangeValue, 0)
  _startField.setPrefWidth(_RangeFieldSize)
  _startField.setTooltip(new Tooltip("Envelope start value"))
  _startField.valueProperty.addListener((_, _, nv) =>
    _currentEnvelope.foreach(_.start.set(nv.intValue))
  )

  private val _endField = JagNumericField(-_maxRangeValue, _maxRangeValue, 0)
  _endField.setPrefWidth(_RangeFieldSize)
  _endField.setTooltip(new Tooltip("Envelope end value"))
  _endField.valueProperty.addListener((_, _, nv) =>
    _currentEnvelope.foreach(_.end.set(nv.intValue))
  )

  private val _rangeRow = HBox(4)
  _rangeRow.setAlignment(Pos.CENTER_LEFT)
  _rangeRow.getChildren.addAll(
    Label("S:"),
    _startField,
    new Region() { HBox.setHgrow(this, Priority.ALWAYS) },
    Label("E:"),
    _endField
  )

  // Segments section
  private val _segLabel = Label("SEGMENTS")
  _segLabel.getStyleClass.addAll("label", "h-head")

  private val _segmentEditor = EnvelopeSegmentEditor()

  getChildren.addAll(
    _waveLabel,
    _waveGrid,
    _rangeLabel,
    _rangeRow,
    _segLabel,
    _segmentEditor
  )

  /** Bind to envelope view model. */
  def bind(envelope: EnvelopeViewModel): Unit =
    _currentEnvelope = Some(envelope)

    val formStr = envelope.waveform.get match
      case Waveform.Square => "Sqr"
      case Waveform.Sine   => "Sin"
      case Waveform.Saw    => "Saw"
      case Waveform.Noise  => "Nse"
      case _               => "Off"

    _waveGrid.setSelected(formStr)
    _startField.setValue(envelope.start.get)
    _endField.setValue(envelope.end.get)
    _segmentEditor.bind(envelope)
