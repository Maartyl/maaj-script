/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Term;

/**
 * Represents a collection that can return something containing all of it + t
 * <p>
 * @author maartyl
 * @param <G> the type of collection implementing this interface
 */
public interface Growable<G extends Growable<G>> {

  /**
   * Conj[oin] element to collection
   * <p>
   * @param t term to conjoin
   * @return new collection that on top of original elements includes t
   */
  G conj(Term t);
}
