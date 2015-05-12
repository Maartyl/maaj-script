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
import maaj.util.MapH;
import maaj.util.SeqH;

/**
 *
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


}
