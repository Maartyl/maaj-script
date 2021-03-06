/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;
import maaj.term.visitor.Visitor;
import maaj.util.H;

/**
 * essentially simple terms that evaluate to themselves
 * <p>
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
  public default Monad unquoteTraverse(Context c) {
    return H.tuple(this);
  }

  @Override
  public default <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.ground(this, arg);
  }


}
