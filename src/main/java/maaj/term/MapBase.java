/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import com.github.krukow.clj_lang.ArityException;
import maaj.coll.traits.MapLikeBase;
import maaj.lang.Context;

/**
 *
 * @author maartyl
 * @param <M> self
 */
public interface MapBase<M extends MapBase<M>> extends CollectionBase<M>, MapLikeBase<M> {

  @Override
  public default Term apply(Context cxt, Seq args) {
    int count = args.boundLength(20);
    if (count == 1)
      return valAt(args.first().eval(cxt));
    if (count == 2)
      return valAt(args.first().eval(cxt), args.rest().first().eval(cxt));
    throw new ArityException(count, "Map - expects: (key) or (key default).");
  }

  @Override
  public default M fmap(Invocable mapper) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

}
