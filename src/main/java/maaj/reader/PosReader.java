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
import java.io.PushbackReader;
import java.io.Reader;
import maaj.util.H;

/**
 * Allows to read chars and remembers position in steam, in terms of row, col.
 * Assumes buffered reader.
 * <p>
 * @author maartyl
 */
public final class PosReader {
  private final PushbackReader rdr;
  private int row = 1;   //first row is 1
  private int column = 0;//first col is 1 (incremented upon reading first char)
  private int pos = -1;  //the position in stream
  private int lastChar = 0;
  private int curChar = 0;

  private boolean canUnread = false;
  private int beforeLastForUnread = -1; //this will be assigned to lastChar, upon unread
  private int beforeColumnForUnreadOnPrevLine = -1; // -"- column; ONLY used if curChar is \n : otherwise: just --;

  public PosReader(Reader rdr) {
    this.rdr = rdr instanceof PushbackReader ? (PushbackReader) rdr : new PushbackReader(rdr);
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
    return pos;
  }


  /**
   * 
   * @return the character read 1 before getCurrentChar() (or 0, if not yet set)
   */
  public int getLastChar() {
    return lastChar;
  }

  /**
   * 
   * @return the most recently read character (or 0, if not yet set)
   */
  public int getCurrentChar() {
    return curChar;
  }

  /**
   * //@throws IOException
   * @return read char in advanced position in underlying Reader
   */
  public int readNextChar() {
    try {
      beforeLastForUnread = lastChar;
      lastChar = curChar;
      curChar = rdr.read();
      canUnread = true;
      pos++;
      while (curChar == '\r') curChar = rdr.read(); //ignore \r: it's useless anyway (sorry, users of old Macs...)

      if (curChar < 0) return curChar; //EOF: ignore (... EOS I guess)

      if (curChar == '\n') {
        row++;
        beforeColumnForUnreadOnPrevLine = column;
        column = 0;
      } else column++;

      return curChar;
    } catch (IOException e) {
      throw H.sneakyThrow(e); //just rethrow
    }
  }

  /**
   * Works like PushbackReader.unread(c), where c == curChar; Can only be used once
   */
  public void unread() {
    if (!canUnread)
      throw new UnsupportedOperationException("canot unread: can only be done once before next .read()");
    try {
      if (curChar == '\n') {
        row--;
        column = beforeColumnForUnreadOnPrevLine;
      } else column--;

      rdr.unread(curChar);
      curChar = lastChar;
      lastChar = beforeLastForUnread;

    } catch (IOException e) {
      throw H.sneakyThrow(e); //just rethrow
    }
  }

  /**
   * Returns the next character. Possibly \r. (unlike .readNextChar())
   */
  public int peek() {
    try {
      int c = rdr.read();
      rdr.unread(c);
      return c;
    } catch (Exception e) {
      throw H.sneakyThrow(e); //just rethrow
    }

  }

}
