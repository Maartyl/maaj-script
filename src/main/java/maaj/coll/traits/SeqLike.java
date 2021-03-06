/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import java.util.Iterator;
import maaj.term.Invocable;
import maaj.term.Seq;
import maaj.term.Term;
import maaj.util.H;

/**
 *
 * @author maartyl
 * @param <S> self
 */
public interface SeqLike<S extends SeqLike<S>> extends Numerable, Seqable, Reducible, Iterable<Term>, Growable<S> {
  //--contract:

  Term first();

  Seq rest();

  boolean isNil();

  default boolean isCounted() {
    return false;
  }

  //--methods:
  default int boundLength(int maxLen) {
    /**
     * **: actually requires isNil: LazySeq doesn't evaluate to check if isCounted : could return false for nil
     */
    int curLen = 0;
    for (SeqLike cur = this;
         curLen <= maxLen && !cur.isNil(); //WRONG: no need to check nil: nil is counted*
         cur = cur.rest(), ++curLen)
      if (cur.isCounted())
        return curLen + cur.count().asInteger();

    if (curLen > maxLen) return Integer.MAX_VALUE;
    else return curLen;
  }

  @Override
  public default Term reduce(Term acc, Invocable reducer) {
    for (SeqLike cur = this; !cur.isNil(); cur = cur.rest())
      acc = reducer.invoke(acc, cur.first());
    return acc;
  }

  @Override
  public default Iterator<Term> iterator() {
    return new SeqIterator(this);
  }

  static final class SeqIterator implements Iterator<Term> {
    private SeqLike self;

    public SeqIterator(SeqLike self) {
      this.self = self;
    }

    @Override
    public boolean hasNext() {
      return !self.isNil();
    }

    @Override
    public Term next() {
      Term v = self.first();
      self = self.rest();
      return v;
    }
  }

  //Nil must override
  public default Term firstOrNil() {
    return first();
  }

  //Nil must override
  public default Seq restOrNil() {
    return rest();
  }

}
