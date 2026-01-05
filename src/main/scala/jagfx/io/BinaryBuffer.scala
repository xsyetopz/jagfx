package jagfx.io

import jagfx.Constants.Smart
import jagfx.Constants

/** Binary buffer for reading and writing primitive types with explicit
  * endianness. Tracks current position for sequential access.
  *
  * @param data
  *   Underlying byte array
  */
class BinaryBuffer(val data: Array[Byte]):
  private var _pos: Int = 0
  private var _truncated: Boolean = false

  /** Current read/write position (read-only). */
  def pos: Int = _pos

  /** True if read operations have exceeded buffer length (EOF). */
  def isTruncated: Boolean = _truncated

  /** Advances position by `n` bytes without reading. */
  def skip(n: Int): Unit = _pos += n

  /** Creates buffer with specified size. */
  def this(size: Int) = this(new Array[Byte](size))

  /** Returns remaining bytes from current position. */
  def remaining: Int = data.length - _pos

  /** Peeks at byte at current position without advancing. */
  def peek(): Int =
    if _pos >= data.length then 0 else data(_pos) & 0xff

  /** Reads unsigned 8-bit integer, advances position by `1`. */
  def readU8(): Int =
    if _checkTruncation(1) then 0
    else
      val v = data(_pos) & 0xff
      _pos += 1
      v

  /** Reads signed 8-bit integer, advances position by `1`. */
  def readS8(): Int =
    if _checkTruncation(1) then 0
    else
      val v = data(_pos)
      _pos += 1
      v

  /** Reads unsigned 16-bit big-endian integer, advances position by `2`. */
  def readU16BE(): Int =
    if _checkTruncation(2) then 0
    else
      _pos += 2
      ((data(_pos - 2) & 0xff) << 8) + (data(_pos - 1) & 0xff)

  /** Reads unsigned 16-bit little-endian integer, advances position by `2`. */
  def readU16LE(): Int =
    if _checkTruncation(2) then 0
    else
      _pos += 2
      (data(_pos - 2) & 0xff) + ((data(_pos - 1) & 0xff) << 8)

  /** Reads signed 16-bit big-endian integer, advances position by `2`. */
  def readS16BE(): Int =
    if _checkTruncation(2) then 0
    else
      import Constants._
      _pos += 2
      var value = ((data(_pos - 2) & 0xff) << 8) + (data(_pos - 1) & 0xff)
      if value > Short.MaxValue then value -= Int16.Range
      value

  /** Reads signed 32-bit big-endian integer, advances position by `4`. */
  def readS32BE(): Int =
    if _checkTruncation(4) then 0
    else
      _pos += 4
      ((data(_pos - 4) & 0xff) << 24) +
        ((data(_pos - 3) & 0xff) << 16) +
        ((data(_pos - 2) & 0xff) << 8) +
        (data(_pos - 1) & 0xff)

  /** Reads signed variable-length smart integer (`1` or `2` bytes). */
  def readSmart(): Int =
    if remaining == 0 then return 0
    val value = peek()
    if value < Smart.Threshold then readU8() - Smart.SignedOffset
    else readU16BE() - Smart.SignedBaseOffset

  /** Reads unsigned variable-length smart integer (`1` or `2` bytes). */
  def readSmartUnsigned(): Int =
    import Constants._
    if remaining == 0 then return 0
    val value = peek()
    if value < Smart.Threshold then readU8()
    else readU16BE() - Int16.UnsignedMaxValue

  /** Writes signed 32-bit big-endian integer, advances position by `4`. */
  def writeS32BE(value: Int): Unit =
    data(_pos) = (value >> 24).toByte
    data(_pos + 1) = (value >> 16).toByte
    data(_pos + 2) = (value >> 8).toByte
    data(_pos + 3) = value.toByte
    _pos += 4

  /** Writes signed 32-bit little-endian integer, advances position by `4`. */
  def writeS32LE(value: Int): Unit =
    data(_pos) = value.toByte
    data(_pos + 1) = (value >> 8).toByte
    data(_pos + 2) = (value >> 16).toByte
    data(_pos + 3) = (value >> 24).toByte
    _pos += 4

  /** Writes signed 16-bit little-endian integer, advances position by `2`. */
  def writeS16LE(value: Int): Unit =
    data(_pos) = value.toByte
    data(_pos + 1) = (value >> 8).toByte
    _pos += 2

  /** Writes unsigned 8-bit integer, advances position by `1`. */
  def writeU8(value: Int): Unit =
    data(_pos) = value.toByte
    _pos += 1

  /** Writes unsigned 16-bit big-endian integer, advances position by `2`. */
  def writeU16BE(value: Int): Unit =
    data(_pos) = (value >> 8).toByte
    data(_pos + 1) = value.toByte
    _pos += 2

  /** Writes unsigned variable-length smart integer (`1` or `2` bytes). */
  def writeSmartUnsigned(value: Int): Unit =
    if value < Smart.Threshold then writeU8(value)
    else
      writeU8((value >> 8) + Smart.Threshold)
      writeU8(value & 0xff)

  /** Writes signed variable-length smart integer (`1` or `2` bytes). */
  def writeSmart(value: Int): Unit =
    val adjusted = value + Smart.SignedOffset
    if adjusted >= 0 && adjusted < Smart.Threshold then writeU8(adjusted)
    else
      val v = value + Smart.SignedBaseOffset
      writeU8((v >> 8) & 0xff)
      writeU8(v & 0xff)

  private def _checkTruncation(bytes: Int): Boolean =
    if _pos + bytes > data.length then
      _truncated = true
      _pos += bytes
      true
    else false
