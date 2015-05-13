/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.exceptions;

import maaj.reader.ReaderContext;

/**
 *
 * @author maartyl
 */
public class ReaderException extends UnsupportedOperationException {
  private final ReaderContext context;
  private final int row;
  private final int col;

  public ReaderException(ReaderContext context, int row, int col, String message) {
    super(composeMessage(row, col, context, message));
    this.context = context;
    this.row = row;
    this.col = col;
  }

  public ReaderException(ReaderContext context, int row, int col, String message, Throwable cause) {
    super(composeMessage(row, col, context, message), cause);
    this.context = context;
    this.row = row;
    this.col = col;
  }

  public ReaderException(ReaderContext context, int row, int col, Throwable cause) {
    super(cause);
    this.context = context;
    this.row = row;
    this.col = col;
  }

  public ReaderContext getContext() {
    return context;
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  private static String composeMessage(int row, int col, ReaderContext cxt, String msg) {
    return "[" + cxt.getFileName() + ":" + row + "," + col + "] " + msg;
  }

}
