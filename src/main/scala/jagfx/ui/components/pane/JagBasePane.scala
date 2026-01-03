package jagfx.ui.components.pane

import javafx.scene.layout._
import javafx.scene.control.Label
import javafx.geometry.Pos

/** Abstract base for pane components with header. */
abstract class JagBasePane(title: String) extends VBox:
  protected val header = new Label(title)
  header.getStyleClass.add("panel-head")
  header.setMaxWidth(Double.MaxValue)
  header.setAlignment(Pos.CENTER)

  getChildren.add(header)

  def dispose(): Unit = {}
