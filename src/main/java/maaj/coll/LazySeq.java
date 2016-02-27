/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll;

import java.io.IOException;
import maaj.util.H;
import maaj.util.SeqH;
import maaj.term.Int;
import maaj.term.Invocable;
import maaj.term.Map;
import maaj.term.Seq;
import maaj.term.Str;
import maaj.term.Term;

/**
 * represents lazy sequence node: seq only gets computed when needed, 1 node at a time
 * forwards most operations onto underlying seq
 * <p>
 * @author maartyl
 */
public final class LazySeq implements Seq {

  private Invocable fn;
  private Seq s = null; //null is not a valid Seq : will always be NiSeq or something...

  /**
   *
   * @param seqBuilder :: () -> Seq
   */
  public LazySeq(Invocable seqBuilder) {
    if (seqBuilder == null)
      throw new IllegalArgumentException("seqBuilder cannot be null");
    this.fn = seqBuilder;
  }

  @Override
  public Term first() {
    return seq().first();
  }

  @Override
  public Seq rest() {
    return seq().rest();
  }

  @Override
  public Term firstOrNil() {
    return seq().firstOrNil();
  }

  @Override
  public Seq restOrNil() {
    return seq().restOrNil();
  }

  @Override
  public boolean isNil() {
    return seq().isNil();
  }


  @Override
  public Int count() {
    return seq().count();
  }

  @Override
  public boolean isCounted() {
    return s != null && seq().isCounted();
  }

  @Override
  public Seq fmap(Invocable mapper) {
    //return H.lazy(() -> SeqH.mapLazy(seq(), mapper));
    return SeqH.mapLazy(this, mapper);
  }


 
  @Override
  public Term transform(Invocable transformer) {
    //TODO: reconsider: necessary? - it wouold evaluate first elem...
    return seq().transform(transformer);
  }

  @Override
  public Class getType() {
    return seq().getType();
  }

  @Override
  public Object getContent() {
    return seq().getContent();
  }

  @Override
  public void serialize(java.io.Writer w) throws IOException {
    seq().serialize(w);
  }

  @Override
  public Map getMeta() {
    //possibly EMPTY instead? or at least when not evaluated?
    return seq().getMeta();
  }

  @Override
  public Str show() {
    return seq().show();
  }

  @Override
  public String toString() {
    return seq().toString();
  }

  @Override
  public boolean equals(Object obj) {
    return seq().equals(obj);
  }

  @Override
  public int hashCode() {
    return seq().hashCode();
  }


  @Override
  public final Seq seq() {
    //synchronization forces flushing of membory buffers: no need to make s volatile
    //just null check is fast and complete: not null -> correct: will never change again
    if (s == null) return buildSelf();
    return s;
  }

  /**
   * realizes lazy seq, storing result in s
   * recursively realizes all lazy seqs / Sequables that would be returned by builderFn
   * - so seq stored in s is already some actual non-lazy Seq.
   */
  private synchronized Seq buildSelf() {
    if (s == null) {
      s = H.seqFrom(H.ret1(fn, fn = null).invoke()).seq(); //<- just this is suficient; omitting LazyNodes is done recursively
//      Seq newMe = H.seqFrom(fn.invoke());
//      fn = null;
//
//      while (newMe instanceof LazySeq) //really an if: recursive
//        newMe = newMe.seq(); //in turn builds seq until no longer lazy
//      s = newMe;
    }

    //Seq uses special: MetaSeq anyway... (so probably will functions etc...)
//    if (s == null) { //works with wrappers too; should be only 1 vallvirt per buildSelf slower...
//      s = (Seq) fn.invoke().transform((Invocable1) x -> {
//        Seq newMe = (Seq) x;
//        fn = null;
//
//        while (newMe instanceof LazySeq)
//          newMe = ((LazySeq) newMe).buildSelf();
//
//        return newMe;
//      });
//
//    }

    return s;
  }

}
