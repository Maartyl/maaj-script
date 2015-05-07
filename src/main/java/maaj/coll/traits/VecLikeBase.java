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
public interface VecLikeBase<VL extends VecLikeBase<VL>> extends Indexed, AssocGet {

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
  public default KVPair entryAt(Term key) {
    Term val = valAt(key, H.notFoundNil);
    //TODO: think through (Nil / Ex / (Key, Nil) / ??)
//    if (val == H.notFoundNil)
//      return H.NIL;
    //This is wrong: shows notFoundNil - that probably shouldn't be happening...
    //KVEntry must become Term anyway, to be useable - Nil is probably best
    return H.buildAssocEntry(key, val);
  }

}
