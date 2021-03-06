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
}
