/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Term;
import maaj.util.H;

/**
 * represents part of associative collection, that provides retrieval of contents
 * <p>
 * @author maartyl
 */
public interface AssocGet extends Lookup {
  default boolean containsKey(Term key) {
    return valAt(key, H.notFoundNil) != H.notFoundNil;
  }

  //generally weird concept, that turned out to be unnecessary
//  public default KVPair entryAt(Term key) {
//    Term val = valAt(key, H.notFoundNil);
//    //TODO: think through (Nil / Ex / (Key, Nil) / ??)
////    if (val == H.notFoundNil)
////      throw new notfound;
//    //This is wrong: shows notFoundNil - that probably shouldn't be happening...
//    //KVEntry must become Term anyway, to be useable - Nil is probably best
//    return H.buildAssocEntry(key, val == H.notFoundNil ? H.NIL : val);
//  }
}
