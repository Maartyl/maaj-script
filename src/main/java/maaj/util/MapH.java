/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import com.github.krukow.clj_lang.PersistentArrayMap;
import java.util.function.BiFunction;
import maaj.coll.traits.Indexed;
import maaj.coll.traits.KVEntry;
import maaj.coll.wrap.MapPWrap;
import maaj.term.Map;
import maaj.term.MapT;
import maaj.term.Term;

/**
 *
 * @author maartyl
 */
public class MapH {

  private MapH() {
  }
  @SuppressWarnings("unchecked")
  private static final Map EMPTY = MapPWrap.of(PersistentArrayMap.EMPTY);


  public static <M> M coerceConj(Term o, BiFunction<Term, Term, M> assoc) {
    //TODO: possibly add second assoc for only KVEnrty; shouldn't be ever needed...
    Object t = o.getContent();
    if (t instanceof Indexed) {
      Indexed ti = (Indexed) t;
      if (2 == ti.getCountAsInteger())
        return assoc.apply(ti.nth(0), ti.nth(1));
    }
    if (t instanceof KVEntry)
      return assoc.apply(((KVEntry) t).getKey(), ((KVEntry) t).getValue());
    throw new IllegalArgumentException("doConj: Cannot coerce " + "arg" + "(" + t.getClass().getName() + ") into key-value pair.");
  }

  public static MapT update(MapT what, Iterable<? extends KVEntry> with) {
    for (KVEntry e : with)
      what.doAssoc(e);
    return what;
  }

  public static Map update(Map what, Iterable<? extends KVEntry> with) {
    //TODO: create something optimized for small 'with' (I don't know size of iterable...)
    return update(what.asTransient(), with).asPersistent();
  }

  public static Map emptyPersistent() {
    return EMPTY;
  }

  public static MapT emptyTransient() {
    return emptyPersistent().asTransient();
  }

}
