package jagfx.ui.components.group

import javafx.scene.layout.HBox
import javafx.beans.property._

/** Abstract base for toggle groups. */
abstract class JagBaseGroup(initialId: String) extends HBox:
  protected val selected = SimpleStringProperty(initialId)

  def selectedProperty: StringProperty = selected
  def getSelected: String = selected.get
  def setSelected(value: String): Unit = selected.set(value)

  def dispose(): Unit = {}
