/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Invocable;
import maaj.term.Term;

/**
 * linear reducer
 * <p>
 * @author maartyl
 */
public interface Reducible {

  /**
   * Reduces target into single value. (foldl)
   * <p>
   * This overload assumes that reducer.invoke() returns start value
   * <p>
   * @param reducer (Acc, Cur)->Acc
   * @return reduced Term
   */
  default Term reduce(Invocable reducer) {
    return reduce(reducer.invoke(), reducer);
  }

  /**
   * Reduces target into single value. (foldl)
   * <p>
   * This overload assumes that reducer.invoke() returns start value
   * <p>
   * @param acc     start Acc
   * @param reducer (Acc, Cur)->Acc
   * @return reduced Terms in Acc
   */
  Term reduce(Term acc, Invocable reducer);
}
