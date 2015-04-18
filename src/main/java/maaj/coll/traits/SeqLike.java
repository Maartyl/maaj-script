/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Invocable;
import maaj.term.Seq;
import maaj.term.Term;

/**
 *
 * @author maartyl
 */
public interface SeqLike extends Numerable, Sequable, Reducible {
  //--contract:

  Term first();

  Seq rest();

  boolean isNil();

  default boolean isCounted() {
    return false;
  }

  //--methods:
  default int boundLength(int maxLen) {
    int curLen = 0;
    for (SeqLike cur = this;
         curLen <= maxLen; //no need to check nil: nil is counted
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
}
