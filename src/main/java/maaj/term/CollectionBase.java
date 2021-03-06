/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.coll.traits.Counted;
import maaj.lang.Context;

/**
 * Base for persistent Collections of Terms.
 * <p>
 * @author maartyl
 * @param <C> self : final type of collection
 */
public interface CollectionBase<C extends CollectionBase<C>> extends Monad<C>, Counted {

  @Override
  public default Term eval(Context c) {
    return fmap((Invocable1) e -> e.eval(c));
  }

  @Override
  public default Term evalMacros(Context c) {
    return fmap((Invocable1) e -> e.evalMacros(c));
  }

}
