package jagfx.ui.controller.footer

import javafx.scene.layout._
import javafx.scene.control.Label
import javafx.geometry.Pos
import jagfx.ui.viewmodel.SynthViewModel
import jagfx.ui.components.slider._
import jagfx.ui.BindingManager

private val _PanelSize = 120
private val _SliderSize = 100

/** Echo controls panel. */
object EchoPanel:
  def create(viewModel: SynthViewModel, bindings: BindingManager): VBox =
    val panel = VBox()
    panel.getStyleClass.add("panel")
    panel.setMinWidth(_PanelSize)
    panel.setPrefWidth(_PanelSize)
    panel.setMaxWidth(_PanelSize)
    HBox.setHgrow(panel, Priority.NEVER)
    val head = Label("ECHO")
    head.getStyleClass.add("panel-head")
    head.setMaxWidth(Double.MaxValue)
    head.setAlignment(Pos.CENTER)
    head.setMaxWidth(Double.MaxValue)

    val mix = JagBarSlider(0, _SliderSize, 0, "MIX:")
    val delay = JagBarSlider(0, _SliderSize, 0, "DEL:")

    viewModel.activeToneIndexProperty.addListener((_, _, _) =>
      bindings.unbindAll()
      val tone = viewModel.getActiveTone
      bindings.bindBidirectional(mix.valueProperty, tone.echoMix)
      bindings.bindBidirectional(delay.valueProperty, tone.echoDelay)
    )

    panel.getChildren.addAll(head, mix, delay)
    panel
