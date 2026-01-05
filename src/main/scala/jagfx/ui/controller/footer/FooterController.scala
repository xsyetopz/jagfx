package jagfx.ui.controller.footer

import javafx.scene.layout._
import jagfx.ui.viewmodel.SynthViewModel
import jagfx.ui.controller.ControllerLike
import jagfx.ui.BindingManager
import javafx.scene.control.Label

/** Footer controller containing tones, partials, echo, and mode panels. */
class FooterController(viewModel: SynthViewModel) extends ControllerLike[VBox]:
  private val _content = HBox()
  _content.getStyleClass.add("footer")

  private val _echoBindings = BindingManager()

  private val _tonesPanel = TonesPanel.create(viewModel)
  private val _partialsPanel = PartialsPanel.create(viewModel)
  private val _echoPanel = EchoPanel.create(viewModel, _echoBindings)

  _content.getChildren.addAll(_tonesPanel, _partialsPanel, _echoPanel)
  HBox.setHgrow(_partialsPanel, Priority.ALWAYS)

  protected val view = VBox(_content)
