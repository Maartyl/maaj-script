/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import com.github.krukow.clj_lang.IPersistentVector;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.Map.Entry;

import maaj.coll.LazySeq;
import maaj.coll.Tuple0;
import maaj.coll.Tuple1;
import maaj.coll.Tuple2;
import maaj.coll.Tuple3;
import maaj.coll.traits.Indexed;
import maaj.coll.traits.Seqable;
import maaj.coll.wrap.VecPWrap;
import maaj.reader.MaajReader;
import maaj.reader.ReaderContext;
import maaj.term.*;

/**
 * General helpers
 * <p>
 * @author maartyl
 */
public class H {

  private H() {
  }

  public static final Nil NIL = Nil.NIL;
  public static final NilSeq END = NilSeq.END;
  public static final Term TRUE = Symbol.of("t");
  /**
   * this value is to never be actually stored as anything valid.
   * Possibly returned from lookups to signal that given key was not found.
   * Only if passed in as default. Otherwise normal nil is returned.
   */
  public static final Nil notFoundNil = new Nil() {
  };
  /**
   * for use only in .uniqueInt()
   */
  private static final AtomicInteger UNIQUE = new AtomicInteger(0);

  private static final ReaderContext staticReaderContext = new ReaderContext(symbol("maaj.static"), "<?>");

  private static <TIn> Term wrapNonNull(TIn val, Function<TIn, Term> transformNonNull) {
    if (val == null)
      return H.NIL;
    return transformNonNull.apply(val);
  }

  public static Term wrap(Term o) {
    return wrapNonNull(o, x -> x);
  }
  public static Term wrap(Integer o) {
    return wrapNonNull(o, Int::of);
  }
  public static Term wrap(Long o) {
    return wrapNonNull(o, Int::of);
  }
  public static Term wrap(Double o) {
    return wrapNonNull(o, Dbl::of);
  }
  public static Term wrap(Float o) {
    return wrapNonNull(o, Dbl::of);
  }
  public static Term wrap(Character o) {
    return wrapNonNull(o, Char::of);
  }
  public static Term wrap(int o) {
    return Int.of(o);
  }
  public static Term wrap(long o) {
    return Int.of(o);
  }
  public static Term wrap(double o) {
    return Dbl.of(o);
  }
  public static Term wrap(float o) {
    return Dbl.of(o);
  }
  public static Term wrap(char o) {
    return Char.of(o);
  }
  public static Term wrap(boolean o) {
    return o ? TRUE : NIL;
  }
  public static Term wrap(String o) {
    return wrapNonNull(o, Str::of);
  }
  public static Term wrap(IPersistentVector<Term> o) {
    return VecPWrap.ofNil(o);
  }

  public static Term wrap(Object o) {
    if (o == null)
      return H.NIL;

    if (o instanceof Term)
      return (Term) o;

    Class oc = o.getClass();
    if (oc == Integer.class)
      return Int.of((Integer) o);
    if (oc == Long.class)
      return Int.of((Long) o);
    if (oc == Double.class)
      return Dbl.of((Double) o);
    if (oc == Float.class)
      return Dbl.of((Float) o);
    if (oc == Character.class)
      return Char.of((Character) o);

    if (oc == String.class)
      return Str.of((String) o);

    //JObj

    throw new UnsupportedOperationException("wrapper not yet implemented for: " + oc.getName());
  }

  public static Object unwrap(Term t) {
    //TODO: resolve name crash: Term.unwrap vs. this (different concept)

    if (t == null) return null;
    return t.getContent();
  }

  public static Seq read(Reader r, ReaderContext cxt) {
    return MaajReader.read(r, cxt);
  }

  public static Seq read(Reader r) {
    return read(r, staticReaderContext);
  }

  public static Seq read(String txt) {
    return read(new StringReader(txt));
  }

  public static Term read1(Reader r) {
    return read(r).first();
  }
  public static Term read1(String txt) {
    return read(txt).first();
  }

