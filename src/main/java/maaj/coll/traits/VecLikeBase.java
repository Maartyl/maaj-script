/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.KVPair;
import maaj.util.H;
import maaj.term.Term;

/**
 *
 * @author maartyl
 * @param <VL> self
 */
public interface VecLikeBase<VL extends VecLikeBase<VL>> extends Indexed, AssocGet, Peekable {

  @Override
  public default Term valAt(Term key) {
    return nth(H.requireInt(key));
  }

  @Override
  public default Term valAt(Term key, Term dflt) {
    return nth(H.requireInt(key), dflt);
  }

  @Override
  public default boolean containsKey(Term key) {
    int k = H.requireInt(key).asInteger();
    return k >= 0 && k < getCountAsInteger();
  }

  @Override
  public default Term peek() {
    int count = getCountAsInteger();
    if (count == 0) return H.NIL;
    return nth(count - 1);
  }


}
