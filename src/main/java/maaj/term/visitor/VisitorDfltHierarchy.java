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
public interface VisitorDfltHierarchy<TR, TA> extends VisitorRecursive<TR, TA> {

  //default value returned from all
  TR dflt(Term t, TA arg);

  @Override
  public default TR id(Term t, TA arg) {
    return dflt(t, arg);
  }

  @Override
  public default TR monad(Monad t, TA arg) {
    return dflt(t, arg);
  }


}
