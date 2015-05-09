/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import java.util.Iterator;
import maaj.coll.Cons;
import maaj.coll.Sexp;
import maaj.lang.Context;
import maaj.term.*;

/**
 * Seq helpers - operations on seqs
 * <p>
 * @author maartyl
 */
public class SeqH {

  private SeqH() {
  }

  //--ctors
  public static Sexp sexp(Term t, Seq s) {
    return new Sexp(t, s);
  }

  public static Cons cons(Term t, Seq s) {
    return new Cons(t, s);
  }

  public static Seq mapEval(Seq s, Context c) {
    return s.fmap((Invocable1) x -> x.eval(c));
  }

  //--eager variants

  /**
   * eager, recursive
   * @param seqs [[1,2],[],[1],[[2,5],4,8]]
   * @return [1,2,1,[2,5],4,8]
   */
  public static Seq concatSexp(Seq seqs) {
    if (seqs.isNil()) return H.END;
    return concatSexp((Seq) seqs.first(), seqs.rest());
  }
  /**
   * eager, recursive
   */
  private static Seq concatSexp(Seq firsts, Seq rests) {
    if (firsts.isNil()) return concatSexp(rests);
    return sexp(firsts.first(), concatSexp(firsts.rest(), rests));
  }
  /**
   * eager, recursive
   * @param coll   [1, 2, 3]
   * @param mapper f
   * @return [f 1, f 2, f 3]
   */
  public static Seq mapSexp(Seq coll, Invocable mapper) {
    if (coll.isNil()) return H.END;
    return sexp(mapper.invoke(coll.first()), mapSexp(coll.rest(), mapper));
  }

  //--lazy variants
  /**
   * lazy
   * <p>
   * @param seqs [[1,2],[],[1],[[2,5],4,8]]
   * @return [1,2,1,[2,5],4,8]
   */
  public static Seq concatLazy(Seq seqs) {
    if (seqs.isNil()) return H.END;
    if (seqs.rest().isNil()) return (Seq) seqs.first();
    return concatLazy((Seq) seqs.first(), seqs.rest());
  }

  /**
   * lazy
   */
  private static Seq concatLazy(Seq firsts, Seq rests) {
    if (firsts.isNil()) return concatLazy(rests);
    return H.lazy(firsts.first(), () -> concatLazy(firsts.rest(), rests));
  }

  /**
   * lazy
   * <p>
   * @param coll   [1, 2, 3]
   * @param mapper f
   * @return [f 1, f 2, f 3]
   */
  public static Seq mapLazy(Seq coll, Invocable mapper) {
    return H.lazy(() -> mapLazyInner(coll, mapper));
  }

  private static Seq mapLazyInner(Seq coll, Invocable mapper) {
    if (coll.isNil()) return H.END;
    return H.lazy(mapper.invoke(coll.first()), () -> mapLazyInner(coll.rest(), mapper));
  }

  /**
   * @param iterable
   * @return seq of all results from calling .next() on iterator
   */
  public static Seq iterable2seq(Iterable<? extends Term> iterable) {
    return H.lazy(new Invocable0() {
      private final Iterator<? extends Term> it = iterable.iterator();

      /**
       * There is no point creating new lambda instance for each iteration: they share the same iterator.
       */
      @Override
      public Term invoke() {
        if (!it.hasNext()) return H.END;
        return H.lazy(it.next(), this);
      }
    });
  }

  /**
   * This variant wraps all elements for seq that need it.
   * <p>
   * @param iterable
   * @return seq of wrapped results from calling .next() on iterator
   */
  public static Seq iterableWrap2seq(Iterable<?> iterable) {
    return H.lazy(new Invocable0() {
      private final Iterator<?> it = iterable.iterator();

      /**
       * There is no point creating new lambda instance for each iteration: they share the same iterator.
       */
      @Override
      public Term invoke() {
        if (!it.hasNext()) return H.END;
        return H.lazy(H.wrap(it.next()), this);
      }
    });
  }


  //Generic Indexed lazy seq {
  /**
   * creates lazy seq from indexed something represented by functions that access it
   * head of produced seq is not lazy
   * start: 0; update: ++
   * @param getter  gets Term at passed position
   * @param testEnd tests if list should end (end of array)
   * @return lazy seq of values from getter
   */
  public static Seq incremental2lazySeq(PerIndexRetriever<Term> getter, PerIndexTestEnd testEnd) {
    return incremental2lazySeq(0, getter, H::inc, testEnd);
  }

  /**
   * creates lazy seq from indexed something represented by functions that access it
   * head of produced seq is not lazy
   * start: 0
   * <p>
   * @param getter  gets Term at passed position
   * @param updater updates current index to next
   * @param testEnd tests if list should end (end of array)
   * @return lazy seq of values from getter
   */
  public static Seq incremental2lazySeq(PerIndexRetriever<Term> getter, PerIndexUpdater updater, PerIndexTestEnd testEnd) {
    return incremental2lazySeq(0, getter, updater, testEnd);
  }

  /**
   * creates lazy seq from indexed something represented by functions that access it
   * head of produced seq is not lazy
   * <p>
   * @param startIndex initial index for getter
   * @param getter     gets Term at passed position
   * @param updater    updates current index to next
   * @param testEnd    tests if list should end (end of array)
   * @return lazy seq of values from getter
   */
  public static Seq incremental2lazySeq(int startIndex, PerIndexRetriever<Term> getter, PerIndexUpdater updater, PerIndexTestEnd testEnd) {
    if (testEnd.isEnd(startIndex)) return H.END;
    return H.lazy(getter.valAt(startIndex), () -> incremental2lazySeq(updater.update(startIndex), getter, updater, testEnd));
  }
  public static interface PerIndexRetriever<T> {

    T valAt(int i);
  }

  public static interface PerIndexUpdater {

    int update(int i);
  }

  public static interface PerIndexTestEnd {

    boolean isEnd(int i);
  }
  //}
}
