/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll;

import maaj.coll.traits.Seqable;
import maaj.lang.Context;
import maaj.util.H;
import maaj.term.*;

/**
 * To be used when prepending to seqs of unknown count
 - as opposed to Sexp that is to build seqs in their entirety
 * <p>
 * @author maartyl
 */
public class Cons implements Seq {

  protected final Term head;
  protected final Seq tail;

  public Cons(Term head, Seq tail) {
    this.head = head;
    this.tail = tail;
  }

  @Override
  public Term first() {
    return head;
  }

  @Override
  public Seq rest() {
    return tail;
  }

  @Override
  public Int count() {
    //Cons shouldn't be used for long structures
    //if used for long structures: count is known to be O(n) to get computed
    //tail can know it's count, that could help

    //this might run ~infinitely! long instead of causing StackOverflow
    return Int.of(boundLength(Integer.MAX_VALUE));
  }

  @Override
  public Seq retM(Term contents) {
    return retM1(contents);
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    throw new UnsupportedOperationException("Cons cannot be used as function."); //TODO: implement
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Term)
      obj = ((Term) obj).unwrap();
    else return false;
    if (!(obj instanceof Seq))
      return false;
    Seq st = this;
    Seq so = (Seq) obj;
    for (; !st.isNil(); st = st.rest(), so = so.rest())
      if (so.isNil() || !st.first().equals(so.first()))
        return false;
      else if (st == so) return true; //share the same tail, at same offset; until now the same
    return so.isNil();
  }

  @Override
  public int hashCode() {
    int hash = 1;
    for (Term t : this)
      hash = hash * 31 + t.hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return print();
  }


  //--- STATIC
  public static Seq retM1(Term t) {
    return new Sexp(1, t, H.END);
  }
}
