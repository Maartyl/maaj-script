/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Seq;

/**
 * Able to present contained elements in a seq. (Presumably lazy)
 *
 * @author maartyl
 */
public interface Seqable {

  /**
   * linear representation of collection
   * <p>
   * @return sequence of elements
   */
  Seq seq();
}
