/*
 * Copyright (c) 2014 maartyl.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    maartyl - initial API and implementation and/or initial documentation
 */
package maaj.reader;

import java.io.IOException;
import java.io.Reader;
import maaj.util.H;

/**
 * Allows to read chars and remembers position in stream, in terms of row, col.
 * <p>
 * @author maartyl
 */
public final class PosReader {
  private final Reader rdr;
  private int row = 1;   //first row is 1
  private int column = 0;//first col is 1 (incremented upon reading first char)
  private int position = -1;  //the position in stream
  private int beforeColumnForUnreadOnPrevLine = -1; // -"- column; ONLY used if curChar is \n : otherwise: just --;

  private char[] buff = new char[buffSize];
  private char[] otherBuff = new char[buffSize];

  private int buffLen = 0;
  private int otherBuffLen = 0;

  private int pos = 0; // position in buff; invariant: 0 <= pos < buffLen  ||  afterLastChar

  private boolean isLastBuff = true; //is `buff` the buffer last read into?

  private boolean afterLastChar = false; // true iff position >= streamLength; also afterLast => isLastBuff

  public PosReader(Reader rdr) {
    this.rdr = rdr;
  }

  public int getRow() {
    return row;
  }

  public int getColumn() {
    return column;
  }

  /**
   * @return nth. in reader stream, indexed from 0
   */
  public int getPosition() {
    return position;
  }

  private int read(int offset) {
    try {
      return rdr.read(buff, offset, buffSize - offset);
    } catch (IOException ex) {
      throw H.sneakyThrow(ex);
    }
  }

  private void readAndUpdate() {
    int cnt = read(buffLen);
    pos = buffLen; //move to first char of newly read chunk || after last if EOS
    if (cnt > 0) 
      buffLen += cnt;
    else // EOS //cannot return 0
      afterLastChar = true;
  }

  private void inc() {
    if (++pos < buffLen) { //fast path
      //increment only: done
    } else if (isLastBuff) {
      if (afterLastChar) { //don't care; but keep track of how far I am
        //increment only: done
      } else if ((buffSize - buffLen) > buffMinSpaceForRead) { //still room in buff?
        readAndUpdate();
      } else {
        swap(); //cannot read more to this buffer: reuse other one
        buffLen = 0; //discard buffer
        readAndUpdate();
      }

    } else { // not last buff: have more read in other buff
      swap();
      pos = 0;
      isLastBuff = true;
    }
    incRC(cur());
  }

  private void dec() {
    decRC(cur());
    if (pos > 0) { //fast path
      pos--;
    } else if (isLastBuff) { //try previous buffer
      assert pos == 0;

      swap();
      pos = buffLen - 1; //move to last position in previous buffer
      isLastBuff = false; //current buffer is no longer lastRead

      if (buffLen == 0) //other buffer is empty
        throw new UnsupportedOperationException("cannot unread: reached start of stream");
    } else {
      assert pos == 0;

      if (position < 0)
        throw new UnsupportedOperationException("cannot unread: reached start of stream");
      throw new UnsupportedOperationException("cannot unread: too long (" + (buffLen + otherBuffLen) + ")");
    }
  }

  private int cur() {
    if (afterLastChar) return -1;
    return buff[pos];
  }

  //swap buffers
  // !!! invalidates pos !!!
  private void swap() {
    char[] b1 = buff;
    char[] b2 = otherBuff;
    int len1 = buffLen;
    int len2 = otherBuffLen;

    buff = b2;
    otherBuff = b1;
    buffLen = len2;
    otherBuffLen = len1;
  }

  private void incRC(int curChar) {
    position++;
    if (curChar == '\n') {
      row++;
      beforeColumnForUnreadOnPrevLine = column;
      column = 0;
    } else column++;
  }

  private void decRC(int curChar) {
    position--;
    //THESE will NOT WORK if goes over 2 lines
    if (curChar == '\n') {
      row--;
      column = beforeColumnForUnreadOnPrevLine;
    } else column--;
  }


  /**
   * 
   * @return the character read 1 before getCurrentChar() (or 0, if not yet set)
   */
  public int getLastChar() {
    if (pos > 0 && pos <= buffLen)
      return buff[pos - 1];

    dec();
    int c = cur();
    inc();
    return c;
  }

  /**
   * 
   * @return the most recently read character (or 0, if not yet set)
   */
  public int getCurrentChar() {
    return cur();
  }

  /**
   * //@throws IOException
   * @return read char in advanced position in underlying Reader
   */
  public int readNextChar() {
    inc();
    return cur();
  }

  /**
   * Works like PushbackReader.unread(c), where c == curChar; Can only be used once
   */
  public void unread() {
    dec();
  }

  /**
   * Returns the next character.
   * <p>
   * @return the next character.
   */
  public int peek() {
    if (pos < buffLen - 1)
      return buff[pos + 1];

    inc();
    int c = cur();
    dec();
    return c;
  }

  public void close() {
    try {
      rdr.close();
    } catch (IOException ex) {
      throw H.sneakyThrow(ex);
    }
  }

  private static final int buffSize = 2048;
  private static final int buffMinSpaceForRead = 256;

}
