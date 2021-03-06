/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.wrap;

import com.github.krukow.clj_lang.IPersistentMap;
import java.util.Iterator;
import java.util.Map.Entry;
import maaj.term.Int;
import maaj.term.KVPair;
import maaj.term.Map;
import maaj.term.MapT;
import maaj.term.Term;
import maaj.util.H;
import maaj.util.MapH;

/**
 * wraps functionality of ~Clojure IPersistentMap&lt;Term, Term&gt;, making it a term.
 * (Essentially: ArrayMap up to 8 terms, then HashMap)
 * <p>
 * @author maartyl
 */
public final class MapPWrap implements Map {

  private final IPersistentMap<Term, Term> map;

  private MapPWrap(IPersistentMap<Term, Term> map) {
    this.map = map;
  }

  @Override
  public Int getCount() {
    return Int.of(map.count());
  }

  @Override
  public int getCountAsInteger() {
    return (map.count());
  }

  @Override
  public Term valAt(Term key, Term dflt) {
    return map.valAt(key, dflt);
  }

  @Override
  public Iterator<KVPair> iterator() {
    return new Iterator<KVPair>() {
      Iterator<Entry<Term, Term>> it = map.iterator();

      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public KVPair next() {
        return H.buildAssocEntry(it.next());
      }
    };
  }

  @Override
  public Map assoc(Term key, Term value) {
    return wrap(map.assoc(key, value));
  }

  @Override
  public Map dissoc(Term key) {
    return wrap(map.without(key));
  }

  @Override
  public MapT asTransient() {
    return MapTWrap.of(map);
  }

  @Override
  public String toString() {
    return print();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Term)
      obj = ((Term) obj).getContent();
    if (obj instanceof MapPWrap) {
      return map.equiv(((MapPWrap) obj).map);
    }
    //WISH: handle other implementations too
    return false;
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  private static MapPWrap wrap(IPersistentMap<Term, Term> pmap) {
    return new MapPWrap(pmap);
  }
  @SuppressWarnings("unchecked")
  public static MapPWrap of(IPersistentMap<Term, Term> pmap) {
    //null checked in H.wrap: properly returns Nil on null
    return wrap(pmap);
  }

  @SuppressWarnings("unchecked")
  public static Map ofNil(IPersistentMap<Term, Term> pmap) {
    //variant that can handle null
    if (pmap == null)//null-> empty - any other option?
      return MapH.emptyPersistent();
    return new MapPWrap(pmap);
  }

}
