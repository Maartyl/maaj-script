/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import java.util.function.BiFunction;
import maaj.coll.traits.Indexed;
import maaj.coll.traits.KVEntry;
import maaj.term.Term;

/**
 *
 * @author maartyl
 */
public class MapH {

  private MapH() {
  }

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

}
