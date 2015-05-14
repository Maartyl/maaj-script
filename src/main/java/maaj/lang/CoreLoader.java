/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import maaj.coll.traits.Counted;
import maaj.coll.traits.Reducible;
import maaj.coll.traits.SeqLike;
import maaj.exceptions.InvalidOperationException;
import maaj.reader.ReaderContext;
import maaj.term.Fn;
import maaj.term.FnSeq;
import maaj.term.Int;
import maaj.term.Invocable;
import maaj.term.Invocable1;
import maaj.term.Keyword;
import maaj.term.Macro;
import maaj.term.MacroSeq;
import maaj.term.Map;
import maaj.term.Num;
import maaj.term.Seq;
import maaj.term.Sf;
import maaj.term.Str;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.Var;
import maaj.term.Vec;
import maaj.util.FnH;
import maaj.util.H;
import maaj.util.MapH;
import maaj.util.SeqH;
import maaj.util.Sym;

/**
 * loader that does not use file, instead: loads core special forms, macros and functions
 * <p>
 * @author maartyl
 */
public class CoreLoader extends Namespace.Loader {

  @Override
  public Namespace loadNamespaceFor(Symbol nsName, Context cxt) {
    if (!nsName.isSimple())
      throw new IllegalArgumentException("invalid namespace name: " + nsName.print());
    Namespace ns = createEmptyWithName(nsName);
    Context c = cxt.withNamespace(ns);
    switch (nsName.getNm()) {
    case "#": loadSf(ns);
      break;
    case "#core": loadCore(c, ns, new ReaderContext(nsName, "<?>"));
      break;
    case "#macro": loadMacro(ns);
      break;
    default:
      throw new IllegalArgumentException("no core namespace with name: " + nsName.print());
    }
    return ns;
  }
  /**
   * special forms
   * called as very first
   * cannot depend on anything else
   * Context doesn't work yet
   */
  private void loadSf(Namespace core) {
    def(core, "if", "", (c, a) -> {
      int len = a.boundLength(3);
      if (len == 2)
        return a.first().eval(c).isNil() ? H.NIL : a.rest().first().eval(c);
      if (len == 3)
        return !a.first().eval(c).isNil() ? a.rest().first().eval(c) : a.rest().rest().first().eval(c);
      throw new InvalidOperationException("Wrong number of args passed to #/if: " + a.boundLength(30));
    });
    def(core, "def", "(def ^{meta here} name term); meta of symbol becomes meta of var", (c, a) -> {
      if (a.boundLength(2) != 2) //TODO: add option to only create without value
        throw new InvalidOperationException("Wrong number of args passed to #/def: " + a.boundLength(30));
      Term name = a.first();
      Term val = a.rest().first().eval(c);
      Term nu = name.unwrap();
      if (defCheckAndRetIfQualified(nu, c.getCurNs().getName())) {
        Var v = c.getVar((Symbol) nu);
        if (v == null) throw new InvalidOperationException("#/def: cannot create qualified var:" + nu.print());
        v.doSet(val);
        //v.addMeta(name.getMeta());
        return v;
      }
      return c.def((Symbol) nu, val, name.getMeta());
    });
    def(core, "do", "(do (action) (action)...); perform in sequence; return last", (c, a) -> {
      if (a.isNil()) return H.NIL;
      while (!a.rest().isNil()) {
        a.first().eval(c);
        a = a.rest();
      }
      return a.first().eval(c);
    });
    def(core, "let", "(let [v exp v2 exp3] ( ... v2 )(... v ... v ...))", (c, a) -> {
      if (a.isNil())
        throw new InvalidOperationException("#/let: requires bindings");
      if (!(a.first().getContent() instanceof Vec))
        throw new InvalidOperationException("#/let: bindings must be a vector");
      return H.cons(Sym.doSymC, a.rest()).eval(letEvalBindings(c, (Vec) a.first()));
    });
    def(core, "fnseq", "(fnseq body body $args body)", (c, a) -> FnSeq.of(a, c));
    def(core, "macroseq", "(macroseq body body $args body)", (c, a) -> MacroSeq.of(a, c));
    def(core, "eval", "evaluates given term", (c, a) -> {
      arityRequire(1, a, "eval");
      return a.first().eval(c).eval(c);
    });
    def(core, "apply", "applies first argument on [last argumet (seq) with other arguments prepended]; "
                       + "(apply + 7 8 [4 5 6]) -> (+ 7 8 4 5 6)",
        (c, a) -> a.isNil() ? H.NIL : SeqH.extend(SeqH.mapEval(a, c)).eval(c));

    defmacro(core, "fn", "creates function that binds args", a -> argsBindMacro(a, Sym.fnseqSymC));
    defmacro(core, "macro", "creates macro that binds args", a -> argsBindMacro(a, Sym.macroseqSymC));
    defmacro(core, "fn2", "creates function that binds args", a -> argsBindMacro2(a, Sym.fnseqSymC));
  }

