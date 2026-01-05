package jagfx.ui.viewmodel

import javafx.beans.property._
import jagfx.model.Partial

/** `ViewModelLike` for `Partial` data. */
class PartialViewModel extends ViewModelLike:
  val active = new SimpleBooleanProperty(false)
  val pitchOffset = new SimpleIntegerProperty(0)
  val volume = new SimpleIntegerProperty(0)
  val startDelay = new SimpleIntegerProperty(0)

  def load(h: Partial): Unit =
    active.set(true)
    pitchOffset.set(h.pitchOffset)
    volume.set(h.volume)
    startDelay.set(h.startDelay)
    notifyListeners()

  def clear(): Unit =
    active.set(false)
    pitchOffset.set(0)
    volume.set(0)
    startDelay.set(0)
    notifyListeners()

  def toModel(): Partial =
    Partial(volume.get, pitchOffset.get, startDelay.get)

  override protected def registerPropertyListeners(cb: () => Unit): Unit =
    active.addListener((_, _, _) => cb())
    pitchOffset.addListener((_, _, _) => cb())
    volume.addListener((_, _, _) => cb())
    startDelay.addListener((_, _, _) => cb())
