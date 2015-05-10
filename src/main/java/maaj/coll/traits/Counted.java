/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Int;

/**
 *
 * @author maartyl
 */
public interface Counted extends Numerable {
  Int getCount();

  default int getCountAsInteger() {
    return getCount().asInteger();
  }

  @Override
  public default Int count() {
    return getCount();
  }

  public default boolean isEmpty() {
    return getCountAsInteger() == 0;
  }
}
