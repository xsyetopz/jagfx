package jagfx.ui.components.slider

import javafx.scene.layout.VBox
import javafx.beans.property._

/** Abstract base for sliders. */
abstract class JagBaseSlider(min: Int, max: Int, initial: Int) extends VBox:
  protected val value = SimpleIntegerProperty(initial)

  def valueProperty: IntegerProperty = value
  def getValue: Int = value.get
  def setValue(v: Int): Unit = value.set(clamp(v))

  protected def clamp(v: Int): Int = math.max(min, math.min(max, v))

  def dispose(): Unit = {}
