/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Term;

/**
 * Transient variant of modifying operations on Assoc
 * <p>
 * @author maartyl
 * @param <A> collection implementing updatable assoc
 */
public interface AssocUpdateT<A extends AssocUpdateT<A>> {
  A doAssoc(Term key, Term value);

  default A doAssoc(KVEntry entry) {
    return doAssoc(entry.getKey(), entry.getValue());
  }
}
