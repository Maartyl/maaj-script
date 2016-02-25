/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term.visitor;

import maaj.term.Monad;
import maaj.term.Term;

/**
 *
 * @author maartyl
 * @param <TR>
 * @param <TA>
 */
public interface VisitorDfltRec<TR, TA> extends VisitorRecursive<TR, TA> {

  //default value returned from all
  TR dflt();

  @Override
  public default TR id(Term t, TA arg) {
    return dflt();
  }

  @Override
  public default TR monad(Monad t, TA arg) {
    return dflt();
  }


}
