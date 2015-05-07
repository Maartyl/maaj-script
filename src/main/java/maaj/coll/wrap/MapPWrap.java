/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.wrap;

import java.util.Iterator;
import maaj.term.Int;
import maaj.term.Invocable;
import maaj.term.KVPair;
import maaj.term.Map;
import maaj.term.MapT;
import maaj.term.Seq;
import maaj.term.Term;

/**
 *
 * @author maartyl
 */
public class MapPWrap implements Map {

  @Override
  public Map retM(Term contents) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Map bindM(Invocable fn2Monad) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Map fmap(Invocable mapper) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Int getCount() {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Map conj(Term t) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Seq seq() {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Term valAt(Term key, Term dflt) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Iterator<KVPair> iterator() {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Map assoc(Term key, Term value) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Map dissoc(Term key) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public MapT asTransient() {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

}
