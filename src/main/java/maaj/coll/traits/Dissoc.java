/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Term;

/**
 *
 * @author maartyl
 * @param <D> self
 */
public interface Dissoc<D extends Dissoc<D>> {

  /**
   * Dissociate.
   * Returns new self without something with given key.
   * (For associative collections)
   * <p>
   * @param key
   * @return self without (key->_)
   */
  D dissoc(Term key);
}