  public static Map map(Term key, Term val) {
    return MapH.emptyPersistent().assoc(key, val);
  }

  public static Map map(Term key0, Term val0, Term key1, Term val1) {
    return map(key0, val0).assoc(key1, val1);
  }

  public static Map map(Term key0, Term val0, Term key1, Term val1, Term key2, Term val2) {
    return map(key0, val0).assoc(key1, val1).assoc(key2, val2);
  }

  public static KVPair buildAssocEntry(Term key, Term value) {
    return new Tuple2(key, value);
  }

  public static KVPair buildAssocEntry(Entry<Term, Term> e) {
    return new Tuple2(e.getKey(), e.getValue());
  }

  public static Vec tuple() {
    return Tuple0.EMPTY_VEC;
  }

  public static Vec tuple(Term t0) {
    return new Tuple1(t0);
  }

  public static Vec tuple(Term t0, Term t1) {
    return new Tuple2(t0, t1);
  }

  public static Vec tuple(Term t0, Term t1, Term t2) {
    return new Tuple3(t0, t1, t2);
  }

  public static Vec vec(Seq s) {
    return VecH.fromSeq(s);
  }

  public static Symbol symbol(String symbol) {
    return Symbol.of(symbol);
  }

  public static Symbol symbol(String ns, String name) {
    return Symbol.qualified(ns, name);
  }

  /**
   * eagerly constructs list (Sexp) from given args array
   * <p>
   * @param ts terms
   * @return args as seq
   */
  public static Seq list(Term... ts) {
    Seq s = H.END;
    for (int cur = ts.length - 1; cur >= 0; --cur) {
      s = SeqH.sexp(ts[cur], s);
    }
    return s;
  }

  public static Seq list() {
    return H.END;
  }

  public static Seq list(Term t1) {
    return Seq.retM1(t1);
  }

  public static Seq list(Term t1, Term t2) {
    return SeqH.sexp(t1, list(t2));
  }

  public static Seq list(Term t1, Term t2, Term t3) {
    return SeqH.sexp(t1, list(t2, t3));
  }

  public static Seq list(Term t1, Term t2, Term t3, Term t4) {
    return SeqH.sexp(t1, list(t2, t3, t4));
  }

  public static Seq list(Term t1, Term t2, Term t3, Term t4, Term t5) {
    return SeqH.sexp(t1, list(t2, t3, t4, t5));
  }

  public static Class[] typesOfElems(Seq seq) {
    Class[] clss = new Class[seq.count().asInteger()];
    RangeSeeder rs = new RangeSeeder(); //lambda cannot bind mutable reference
    seq.foreach((Invocable1) x -> {
      clss[rs.next()] = x.getType();
      return H.NIL;
    });
    return clss;
  }

  public static Object[] contentsOfElems(Seq seq) {
    Object[] objs = new Object[seq.count().asInteger()];
    RangeSeeder rs = new RangeSeeder(); //lambda cannot bind mutable reference
    seq.foreach((Invocable1) x -> {
      objs[rs.next()] = x.getContent();
      return H.NIL;
    });
    return objs;
  }

  public static Seq cons(Term head, Seq tail) {
    return SeqH.cons(head, tail);
  }
//  public static Seq sexp(Term head, Seq tail) {
//    return SeqH.sexp(head, tail);
//  }
  public static Seq lazy(Invocable seqBuilder) {
    return new LazySeq(seqBuilder);
  }

  //just for lambdas for Invocable is not func. ifce //checks me too
  public static Seq lazy(Invocable0 seqBuilder) {
    return lazy((Invocable) seqBuilder);
  }

  //this kind wrap makes no sense... : head is already computed anyway...
//  public static Seq lazy(Term head, Invocable seqBuilder) {
//    return lazy(() -> cons(head, (Seq) seqBuilder.invoke()));
//  }
//
//  public static Seq lazy(Term head, Invocable0 seqBuilder) {
//    return lazy(head, (Invocable) seqBuilder);
//  }

