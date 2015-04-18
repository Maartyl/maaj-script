/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.exceptions;

/**
 *
 * @author maartyl
 */
public class IndexOutOfBoundsExceptionInfo extends IndexOutOfBoundsException {

  private final int size;
  private final int index;

  public IndexOutOfBoundsExceptionInfo(int size, int index) {
    this.size = size;
    this.index = index;
  }

  public int getSize() {
    return size;
  }

  public int getIndex() {
    return index;
  }

  @Override
  public String getMessage() {
    return "Size: " + getSize() + ", Index: " + getIndex();
  }

}