  private Term argsBindMacro2(Seq a, Symbol fnType) {
    if (a.isNil())
      throw new IllegalArgumentException("Cannot bind args withut binding form");
    if (!(a.first().unwrap() instanceof Seq)) //if simple body without overloads: make it 1 overload
      return argsBindMacro2(H.list(a.addMeta(a.first().getMeta())), fnType);
    //map "create overload" over args, affregate and summary, compose into case
    Seq data = a.fmap((Invocable1) x -> argsBindOverloadData(x));
    Map aritys = (Map) data.reduce(H.map(Sym.maxSymK, Int.of(Integer.MIN_VALUE)), FnH.<Map, Seq, Map>liftTypeUncheched2((m, s) -> {
      Term ar = s.first();
      Num arity = (Num) ar.unwrap();
      if (m.containsKey(arity))
        throw new IllegalArgumentException(
                "Arity overload clash: " + arity + ": " + ar.getMeta().valAt(Sym.patternSym) + s.rest()
                + " and " + m.valAt(arity).getMeta().valAt(Sym.patternSym) + m.valAt(arity));
      if (arity.asInteger() < 0) { // variadic arity; can only contain 1
        if (m.containsKey(Sym.variadicSymK))
          throw new IllegalArgumentException(
                  "Multiple variadic overloads (" + arity.neg().dec() + "+): "
                  + ar.getMeta().valAt(Sym.patternSym) + s.rest() + " and "
                  + m.valAt(Sym.variadicSymK).getMeta().valAt(Sym.patternSym) + m.valAt(Sym.variadicSymK));
        else //variadic set, meta: arity
          return m.assoc(Sym.variadicSymK, s.rest()
                         .addMeta(ar.getMeta())
                         .addMeta(H.map(Sym.aritySymK,
                                        arity.neg().dec())));
      }
      Num maxOld = (Num) m.valAt(Sym.maxSymK).unwrap(); // I will need to know maximal arity
      return H.map(arity, s.rest().addMeta(ar.getMeta()),
                   Sym.maxSymK, maxOld.max(arity));
    }));
    Num maxArity = ((Num) aritys.valAt(Sym.maxSymK));

    if (maxArity.asInteger() == Integer.MIN_VALUE) {
      //either : no overloads or only variadic
      //there must be at least 1 overload : can only be 1 variadic function
      //this is : simple function with 1 variadic overload
      assert aritys.containsKey(Sym.variadicSymK) : "expected 1 variadic overload but not present: " + a + ", " + aritys;
      Term body = aritys.valAt(Sym.variadicSymK);
      return H.list(fnType, argsBindArityDispatchVariadic(body, a));
    }
    Seq posData = SeqH.filter(data, x -> H.wrap(((Num) ((SeqLike) x).first().unwrap()).asInteger() >= 0));
    Seq notMatched = aritys.containsKey(Sym.variadicSymK) ?
             argsBindArityDispatchVariadic(aritys.valAt(Sym.variadicSymK), a) :
                     H.list(Sym.throwAritySymCore, Sym.argsSym, H.list(Sym.quoteSymC, a));
    Seq els = H.list(Sym.ignoreSym, notMatched);
    Seq allCases = SeqH.concatLazy(H.list(SeqH.concatLazy(posData), els));
    Seq allWithCase = H.cons(Sym.caseSymCore, H.list(Sym.countPrimeSymCore, maxArity, Sym.argsSym), allCases);
    return H.list(fnType, allWithCase);
  }

  private Seq argsBindArityDispatchVariadic(Term body, Term origData) {
    Num minArity = (Num) body.getMeta().valAt(Sym.aritySymK);
    if (minArity.asInteger() == 0) {
      return argsBindMacroLet(H.seqFrom(body));
    }
    // `(~fnType (if (< (count' ~minArity $args) ~minArity) (throw-arity $args ~a) ~@(argsBindMacroLet (seq body)))
    return (SeqH.extend(H.list(Sym.ifSymC,
                                     H.list(Sym.LTSymCore,
                                            H.list(Sym.countPrimeSymCore, minArity, Sym.argsSym),
                                            minArity),
                               H.list(Sym.throwAritySymCore, Sym.argsSym, H.list(Sym.quoteSymC, origData)),                                     argsBindMacroLet(H.seqFrom(body)))));
  }