  public static Seq lazy(Term head, Invocable seqBuilder) {
    return cons(head, lazy(seqBuilder));
  }

  public static Seq lazy(Term head, Invocable0 seqBuilder) {
    return lazy(head, (Invocable) seqBuilder);
  }

  public static Seq seqFrom(Term t) {
    if (t instanceof Seq)
      return (Seq) t;
    if (t instanceof Seqable)
      return ((Seqable) t).seq();

    throw new IllegalArgumentException("Cannot create Seq from: " + t.getClass().getName());
  }

  /**
   * The created seq is semi-immutable. Realized nodes are immutable,
   * but if data in in underlying Indexed change, the change will be reflected upon
   * realizing given node of seq.
   * <p>
   * @param v indexed data source
   * @return seq of nth(0) ... nth(.getCountAsInteger()-1)
   */
  public static Seq indexed2Seq(Indexed v) {
    return SeqH.incremental2lazySeq(v::nth, i -> i >= v.getCountAsInteger());
  }

  //--requires:

  public static Int requireInt(Term t) {
    //TODO: change all requires to default methods on Term
    // - actually: another interfece extended by Term
    if (t instanceof Int)
      return (Int) t;

    throw new IllegalArgumentException("Requires Int, got: " + t.getClass().getName());
  }
  public static Char requireChar(Term t) {
    //TODO: change all requires to default methods on Term
    if (t instanceof Char)
      return (Char) t;

    throw new IllegalArgumentException("Requires Char, got: " + t.getClass().getName());
  }

  public static int inc(int i) {
    return i + 1;
  }

  public static Num inc(Num i) {
    return i.inc();
  }

  public static int uniqueInt() {
    return UNIQUE.getAndIncrement();
  }

  /**
   * unique symbol, name starts with given string
   * <p>
   * @param name
   * @return
   */
  public static Symbol uniqueSymbol(String name) {
    return symbol(name + "_#" + uniqueInt());
  }
  /**
   * unique symbol of form '_auto_#456
   * <p>
   * @return
   */
  public static Symbol uniqueSymbol() {
    return uniqueSymbol("_auto");
  }

  /**
   * Generates unique symbol with the name (and possibly same namespace) of given symbol, but adds some unique number to the name.
   * <p>
   * @param s 'a ; 'a/b
   * @return 'a_#156 ; 'a/b_#245
   */
  public static Symbol uniqueSymbol(Symbol s) {
    if (s.isQualified()) {
      //this is not super efficient, but also not common
      return uniqueSymbol(s.getNm()).prependNamespace(s.getNs());
    } else {
      return uniqueSymbol(s.getNm());
    }
  }


  /**
   * Allows to perform arbitrary statement in second argument for side effects in any place, thanks to returning first.
   * - Thanks to Java evaluation: ignore is always run after computing ret
   * <p>
   * @param <T>    ret type
   * @param <U>    something to ignore
   * @param ret    this will be returned
   * @param ignore side-effect-ful something
   * @return ret
   */
  public static <T, U> T ret1(T ret, U ignore) {
    return ret;
  }


  /**
   * http://www.mail-archive.com/javaposse@googlegroups.com/msg05984.html
   * <p>
   * "throw H.sneakyThrow(ex)" to fix Java exception handling bug
   * <p>
   * <p>
   * I love this bug-fix. If anyone were to be against it, I would like to remind them, that they have said themselves that
   * explicit throwing wrap all exceptions in all method declarations was a bad design decision in the first place.
   * <p>
   * @param t
   * @return t
   */
  public static RuntimeException sneakyThrow(Throwable t) {
    if (t == null) throw new NullPointerException("t");
    H.<RuntimeException>sneakyThrow0(t);
    return null;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
    throw (T) t;
  }

  public static class RangeSeeder {

    AtomicInteger cur;

    public RangeSeeder(int start) {
      this.cur = new AtomicInteger(start);
    }

    public RangeSeeder() {
      this(0);
    }

    public int next() {
      return cur.getAndIncrement();
    }
  }

}
