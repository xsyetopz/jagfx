package jagfx.ui.components.field

import javafx.scene.control.TextField
import javafx.beans.property._

/** Abstract base for numeric fields. */
abstract class JagBaseField(initial: Int) extends TextField:
  protected val value = SimpleIntegerProperty(initial)

  def valueProperty: IntegerProperty = value
  def getValue: Int = value.get
  def setValue(v: Int): Unit = value.set(v)

  def dispose(): Unit = {}
