/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Term;

/**
 * Represents a collection that can return something containing all of it + t
 * Transient version
 * <p>
 * @author maartyl
 * @param <G> the type of collection implementing this interface
 */
public interface GrowableT<G extends GrowableT<G>> {

  /**
   * Conj[oin] element to collection
   * <p>
   * @param t term to conjoin
   * @return this, that on top of original elements includes t
   */
  G doConj(Term t);
}