  private Seq argsBindOverloadData(Term t) {
    if (!(t.unwrap() instanceof Seq))
      throw new InvalidOperationException("Overload is not a seq: " + t);
    Seq s = (Seq) t.unwrap();
    if (s.isNil())
      throw new IllegalArgumentException("Cannot bind args withut binding form");
    int arity = argsBindPatternArity(s.first());
    //{:arity arity :body (argsBindLet s)}
    //return H.map(Sym.aritySymK, H.wrap(arity), Sym.bodySymK, argsBindMacroLet(s));
    //this creates ~sort of 'case cases right away (some might my negative and stuff though...)
    return H.cons(H.wrap(arity).addMeta(H.map(Sym.patternSym, s.first())), argsBindMacroLet(s));
  }

  /**
   * negative means variadic arity
   * - the number means : "at least"
   * -- but -1: otherwise : I would get 0 for: [& r]
   * -- that returns -1
   * -- [ a b c & r ] -> -4
   * - probably not important, but why not...
   * determines arity from pattern binding
   */
  private int argsBindPatternArity(Term ptrn) {
    if (ptrn.unwrap() instanceof Symbol) {
      Symbol s = (Symbol) ptrn.unwrap();
      if (s.isSimple())
        return -1; // simple symbol captures all args : variadic
    }
    if (!(ptrn.unwrap() instanceof Vec))
      throw new IllegalArgumentException("Cannot create args binding pattern from: " + ptrn);
    Vec v = (Vec) ptrn.unwrap();
    if (v.cnt() < 2)
      return v.cnt();
    if (v.nth(v.cnt() - 2).equals(Sym.ampSym)) // [... & rest] : variadic
      return -(v.cnt() - 2) - 1; // [a b c & r] : returns negated : number of needed args -1
    return v.cnt(); //not variadic : this is how many args it takes ; [a b c]
    //[] is a valid args bind meaning: this takes 0 arguments
  }

  private Term argsBindMacro(Seq a, Symbol fnType) {
    return argsBindMacroSimple(a, fnType);
  }

  /**
   * fn and macro core transformations:
   * (fn binding body1 body2) -> (fnseq (let [binding $args] body1 body2))
   * //macro only creates macroseq
   * binding gets 'compiled' using @patternBinder
   */
  private Seq argsBindMacroSimple(Seq a, Symbol fnType) {
    return H.cons(fnType, argsBindMacroLet(a));
  }

  private Seq argsBindMacroLet(Seq a) {
    if (a.isNil())
      throw new IllegalArgumentException("Cannot bind args withut binding form");
    Term ptrn = a.first();
    Seq body = a.rest();
    if (body.isNil())
      return H.list(); // nothing would use the pattern anyway...
    Term pb = patternBinder(ptrn).addMeta(ptrn.getMeta()).addMeta(H.map(Sym.patternSym, ptrn));
    return H.list(H.cons(Sym.letSymC, H.cons(H.tuple(pb, Sym.argsSym), body)));
  }

  /**
   * multiple conditional divergences of path : like if with multiple tests
   * evaluates first body after successful test; not evaluating anything after that
   * - throw exception if not even number of arguments
   * i.e. (cond test1 body1 test2 body2 test3 body3)
   * (cond) returns nil
   * impl:
   * (cond test body ^rest^) -> (if test body (cond ^rest^))
   * <p>
   * --
   * this could be defined in the language; sure : but I need it for arity dispatch
   * and prefer not to do it without it
   * - but more importantly: it requires me to throw IllegalArgument ... can't do that yet
   * - maybe rewrite as normal macro at some point later...
   */
  private Term condMacro(Seq a) {
    switch (a.boundLength(2)) {
    case 0: return H.END; //also nil; but can be seq
    case 1: throw new IllegalArgumentException("cond: odd number of terms //: " + a.first().print());
    default:
      Term test = a.first();
      Term body = a.rest().first();
      Seq rest = a.rest().rest();
      return H.list(Sym.ifSymC, test, body, H.cons(Sym.condSymCore, rest));
    }
  }

