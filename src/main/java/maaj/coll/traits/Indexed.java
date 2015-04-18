/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.util.H;
import maaj.term.Int;
import maaj.term.Term;

/**
 *
 * @author maartyl
 */
public interface Indexed extends Counted {

  Term nth(int i, Term dflt);

  default Term nth(Int i, Term dflt) {
    return nth(i.asInteger(), dflt);
  }

  default Term nth(int i) {
    return nth(i, H.NIL);
  }

  default Term nth(Int i) {
    return nth(i.asInteger());
  }
}
