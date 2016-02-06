/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.coll.traits.Functor;
import maaj.lang.Context;

/**
 *
 * @author maartyl
 * @param <M> self
 */
public interface Monad<M extends Monad<M>> extends Term, Functor<M> {

  /**
   * Create monad from contents
   * <p>
   * Method to make use of virtuality. Otherwise caller would have to specify type. (also possible when necessary per Monad type)
   * <p>
   * @param contents
   * @return
   */
  M retM(Term contents);

  M bindM(Invocable fn2Monad);

  @Override
  public default M fmap(Invocable mapper) {
    return bindM((Invocable1) x -> retM(x.transform(mapper)));
  }

  /**
   * join :: (Monad m) => m (m a) -> m a
   * join x = x >>= id
   * @return flattened monad
   */
  default M joinM() {
    return bindM((Invocable1) x -> x);
  }

  @Override
  public default Monad unquoteTraverse(Context c) {
    return retM(bindM((Invocable1) x -> x.unquoteTraverse(c)));
  }
}