  /**
   * like Java switch: evaluates expression once and then matches it with options
   * _ is equal to default, but need not to be last ... everything afterwards is ignored, though
   * (case (eval-exp) 1 body1 2 body2 45 body3)
   * is defined in terms of cond and .equals()
   * - no matching: returns nil
   * (case exp t1 b1 t2 b2) -> (let [g## exp] (cond (= g t1) b1 (= g t2) b2))
   */
  private Term caseMacro(Seq a) {
    switch (a.boundLength(3)) {
    case 0: throw new IllegalArgumentException("case: requires expression to match on");
    case 1: return H.NIL; //no match cases
    case 2: throw new IllegalArgumentException("case: match case withou body");
    //(cond exp match body) -> (if (=# match exp) body) // no need for let
    case 3: return H.list(Sym.ifSymC, H.list(Sym.equalSymCCore, a.rest().first(), a.first()), a.rest().rest().first());
    default:
      Symbol gensym = H.uniqueSymbol("exp");
      Term exp = a.first();
      Seq matchList = SeqH.mapAlternate(a.rest(), x -> Sym.ignoreSym.equals(x) ? Sym.elseSymK :
                                                       H.list(Sym.equalSymCCore, x, gensym), FnH::id);
      return H.list(Sym.letSymC, H.tuple(gensym, exp), H.cons(Sym.condSymCore, matchList));
    }
  }

  /**
   * gets vector of binding from let;
   * modifies context to include bound evaluated expressions
   */
  private Context letEvalBindings(Context cxt, Vec v) {
    if (v.getCountAsInteger() % 2 != 0)
      throw new InvalidOperationException("#/let: binding requires even number of terms");
    
    for (Seq s = v.seq(); !s.isNil(); s = s.rest().rest()) {
      Invocable ptrn = patternBinder(s.first()); //create patternBinder from first in pair
      Term r = s.rest().first().eval(cxt); //evaluate second
      cxt = cxt.addToScope(applyPatternBinder(ptrn, r)); //pattern bind result; add to scope
    }
    return cxt;
  }

  /**
   * throws on any problem; only returns if nu is valid Symbol
   * returns if ((Sym)nu).isQualified
   */
  private boolean defCheckAndRetIfQualified(Term nu, Symbol curNs) {
    if (nu instanceof Keyword)
      throw new InvalidOperationException("#/def: requires symbol for name, got: " + nu.getType().getName());
    if (!(nu instanceof Symbol))
      throw new InvalidOperationException("#/def: requires symbol for name, got: " + nu.getType().getName());
    Symbol s = (Symbol) nu;
    if (s.isQualified()) {
      if (s.getNs() == null ? curNs.getNm() != null : !s.getNs().equals(curNs.getNm()))
        throw new InvalidOperationException("#/def: cannot modify vars outside current namespace: " + s.print());
      return true;
    }
    return false;
  }

  /**
   * Creates pattern binder
   * produces function that takes 1 arg.
   * it destructures the argument into subparts that are named in term by structure in term
   * returns map from these names to subvalues of arg
   * i.e.: (let [[a [ba bb] c & rest] (some-expression-generating-seq)] (i-can-use a ba etc.-in-here))
   */
  private Invocable patternBinder(Term t) {
    // I only have limited breaker for now : vector : firss and rest of seq
    //or just symbol
    Term tptrn = t.unwrap();
    if (tptrn instanceof Symbol) { //fast path
      Symbol s = (Symbol) tptrn;
      if (!s.isSimple())
        throw new IllegalArgumentException("Cannot create pattern binder from qualified symbol or keyword: " + s);
      if (s.equals(Sym.ignoreSym))
        return (Invocable1) x -> MapH.emptyPersistent();
      return (Invocable1) x -> H.map(s, x);
    }
    if (tptrn instanceof Vec) {
      /*
       works with seqables
       how it works:
       I recursively map ptrnBinder over the vector of patterns I get
       then, with vector of functions: I zip it with args, applying it -> seq of maps
       I then join these maps (MapH.update)
       - if next to last is '& : the last binder after that binds the rest of seq in arg
       -- I make the vector 2 shorter, do the same on it as before
       -- I get the last thing and append the map as last,
       --- invoking it's binder on (drop <v2 captured cnt> args)
       */
      Vec v = (Vec) tptrn;
      if (v.cnt() > 1) {
        if (v.nth(v.cnt() - 2).equals(Sym.ampSym)) {
          //has rest capture:
          Vec v2 = v.pop().pop();
          int size = v2.cnt() - 1; //I essentially want last index
          Vec vptrn = v2.fmap((Invocable1) this::patternBinder); //recursively, eagerly create pattern binders
          Invocable restPtrn = patternBinder(v.peek());
          return (Invocable1) a
                  -> MapH.update(applyVectorPatternBinder(a, vptrn),
                                 (Map) restPtrn.invoke(SeqH.drop(size, H.seqFrom(a))));
        }
      }
      if (v.isEmpty())
        return (Invocable1) x -> MapH.emptyPersistent();
      Vec vptrn = v.fmap((Invocable1) this::patternBinder);
      return (Invocable1) a -> applyVectorPatternBinder(a, vptrn);
    }
    if (tptrn instanceof Invocable)
      return (Invocable) tptrn; //Assume to be something able of pattern binding; if not : used incorrectly ...
    throw new IllegalArgumentException("Cannot create pattern binder from: " + tptrn.getType().getName() + " : " + tptrn.print());
  }

