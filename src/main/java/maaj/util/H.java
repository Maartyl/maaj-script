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
import maaj.coll.traits.AssocUpdate;
import maaj.coll.traits.AssocUpdateT;
import maaj.coll.traits.Deref;
import maaj.coll.traits.Dissoc;
import maaj.coll.traits.DissocT;
import maaj.coll.traits.Functor;
import maaj.coll.traits.Growable;
import maaj.coll.traits.GrowableT;
import maaj.coll.traits.Indexed;
import maaj.coll.traits.Numerable;
import maaj.coll.traits.Peekable;
import maaj.coll.traits.Reducible;
import maaj.coll.traits.Seqable;
import maaj.coll.traits.TraPer;
import maaj.coll.wrap.VecPWrap;
import maaj.lang.Context;
import maaj.reader.MaajReader;
import maaj.reader.ReaderContext;
import maaj.term.*;
import maaj.term.visitor.Visitor;
import maaj.term.visitor.VisitorDfltHierarchy;

/**
 * General helpers
 * <p>
 * @author maartyl
 */
public class H {

  private H() {
  }

  public static final Nil NIL = Nil.END;
  public static final Nil END = Nil.END;
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
    return o ? Sym.TRUE : NIL;
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
    if (oc == Boolean.class)
      return H.wrap((boolean) o);
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

