/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Invocable;
import maaj.term.Term;

/**
 *
 * @author maartyl
 */
public interface Ref extends Deref {

  /**
   * @param setter ::(OldState -> NewState)
   * @return new state
   */
  Term update(Invocable setter);
}