  /**
   * zips vector pattern with args seq using function: invoke first on second
   * - producing seq of maps
   * then reduces the entire thing into 1 map of resulting (symbol -> "matched")
   */
  private Map applyVectorPatternBinder(Term term, Vec ptrn) {
    return (Map) SeqH.zipl(FnH.invoke1(), ptrn.seq(), H.seqFrom(term))
            .reduce(MapH.emptyPersistent(), FnH.<Map, Map, Map>liftTypeUncheched2(MapH::update));
  }

  private Map applyPatternBinder(Invocable binder, Term t) {
    return (Map) binder.invoke(t);
  }

  private void loadCore(Context cxt, Namespace core, ReaderContext rcxt) {
    defmacro(core, "defn", "creates and defs a function", a
             -> H.list(Sym.defSymC, a.first(), H.cons(Sym.fnSymC, a.rest())));
    defmacro(core, "defmacro", "creates and defs a function", a
             -> H.list(Sym.defSymC, a.first().addMeta(Sym.macroMapTag), H.cons(Sym.macroSymC, a.rest())));

    defmacro(core, "cond", "Takes pairs of - test body; evaluates only body after first successful test", this::condMacro);
    defmacro(core, "case", "Takes expr pairs (match body); works like cond with =#; evaluates expr once", this::caseMacro);


    defn(core, "meta", "get meta data of term", a -> a.isNil() ? H.NIL.getMeta() : a.first().getMeta());
    defn(core, Sym.firstSym.getNm(), "first of seq (head)", a -> H.seqFrom(arityRequire(1, a, "first").first()).firstOrNil());
    defn(core, Sym.restSym.getNm(), "rest of seq (tail)", a -> H.seqFrom(arityRequire(1, a, "rest").first()).restOrNil());
    defmacro(core, "car", "first of seq (head)", a -> H.cons(Sym.firstSym, a));
    defmacro(core, "cdr", "rest of seq (tail)", a -> H.cons(Sym.restSym, a));
    defmacro(core, "cadr", "(first (rest a))", a -> H.list(Sym.firstSym, H.cons(Sym.restSym, a)));
    defmacro(core, "cddr", "(rest (rest a))", a -> H.list(Sym.restSym, H.cons(Sym.restSym, a)));
    defn(core, "seq", "seq from collection", a -> H.seqFrom(arityRequire(1, a, "seq").first()));
    defn(core, "count", "number of elements in collection; possibly O(N)", a
         -> H.requireNumerable(arityRequire(1, a, "count")).count());
    defn(core, "count'", "number of elements in ^2 collection; O(1) possibly incorrect; "
                         + "if counts, returns maximally ^1 specified value"
                         + "(count' 5 (100)) -> Int.MaxValue", a -> {
      arityRequire(2, a, "count'");
      Term coll = a.rest().first().unwrap();
      if (coll instanceof Counted)
        return ((Counted) coll).getCount();
      Num max = H.requireNum(a.first());
      return H.wrap(H.seqFrom(coll).boundLength(max.asInteger()));
            });

    defn(core, "cons", "prepends to list; O(1)", a -> {
      arityRequire(2, a, "cons");
      return H.cons(a.first(), H.seqFrom(a.rest().first()));
    });

    defn(core, "not", "(if % () 't)", a -> arityRequire(1, a, "not").first().isNil() ? Sym.TRUE : H.NIL);

    defn(core, "reduce", "get meta data of term", a -> {
      arityRequire(3, a, "reduce");
      Invocable fn = H.requireInvocable(a.first());
      Term start = a.rest().first();
      Reducible coll = H.requireReducible(a.rest().rest().first());
      return coll.reduce(start, fn);
    });

    defn(core, Sym.equalSymCCore.getNm(), "equals?", a -> {
      arityRequire(2, a, "=#");
      return H.wrap(a.first().equals(a.rest().first()));
    });

    defn(core, "+#", "adds 2 args", (Num.Num2Op) Num::add);
    defn(core, "-#", "subtracts arg1 from arg0", (Num.Num2Op) Num::sub);
    defn(core, "*#", "multiplies 2 args", (Num.Num2Op) Num::mul);
    defn(core, "div#", "divides arg1 by arg0", (Num.Num2Op) Num::div);
    defn(core, "min#", "(if (< l r) l r)", (Num.Num2Op) Num::min);
    defn(core, "max#", "(if (> l r) l r)", (Num.Num2Op) Num::max);

    defn(core, "inc", "(+ % 1)", (Num.NumOp) Num::inc);
    defn(core, "dec", "(- % 1)", (Num.NumOp) Num::dec);
    defn(core, "neg", "(- 0 %)", (Num.NumOp) Num::neg);

    defn(core, "<", "Num; is first arg less then second?", (Num.NumPred) (Num::lt));
    defn(core, ">", "Num; is first arg greater then second?", (Num.NumPred) (Num::gt));
    defn(core, "==", "Num; is first arg equal to second?", (Num.NumPred) (Num::eq));
    defn(core, "<=", "Num; is first arg less then or equal to second?", (Num.NumPred) (Num::lteq));
    defn(core, ">=", "Num; is first arg greater then or equal to second?", (Num.NumPred) (Num::gteq));

    H.eval("(defn + a (reduce +# 0 a))", cxt, rcxt);
    H.eval("(defn * a (reduce *# 1 a))", cxt, rcxt);
    //H.eval("(def min (fnseq (reduce min# 1 $args)))", cxt, rcxt);
    //H.eval("(def max (fnseq (reduce max# 1 $args)))", cxt, rcxt);

    defn(core, Sym.throwAritySymCore.getNm(), "throws exception about unmatched arirty; counts first arg; second is data;"
                                              + "(throw-arity $args \"message\")",
         a -> {
           int argC = H.seqFrom(arityRequire(2, a, Sym.throwAritySymCore.getNm()).first()).boundLength(30);
           throw new IllegalArgumentException("Wrong number of args: " + argC
                                              + "; " + a.rest().first() + " //args: " + a.first());
         });

  }

