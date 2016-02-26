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
import maaj.coll.traits.Seqable;
import maaj.coll.wrap.MapPWrap;
import maaj.term.Map;
import maaj.term.MapT;
import maaj.term.Seq;
import maaj.term.Term;

/**
 * Map helpers - operations on maps
 * <p>
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
    if (t instanceof Seqable) {
      Seq s = ((Seqable) t).seq();
      if (s.boundLength(2) == 2)
        return assoc.apply(s.first(), s.rest().first());
    }
    throw new IllegalArgumentException("conj: Cannot coerce arg: " + t.getClass().getName()
                                       + " into key-value pair. //: " + o.print());
  }

  public static MapT update(MapT what, Iterable<? extends KVEntry> with) {
    for (KVEntry e : with)
      what.doAssoc(e);
    return what;
  }

  public static Map update(Map what, Iterable<? extends KVEntry> with) {
    return update(what.asTransient(), with).asPersistent();
  }

  public static Map update(Map what, Map with) {
    if (what.getCountAsInteger() == 0)
      return with;
    switch (with.getCountAsInteger()) {
    case 0: return what;
    case 1:
    case 2:
    case 3:
    case 4:
      for (KVEntry e : with)
        what = what.assoc(e);
      return what;
    default:
      return update(what.asTransient(), with).asPersistent();
    }
  }

  public static Map keepOnly(Map m, Seq keys) {
    //TODO: faster algorith if large m and few removals
    MapT mt = emptyTransient();
    for (Term key : H.ret1(keys, keys = null)) {
      Term v = m.valAt(key, H.notFoundNil);
      if (v != H.notFoundNil)
        mt = mt.doAssoc(key, v);
    }
    return mt.asPersistent();
  }

  public static boolean hasTag(Map m, Term tag) {
    return !m.valAt(tag).isNil();
  }

  public static Map emptyPersistent() {
    return EMPTY;
  }

  public static MapT emptyTransient() {
    return emptyPersistent().asTransient();
  }

}