    //if (!oc.isArray())
    return JWrap.of(o);
    //throw new UnsupportedOperationException("wrapper not yet implemented for: " + oc.getName());
  }

  public static Object unwrapObject(Term t) {
    //TODO: resolve name crash: Term.unwrap vs. this (different concept)

    if (t == null) return null;
    return t.getContent();
  }

  public static boolean bool(Term t) {
    return !t.isNil();
  }

  public static Seq read(Reader r) {
    return read(r, staticReaderContext);
  }

  public static Seq read(String txt) {
    return read(new StringReader(txt));
  }

  public static Seq read(Reader r, ReaderContext cxt) {
    return MaajReader.read(r, cxt);
  }


  public static Seq read(String txt, ReaderContext cxt) {
    return read(new StringReader(txt), cxt);
  }

  public static Term read1(Reader r) {
    return read(r).first();
  }
  public static Term read1(String txt) {
    return read(txt).first();
  }

  public static Term read1(Reader r, ReaderContext cxt) {
    return read(r, cxt).first();
  }

  public static Term read1(String txt, ReaderContext cxt) {
    return read(txt, cxt).first();
  }

  public static Term eval(Term term, Context cxt) {
    return term.eval(cxt);
  }

  public static Term eval(String term, Context cxt) {
    return eval(read1(term), cxt);
  }

  public static Term eval(String term, Context cxt, ReaderContext rcxt) {
    return eval(read1(term, rcxt), cxt);
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

  public static Vec vec(Object[] arr) {
    VecT v = VecH.emptyTransient();
    for (Object o : arr)
      v.doConj(wrap(o));
    return v.asPersistent();
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

  public static Seq list(Object[] ts) {
    Seq s = H.END;
    for (int cur = ts.length - 1; cur >= 0; --cur) {
      s = SeqH.sexp(wrap(ts[cur]), s);
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
  /**
   *
   * @param first
   * @param second
   * @param tail
   * @return cons(first, cons(second, tail))
   */
  public static Seq cons(Term first, Term second, Seq tail) {
    return cons(first, cons(second, tail));
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

  public static Seq seqFrom(Term tt) {
    Term t = tt.unwrap();
    if (t instanceof Seq)
      return (Seq) t;
    if (t instanceof Seqable)
      return ((Seqable) t).seq();

    String extra = tt.getMeta().isEmpty() ? "" : " (meta: " + tt.getMeta().print() + ")";
    throw new IllegalArgumentException("Cannot create Seq from: " + t.getType().getName() + extra);
  }

  public static <T extends Functor<T>> T fmap(T f, Invocable1 m) {
    return f.fmap(m);
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

  public static Int requireInt(Term tt) {
    return TypeVisitors.reqInt.run(tt);
  }
  public static Char requireChar(Term tt) {
    return TypeVisitors.reqChar.run(tt);
  }

  public static Seq requireSeq(Term tt) {
    return TypeVisitors.reqSeq.run(tt);
  }

  public static Num requireNum(Term tt) {
    return TypeVisitors.reqNum.run(tt);
  }

  public static Symbol requireSymbol(Term tt) {
    return TypeVisitors.reqSymbol.run(tt);
  }

  public static Keyword requireKeyword(Term tt) {
    return TypeVisitors.reqKeyword.run(tt);
  }

  public static Monad requireMonad(Term tt) {
    return TypeVisitors.reqMonad.run(tt);
  }

  public static IO requireIO(Term tt) {
    return TypeVisitors.reqIO.run(tt);
  }

  public static Collection requireCollection(Term tt) {
    return TypeVisitors.reqColl.run(tt);
  }

  public static Vec requireVec(Term tt) {
    return TypeVisitors.reqVec.run(tt);
  }

  public static Invocable requireInvocable(Term tt) {
    return TypeVisitors.reqInvocable.run(tt);
  }

  //tests
  public static Term isInt(Term tt) {
    return TypeVisitors.isInt.run(tt);
  }

  public static Term isChar(Term tt) {
    return TypeVisitors.isChar.run(tt);
  }

  public static Term isSeq(Term tt) {
    return TypeVisitors.isSeq.run(tt);
  }

  public static Term isNum(Term tt) {
    return TypeVisitors.isNum.run(tt);
  }

  public static Term isSymbol(Term tt) {
    return TypeVisitors.isSymbol.run(tt);
  }

  public static Term isKeyword(Term tt) {
    return TypeVisitors.isKeyword.run(tt);
  }

  public static Term isMonad(Term tt) {
    return TypeVisitors.isMonad.run(tt);
  }

  public static Term isIO(Term tt) {
    return TypeVisitors.isIO.run(tt);
  }

  public static Term isCollection(Term tt) {
    return TypeVisitors.isColl.run(tt);
  }

  public static Term isVec(Term tt) {
    return TypeVisitors.isVec.run(tt);
  }

  public static Term isInvocable(Term tt) {
    return TypeVisitors.isInvocable.run(tt);
  }

  //- non Term-implementing Interfaces


  public static Deref requireDeref(Term tt) {
    //change to default methods on something extended by Term
    Term t = tt.unwrap();
    if (t instanceof Deref)
      return (Deref) t;

    throw new IllegalArgumentException("Requires Deref, got: " + t.getClass().getName());
  }


  public static Seqable requireSeqable(Term tt) {
    Term t = tt.unwrap();
    if (t instanceof Seqable)
      return (Seqable) t;

    throw new IllegalArgumentException("Requires Seqable, got: " + t.getClass().getName());
  }

  @SuppressWarnings("unchecked")
  public static <T extends Term & Growable<T>> T requireGrowable(Term tt) {
    Term t = tt.unwrap();
    if (t instanceof Growable)
      return (T) t;

    throw new IllegalArgumentException("Requires Growable, got: " + t.getClass().getName());
  }

  @SuppressWarnings("unchecked")
  public static <T extends Term & AssocUpdate<T>> T requireAssocUpdate(Term tt) {
    Term t = tt.unwrap();
    if (t instanceof AssocUpdate)
      return (T) t;

    throw new IllegalArgumentException("Requires AssocUpdate, got: " + t.getClass().getName());
  }

  @SuppressWarnings("unchecked")
  public static <T extends Term & GrowableT<T>> T requireGrowableT(Term tt) {
    Term t = tt.unwrap();
    if (t instanceof GrowableT)
      return (T) t;

    throw new IllegalArgumentException("Requires GrowableT, got: " + t.getClass().getName());
  }

  @SuppressWarnings("unchecked")
  public static <T extends Term & AssocUpdateT<T>> T requireAssocUpdateT(Term tt) {
    Term t = tt.unwrap();
    if (t instanceof AssocUpdateT)
      return (T) t;

    throw new IllegalArgumentException("Requires AssocUpdateT, got: " + t.getClass().getName());
  }

  @SuppressWarnings("unchecked")
  public static <T extends Term & Dissoc<T>> T requireDissoc(Term tt) {
    Term t = tt.unwrap();
    if (t instanceof Dissoc)
      return (T) t;

    throw new IllegalArgumentException("Requires Dissoc, got: " + t.getClass().getName());
  }

  @SuppressWarnings("unchecked")
  public static <T extends Term & DissocT<T>> T requireDissocT(Term tt) {
    Term t = tt.unwrap();
    if (t instanceof DissocT)
      return (T) t;

    throw new IllegalArgumentException("Requires DissocT, got: " + t.getClass().getName());
  }

  @SuppressWarnings("unchecked")
  public static <T extends Term & Peekable> T requirePeekable(Term tt) {
    Term t = tt.unwrap();
    if (t instanceof Peekable)
      return (T) t;

    throw new IllegalArgumentException("Requires Peekable, got: " + t.getClass().getName());
  }

  public static Numerable requireNumerable(Term tt) {
    Term t = tt.unwrap();
    if (t instanceof Numerable)
      return (Numerable) t;

    throw new IllegalArgumentException("Requires Numerable, got: " + t.getClass().getName());
  }

  public static Reducible requireReducible(Term tt) {
    Term t = tt.unwrap();
    if (t instanceof Reducible)
      return (Reducible) t;
    if (t instanceof Seqable)
      return ((Seqable) t).seq();

    throw new IllegalArgumentException("Requires Reducible, got: " + t.getClass().getName());
  }

  public static TraPer requireTraPer(Term tt) {
    Term t = tt.unwrap();
    if (t instanceof TraPer)
      return (TraPer) t;

    throw new IllegalArgumentException("Requires TraPer, got: " + t.getClass().getName());
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

  public static Class classOrNull(String className) {
    try {
      return Class.forName(className);
    } catch (final ClassNotFoundException e) {
      return null;
    }
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

  private static class TypeVisitors {

    private TypeVisitors() {
    }

    static final Visitor<IO, H> reqIO = new VisitorReqBase<IO>("IO") {
      @Override
      public IO io(IO t, H arg) {
        return t;
      }
    };
    static final Visitor<Term, H> isIO = new VisitorIsBase() {
      @Override
      public Term io(IO t, H arg) {
        return t;
      }
    };

    static final Visitor<Monad, H> reqMonad = new VisitorReqBase<Monad>("Monad") {
      @Override
      public Monad monad(Monad t, H arg) {
        return t;
      }
    };
    static final Visitor<Term, H> isMonad = new VisitorIsBase() {
      @Override
      public Term monad(Monad t, H arg) {
        return t;
      }
    };

    static final Visitor<Seq, H> reqSeq = new VisitorReqBase<Seq>("Seq") {
      @Override
      public Seq seq(Seq t, H arg) {
        return t;
      }
    };
    static final Visitor<Term, H> isSeq = new VisitorIsBase() {
      @Override
      public Term seq(Seq t, H arg) {
        return t;
      }
    };

    static final Visitor<Symbol, H> reqSymbol = new VisitorReqBase<Symbol>("Symbol") {
      @Override
      public Symbol symbol(Symbol t, H arg) {
        return t;
      }
    };
    static final Visitor<Term, H> isSymbol = new VisitorIsBase() {
      @Override
      public Term symbol(Symbol t, H arg) {
        return t;
      }
    };

    static final Visitor<Keyword, H> reqKeyword = new VisitorReqBase<Keyword>("Keyword") {
      @Override
      public Keyword keyword(Keyword t, H arg) {
        return t;
      }
    };
    static final Visitor<Term, H> isKeyword = new VisitorIsBase() {
      @Override
      public Term keyword(Keyword t, H arg) {
        return t;
      }
    };

    static final Visitor<Vec, H> reqVec = new VisitorReqBase<Vec>("Vec") {
      @Override
      public Vec vec(Vec t, H arg) {
        return t;
      }
    };
    static final Visitor<Term, H> isVec = new VisitorIsBase() {
      @Override
      public Term vec(Vec t, H arg) {
        return t;
      }
    };

    static final Visitor<Num, H> reqNum = new VisitorReqBase<Num>("Num") {
      @Override
      public Num num(Num t, H arg) {
        return t;
      }
    };
    static final Visitor<Term, H> isNum = new VisitorIsBase() {
      @Override
      public Term num(Num t, H arg) {
        return t;
      }
    };

    static final Visitor<Collection, H> reqColl = new VisitorReqBase<Collection>("Collection") {
      @Override
      public Collection coll(Collection t, H arg) {
        return t;
      }
    };
    static final Visitor<Term, H> isColl = new VisitorIsBase() {
      @Override
      public Term coll(Collection t, H arg) {
        return t;
      }
    };

    static final Visitor<Int, H> reqInt = new VisitorReqBase<Int>("Int") {
      @Override
      public Int integer(Int t, H arg) {
        return t;
      }
    };
    static final Visitor<Term, H> isInt = new VisitorIsBase() {
      @Override
      public Term integer(Int t, H arg) {
        return t;
      }
    };

    static final Visitor<Char, H> reqChar = new VisitorReqBase<Char>("Char") {
      @Override
      public Char character(Char t, H arg) {
        return t;
      }
    };
    static final Visitor<Term, H> isChar = new VisitorIsBase() {
      @Override
      public Term character(Char t, H arg) {
        return t;
      }
    };

    static final Visitor<Invocable, H> reqInvocable = new VisitorReqBase<Invocable>("Invocable") {
      @Override
      public Invocable invocable(Invocable t, H arg) {
        return t;
      }
    };
    static final Visitor<Term, H> isInvocable = new VisitorIsBase() {
      @Override
      public Term invocable(Invocable t, H arg) {
        return t;
      }
    };

    private static class VisitorReqBase<T> implements VisitorDfltHierarchy<T, H> {

      private final String reqClassName;

      public VisitorReqBase(String reqClassName) {
        this.reqClassName = reqClassName;
      }

      @Override
      public T dflt(Term t, H arg) {
        throw new IllegalArgumentException("Requires " + reqClassName + ", got: " + t.getClass().getName());
      }
    }

    private static class VisitorIsBase implements VisitorDfltHierarchy<Term, H> {
      @Override
      public Term dflt(Term t, H arg) {
        return H.NIL;
      }
    }
  }

}
