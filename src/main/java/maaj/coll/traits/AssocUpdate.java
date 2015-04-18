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
 * @param <A> collection implementing updatable assoc
 */
public interface AssocUpdate<A extends AssocUpdate<A>> {
  A assoc(Term key, Term value);

  default A assoc(KVEntry entry) {
    return assoc(entry.getKey(), entry.getValue());
  }

}
