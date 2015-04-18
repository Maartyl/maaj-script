/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Invocable;

/**
 *
 * @author maartyl
 * @param <F> self
 */
public interface Functor<F extends Functor<F>> {

  /**
   * @param mapper Function to generate each new state from old.
   * @return
   */
  F fmap(Invocable mapper);

  //breaks uptypeing along the inheritance hierarchy.
//  /**
//   * This is just a helper function that calls normal fmap.
//   * The type helps building correct lambdas.
//   * <p>
//   * @param mapper Function to generate each new state from old.
//   * @return
//   */
//  default F fmap(Invocable1 mapper) {
//    return fmap((Invocable) mapper);
//  }

  /**
   * By default: just ignore result of fmap
   * <p>
   * lazy variants have to redefine this as eager, otherwise it does nothing
   * <p>
   * @param mapper applied to each element
   * @return this
   */
  default Functor<F> foreach(Invocable mapper) {
    fmap(mapper);
    return this;
  }

}
