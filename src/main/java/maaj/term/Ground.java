/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;
import maaj.util.H;

/**
 *
 * @author maartyl
 */
public interface Ground extends Term {

  @Override
  default Term eval(Context c) {
    return this;
  }

  @Override
  default Term evalMacros(Context c) {
    return this;
  }

  @Override
  public default Collection unquoteTraverse(Context c) {
    return H.tuple(this);
  }


}
