package jagfx.utils

import java.util.Arrays

object DrawingUtils:
  /** Draws line into raw ARGB pixel buffer using Bresenham's algorithm. */
  def line(
      buffer: Array[Int],
      w: Int,
      h: Int,
      x0: Int,
      y0: Int,
      x1: Int,
      y1: Int,
      color: Int
  ): Unit =
    var (x, y) = (x0, y0)
    val dx = math.abs(x1 - x0)
    val dy = math.abs(y1 - y0)
    val sx = if x0 < x1 then 1 else -1
    val sy = if y0 < y1 then 1 else -1
    var err = dx - dy

    while x != x1 || y != y1 do
      if x >= 0 && x < w && y >= 0 && y < h then buffer(y * w + x) = color

      val e2 = 2 * err
      if e2 > -dy then
        err -= dy
        x += sx
      if e2 < dx then
        err += dx
        y += sy

    if x >= 0 && x < w && y >= 0 && y < h then buffer(y * w + x) = color

  def fillRect(
      buffer: Array[Int],
      w: Int,
      h: Int,
      rx: Int,
      ry: Int,
      rw: Int,
      rh: Int,
      color: Int
  ): Unit =
    val updateMinX = math.max(0, rx)
    val updateMinY = math.max(0, ry)
    val updateMaxX = math.min(w, rx + rw)
    val updateMaxY = math.min(h, ry + rh)
    if updateMinX < updateMaxX && updateMinY < updateMaxY then
      for y <- updateMinY until updateMaxY do
        var rowOffset = y * w
        for x <- updateMinX until updateMaxX do buffer(rowOffset + x) = color

  def clear(buffer: Array[Int], color: Int): Unit =
    Arrays.fill(buffer, color)

  inline def setPixel(
      buffer: Array[Int],
      w: Int,
      h: Int,
      x: Int,
      y: Int,
      color: Int
  ): Unit =
    if x >= 0 && x < w && y >= 0 && y < h then buffer(y * w + x) = color

  def fillCircle(
      buffer: Array[Int],
      w: Int,
      h: Int,
      cx: Int,
      cy: Int,
      r: Int,
      color: Int
  ): Unit =
    for dy <- -r to r do
      for dx <- -r to r do
        if dx * dx + dy * dy <= r * r then
          setPixel(buffer, w, h, cx + dx, cy + dy, color)

  def drawCircle(
      buffer: Array[Int],
      w: Int,
      h: Int,
      cx: Int,
      cy: Int,
      r: Int,
      color: Int
  ): Unit =
    var x = r
    var y = 0
    var err = 0
    while x >= y do
      setPixel(buffer, w, h, cx + x, cy + y, color)
      setPixel(buffer, w, h, cx + y, cy + x, color)
      setPixel(buffer, w, h, cx - y, cy + x, color)
      setPixel(buffer, w, h, cx - x, cy + y, color)
      setPixel(buffer, w, h, cx - x, cy - y, color)
      setPixel(buffer, w, h, cx - y, cy - x, color)
      setPixel(buffer, w, h, cx + y, cy - x, color)
      setPixel(buffer, w, h, cx + x, cy - y, color)
      y += 1
      err += 1 + 2 * y
      if 2 * (err - x) + 1 > 0 then
        x -= 1
        err += 1 - 2 * x
