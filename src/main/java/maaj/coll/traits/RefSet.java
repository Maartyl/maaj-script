/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Invocable;
import maaj.term.Term;

/**
 *
 * @author maartyl
 */
public interface RefSet extends Ref {
  public RefSet doSet(Term t);

  @Override
  public default Term update(Invocable setter) {
    Term s = deref().transform(setter);
    doSet(s);
    return s;
  }

}
