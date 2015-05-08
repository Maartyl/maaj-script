/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.wrap;

import com.github.krukow.clj_lang.IEditableCollection;
import com.github.krukow.clj_lang.IPersistentMap;
import com.github.krukow.clj_lang.ITransientMap;
import java.util.Iterator;
import maaj.term.Int;
import maaj.term.Invocable;
import maaj.term.KVPair;
import maaj.term.Map;
import maaj.term.MapT;
import maaj.term.Term;
import maaj.util.MapH;

/**
 *
 * @author maartyl
 */
public final class MapTWrap implements MapT {

  private ITransientMap<Term, Term> map;

  private MapTWrap(ITransientMap<Term, Term> map) {
    this.map = map;
  }

  @Override
  public MapT retM(Term contents) {
    return MapH.emptyTransient().doConj(contents);
  }

  @Override
  public MapT bindM(Invocable fn2Monad) {
    throw new UnsupportedOperationException("Transient maps cennot be iterated over.");
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
    //for now, maybe allow: create persistent, call iterator, replace transient with new .asTransient ....
    throw new UnsupportedOperationException("Transient maps cennot be iterated over.");
  }

  @Override
  public MapT doAssoc(Term key, Term value) {
    map = map.assoc(key, value);
    return this;
  }

  @Override
  public MapT doDissoc(Term key) {
    map = map.without(key);
    return this;
  }

  @Override
  public Map asPersistent() {
    return MapPWrap.of(map.persistentMap());
  }

  @Override
  public String toString() {
    return "#<transient map; size: " + getCountAsInteger() + ">";
  }

//  @Override
//  public boolean equals(Object obj) {
//    return this == obj;
//  }
//
//  @Override
//  public int hashCode() {
//    return -1;
//  }

  @SuppressWarnings("unchecked")
  static MapTWrap of(IPersistentMap<Term, Term> pmap) {
    return new MapTWrap((ITransientMap<Term, Term>) ((IEditableCollection<?>) pmap).asTransient());
  }

  @SuppressWarnings("unchecked")
  public static MapT ofNil(IPersistentMap<Term, Term> pmap) {
    //variant that can handle null
    if (pmap == null)
      return MapH.emptyTransient();
    return of(pmap);
  }

}
