/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.coll.traits.Functor;
import maaj.coll.traits.MapLike;
import maaj.lang.Context;
import maaj.term.visitor.Visitor;
import maaj.util.H;
import maaj.util.MapH;
import maaj.util.SeqH;

/**
 * main interface for associative map data structure used throughout Maaj
 * - immutable
 * <p>
 * @author maartyl
 */
public interface Map extends Collection<Map>, MapBase<Map>, MapLike<Map, MapT> {

  @Override
  public default Map asPersistent() {
    return this;
  }

  @Override
  public default Map retM(Term contents) {
    return MapH.emptyPersistent().conj(contents);
  }

  @Override
  public default Map bindM(Invocable fn2Monad) {
    MapT m = MapH.emptyTransient();
    for (KVPair p : this)
      ((Functor<?>) p.transform(fn2Monad)).foreach((Invocable1) x -> m.doConj(x));
    return m.asPersistent();
  }

  @Override
  public default Map fmap(Invocable mapper) {
    MapT m = MapH.emptyTransient();
    for (KVPair p : this)
      m.doConj(p.transform(mapper));
    return m.asPersistent();
  }

  @Override
  public default Seq seq() {
    return SeqH.iterable2seq(this);
  }

  /**
   * Cannot unquote-splicing map : just map over all keys and values separately
   * return in Tuple1
   * uses [? .seq() ?].firstOrNil() on recursive unquoteTraverse
   * <p>
   * @param c
   * @return
   */
  @Override
  public default Collection unquoteTraverse(Context c) {
    MapT m = MapH.emptyTransient();
    for (KVPair p : this) {
      Term k = H.seqFrom(p.getKey().unquoteTraverse(c)).firstOrNil();
      Term v = H.seqFrom(p.getValue().unquoteTraverse(c)).firstOrNil();
      m.doAssoc(k, v);
    }
    return H.tuple(m.asPersistent());
  }


  @Override
  public default void show(Writer w) throws IOException {
    w.append("{");
    boolean first = true;
    for (KVPair t : this) {
      if (first) {
        first = false;
      } else {
        w.append(", ");
      }
      t.getKey().show(w);
      w.append(" ");
      t.getValue().show(w);
    }
    w.append("}");
  }

  @Override
  public default <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.map(this, arg);
  }

}