  /**
   * this does not define macros; but namespace for working with macros
   */
  private void loadMacro(Namespace macro) {
    def(macro, Sym.quoteSymC.getNm(), "returns first arg without evaluating it", (c, a) -> a.firstOrNil());
    def(macro, Sym.quoteQualifiedSymC.getNm(), //getNm : names are qualified
        "returns first arg without evaluating it; "
        + "recursively looking for unquote, evaluating those "
        + "and returning them in their original place in the structure",
        (c, a) -> a.isNil() ? H.NIL : H.seqFrom(a.first().unquoteTraverse(c)).firstOrNil());

    def(macro, "expand", "expand macro without evaluating it", (c, a) -> a.firstOrNil().evalMacros(c));
  }

  private static Seq arityRequire(int arity, Seq s, String errMsg) {
    if (s.boundLength(arity) != arity)
      throw new IllegalArgumentException(errMsg + " requires arity: " + arity + " but got: " + s.boundLength(30));
    return s;
  }

  private static void def(Namespace ns, String name, String doc, Sf sf) {
    def(ns, H.symbol(name), doc, sf);
  }

  private static void def(Namespace ns, Symbol name, String doc, Sf sf) {
    ns.def(name, sf, H.map(Sym.docSymK, Str.of(doc)));
  }

  private static void defn(Namespace ns, String name, String doc, Fn fn) {
    defn(ns, H.symbol(name), doc, fn);
  }

  private static void defn(Namespace ns, Symbol name, String doc, Fn fn) {
    ns.def(name, fn, H.map(Sym.docSymK, Str.of(doc)));
  }

  private static void defmacro(Namespace ns, String name, String doc, Macro m) {
    defmacro(ns, H.symbol(name), doc, m);
  }

  private static void defmacro(Namespace ns, Symbol name, String doc, Macro m) {
    ns.def(name, m, H.map(Sym.docSymK, Str.of(doc), Sym.macroSymK, Sym.macroSymK));
  }

}
