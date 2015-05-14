/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import maaj.coll.traits.Counted;
import maaj.coll.traits.Reducible;
import maaj.coll.traits.SeqLike;
import maaj.coll.traits.Seqable;
import maaj.exceptions.InvalidOperationException;
import maaj.reader.ReaderContext;
import maaj.term.*;
import maaj.util.*;

/**
 * loader that does not use file, instead: loads core special forms, macros and functions
 * <p>
 * @author maartyl
 */
public class CoreLoader extends Namespace.Loader {
  /**
   * can load 3 basic namespaces,
   * # : required by everything, contains most basic special forms
   * normal namespace don't start with '#'
   * @param nsName name of namespace to load: #, #core, #macro
   * @param cxt    global context (might not be complete for ns '#')
   * @return some core namespace based on nsName
   */
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
    case "#macro": loadMacro(ns);break;
    default: throw new IllegalArgumentException("no core namespace with name: " + nsName.print());
    }
    return ns;
  }
  /**
   * special forms<br/>
   * called as very first<br/>
   * cannot depend on anything else<br/>
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
    def(core, "def", "(def ^{meta here} name term); create global Var with name 'name and value 'term;"
                     + "the Var is created in current namespace, if already exists, can be changed, but only from current namespace"
                     + "; meta of symbol becomes meta of var", (c, a) -> {
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
    def(core, "recur", "repeat function with new arguments : tail recursion optimized;"
                       + " cannot be used in any other context", (c, a) -> Recur.ofArgs(SeqH.mapEval(a, c)));

    def(core, "require'", "takes symbol with namespace name; then options:\n"
                          + ":* - import all vars from namespace directly\n"
                          + ":as <unqualified symbol> - will be accessible through this instead only original namespace name"
                          + "nothing - imports qualified\n"
                          + "var names - not qualified, reqt qualified, not implemented yet;\n"
                          + "//everything is always import as qualified also", (c, a) -> {
      if (a.isNil()) return H.NIL;
      Symbol nsName = H.requireSymbol(a.first());
      Namespace ns = c.require(nsName);
      if (a.rest().isNil()) {
        c.importFullyQualified(ns);
        return H.NIL;
      }
      Term type = a.rest().first();
      if (type.equals(Sym.asteriskSymK)) {
        c.importNotQualified(ns);
        return H.NIL;
      }
      if (type.equals(Sym.asSymK)) {
        if (a.rest().rest().isNil())
          throw new IllegalArgumentException("require': no symbol after :as clause; //:" + a);
        Symbol asName = H.requireSymbol(a.rest().rest().first());
        if (!asName.isSimple())
          throw new IllegalArgumentException("require': cannot qualify namespace with:" + asName + "; requires: unqualified symbol");
        c.importQualified(ns, asName);
        return H.NIL;
      }
      throw new IllegalArgumentException("require': invalid clause: " + a);
    });
    
  }

  /**
   * transforms 'fn expression into 'fnseq
   * - fn can have : multiple arity overloads,
   * - variadic overload with bounds check
   * ... : see argsBindMacroDispatch
   */
  private Term argsBindMacro(Seq a, Symbol fnType) {
    assert fnType.equals(Sym.fnseqSymC) || fnType.equals(Sym.macroseqSymC);
    if (a.isNil())
      throw new IllegalArgumentException("Cannot bind args withut binding form");
    if (!(a.first().unwrap() instanceof Seq)) //if simple body without overloads: make it 1 overload
      return argsBindMacro(H.list(a.addMeta(a.first().getMeta())), fnType);
    Map fnMeta = a.first().getMeta();
    //passes meta to fnseq / metaseq (InvSeq)
    return H.list(fnType, argsBindMacroDispatch(a).addMeta(fnMeta));
  }
  /**
   * transforms function definition with multiple arity overloads into a function that:<br/>
   * - counts arguments (at most maximal non-variadic arity : works with infinite arg lists)<br/>
   * - creates case statement that branches the function into individual arity bodies<br/>
   * - in else clause of case : generates (with test on minimal length, if needed) variadic overload, if provided<br/>
   * - current implementation: non-variadic options are always tested first
   */
  private Term argsBindMacroDispatch(Seq a) {
    //map "create overload" over args, affregate and summary, compose into case
    Seq data = a.fmap((Invocable1) x -> argsBindOverloadData(x)); //generate overload body, cons arity
    //reduce data((arity . body) ...) into {arity body} ++ :maxArity ++ :variadic
    //it looks terrifying, but without the exception messages it's quite short...
    //large part is just passing around meta for exception information etc.
    Map aritys = (Map) data.reduce(H.map(Sym.maxSymK, Int.of(Integer.MIN_VALUE)), FnH.<Map, Seq, Map>liftTypeUncheched2((m, s) -> {
      Term ar = s.first(); //ar meta contains mainly original pattern for exceptions and messages / ...
      Num arity = (Num) ar.unwrap();
      if (m.containsKey(arity))
        argsBindMacroDispatchArityExists(arity, ar, s, m);
      if (arity.asInteger() < 0) { // variadic arity
        if (m.containsKey(Sym.variadicSymK)) //can only contain 1
          argsBindMacroDispatchVariadicExists(arity, ar, s, m);
        else //set variadic, meta: arity
          return m.assoc(Sym.variadicSymK, s.rest()
                         .addMeta(ar.getMeta()) //retain meta produced in argsBindOverloadData
                         .addMeta(H.map(Sym.aritySymK, //add info about minimal arity for variadic overload
                                        arity.neg().dec())));
      }
      Num maxOld = (Num) m.valAt(Sym.maxSymK).unwrap(); // I will need to know maximal arity
      return H.map(arity, s.rest().addMeta(ar.getMeta()),
                   Sym.maxSymK, maxOld.max(arity));
    }));
    Num maxArity = ((Num) aritys.valAt(Sym.maxSymK)); //extract computed max arity from aritys map
    //I just realized ... I'm only using aritys for getting variadic and max (+ checks...)
    //-(WRONG>>) it deson't even need to store the arities and bodies... (it needs for the checks ... at least arities)

    if (maxArity.asInteger() == Integer.MIN_VALUE) {
      //either : no overloads or only variadic
      //there must be at least 1 overload : can only be 1 variadic function
      //this means : simple function with 1 variadic overload
      assert aritys.containsKey(Sym.variadicSymK) : "expected 1 variadic overload but not present: " + a + ", " + aritys;
      Term body = aritys.valAt(Sym.variadicSymK);
      return  argsBindArityDispatchVariadic(body, a);
    }
    //get only non-variadic overloads, in the order of declarations
    Seq posData = SeqH.filter(data, x -> H.wrap(((Num) ((SeqLike) x).first().unwrap()).asInteger() >= 0));
    //what to do if no overload matched actuall arity
    Seq notMatched = aritys.containsKey(Sym.variadicSymK) ? //try using variadic, or just throw
             argsBindArityDispatchVariadic(aritys.valAt(Sym.variadicSymK), a) :
                     H.list(Sym.throwAritySymCore, Sym.argsSymSpecial, H.list(Sym.quoteSymC, a));
    Seq els = H.list(Sym.ignoreSym, notMatched); //the pair : (_ (notMatched...))
    //each overload is list: (arity body) : almost what I want : just concat and add the else cause
    Seq allCases = SeqH.concatLazy(H.list(SeqH.concatLazy(posData), els));
    //prepend (case (compute arity) ...
    Seq allWithCase = H.cons(Sym.caseSymCore, H.list(Sym.countPrimeSymCore, maxArity, Sym.argsSymSpecial), allCases);
    return allWithCase;
  }

  private void argsBindMacroDispatchVariadicExists(Num arity, Term ar, Seq s, Map m) throws IllegalArgumentException {
    throw new IllegalArgumentException(
            "Multiple variadic overloads (" + arity.neg().dec() + "+): "
            + ar.getMeta(Sym.patternSym) + s.rest() + " and "
            + m.valAt(Sym.variadicSymK).getMeta(Sym.patternSym) + m.valAt(Sym.variadicSymK));
  }

  private void argsBindMacroDispatchArityExists(Num arity, Term ar, Seq s, Map m) throws IllegalArgumentException {
    throw new IllegalArgumentException(
            "Arity overload clash: " + arity + ": " + ar.getMeta(Sym.patternSym) + s.rest()
            + " and " + m.valAt(arity).getMeta(Sym.patternSym) + m.valAt(arity));
  }

  private Seq argsBindArityDispatchVariadic(Term body, Term origData) {
    //body is already expanded into let : doing it again only "deletes" it
    /*argsBindMacroLet : already done when computing meta*/
    Num minArity = (Num) body.getMeta().valAt(Sym.aritySymK);
    if (minArity.asInteger() == 0) //can take any arity: don't generate check
      return SeqH.extend(H.seqFrom(body));
    // `(~fnType (if (< (count' ~minArity $args) ~minArity) (throw-arity $args ~a) ~@(seq body))
    return (SeqH.extend(H.list(Sym.ifSymC,
                                     H.list(Sym.LTSymCore,
                                      H.list(Sym.countPrimeSymCore, minArity, Sym.argsSymSpecial),                                            minArity),
                               H.list(Sym.throwAritySymCore, Sym.argsSymSpecial, H.list(Sym.quoteSymC, origData)),
                               H.seqFrom(body))));
  }

  private Seq argsBindOverloadData(Term t) {
    if (!(t.unwrap() instanceof Seq))
      throw new InvalidOperationException("Overload is not a seq: " + t);
    Seq s = (Seq) t.unwrap();
    if (s.isNil())
      throw new IllegalArgumentException("Cannot bind args withut binding form");
    int arity = argsBindPatternArity(s.first());
    //originally: {:arity arity :body (argsBindLet s)}
    //but (cons arity body) is enough... (anything else needed is in meta of arity)
    //return H.map(Sym.aritySymK, H.wrap(arity), Sym.bodySymK, argsBindMacroLet(s));
    //this creates ~sort of 'case cases right away (some might my negative and stuff though...)
    return H.list(H.wrap(arity).addMeta(H.map(Sym.patternSym, s.first())), argsBindMacroLet(s));
  }

  /**
   * negative means variadic arity<br/>
   * - the number means : "at least"<br/>
   * -- but -1: otherwise : I would get 0 for: [& r]<br/>
   * -- that returns -1<br/>
   * -- [ a b c & r ] -> -4<br/>
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

  /**
   * (pattern body body body...) -> (let [~pb $args] body body body)
   */
  private Seq argsBindMacroLet(Seq a) {
    if (a.isNil())
      throw new IllegalArgumentException("Cannot bind args withut binding form");
    Term ptrn = a.first();
    Seq body = a.rest();
    if (body.isNil())
      // nothing would use the pattern anyway...
      // : empty body : just return nil
      return (H.END);
    //create pattern binder with meta of original pattern + add the pattern in case it would be needed
    Term pb = patternBinder(ptrn).addMeta(ptrn.getMeta()).addMeta(Sym.patternSym, ptrn);
    return (H.cons(Sym.letSymC, H.cons(H.tuple(pb, Sym.argsSymSpecial), body)));//(let [~pb $args] ~@body)
  }

  /**
   * multiple conditional divergences of path : like if with multiple tests<br/>
   * evaluates first body after successful test; not evaluating anything after that<br/>
   * - throw exception if not even number of arguments<br/>
   * i.e. (cond test1 body1 test2 body2 test3 body3)<br/>
   * (cond) returns nil<br/>
   * impl: (cond test body ^rest^) -> (if test body (cond ^rest^))
   * <p>
   * this could be defined in the language; sure : but I need it for arity dispatch<br/>
   * and prefer not to do it without it<br/>
   * - but more importantly: it requires me to throw IllegalArgument ... can't do that yet<br/>
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
   * like Java switch: evaluates expression once and then matches it with options<br/>
   * _ is equal to default, but need not to be last ... everything afterwards is ignored, though<br/>
   * (case (eval-exp) 1 body1 2 body2 45 body3)<br/>
   * is defined in terms of cond and .equals()<br/>
   * - no matching: returns nil<br/>
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
   * (used in 'def special form :) checks validity of ~most cases and
   * throws on any problem; only returns if nu is a valid Symbol;
   * <p>
   * @returns ((Symbol)nu).isQualified
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
   * Creates pattern binder;
   * produces function that takes 1 arg.<br/>
   * it destructures the argument into subparts that are named in term by structure in term;
   * returns map from these names to subvalues of arg;
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
   * zips vector pattern with args seq using function: invoke first on second<br/>
   * - producing seq of maps<br/>
   * then reduces the entire thing into 1 map of resulting (symbol -> "matched")<br/>
   * if seq of args is too short : is extended with Nil-s (so all symbols in pattern are in context defined)
   */
  private Map applyVectorPatternBinder(Term term, Vec ptrn) {
    return (Map) SeqH.zipl(FnH.invoke1(), ptrn.seq(), H.seqFrom(term))
            .reduce(MapH.emptyPersistent(), FnH.<Map, Map, Map>liftTypeUncheched2(MapH::update));
  }

  private Map applyPatternBinder(Invocable binder, Term t) {
    return (Map) binder.invoke(t);
  }

  /**
   * extends def for: defn, defmacro
   * - manages metadata used in defining functions and defining their vars
   */
  private Seq defComposeMacro(Seq a, Symbol fnType) {
    assert fnType.equals(Sym.fnSymCore) || fnType.equals(Sym.macroSymCore);
    String defName = fnType.equals(Sym.fnSymCore) ? "defn" : "defmacro";
    if (a.isNil())
      throw new IllegalArgumentException(defName + " requires name and bindng forms; got: no arguments");
    if (a.rest().isNil())
      throw new IllegalArgumentException(defName + " requires name and bindng forms; got: " + a);
    Term fst = a.first();
    Term snd = a.rest().first();
    //I need meta for : getting name inside the function; writing documentation, ...
    Term name = fst.addMeta(snd.getMeta()).addMeta(fst.getMeta());// I need both to have all meta, if have any clashing keys
    Term fnStart = snd.addMeta(fst.getMeta()).addMeta(snd.getMeta()); // then I want theirs to be preffered
    //meta data on name will be procesed by def
    //-"- on fnStart -"- by fn / macro
    if (fnType.equals(Sym.macroSymCore)) //var should know it holds macro, not 'normal' value
      name = name.addMeta(Sym.macroMapTag);
    fnStart = fnStart.addMeta(Sym.nameSym, name);
    Term aMeta = a/*.addMeta(fnStart.getMeta())*/.addMeta(Sym.typeSymK, fnType);
    fnStart = fnStart.addMeta(Sym.srcSymK, aMeta);
    name = name.addMeta(Sym.srcSymK, aMeta);

    return H.list(Sym.defSymC, name, H.cons(fnType, fnStart, a.rest().rest()));
  }

  /**
   * loads the #core namespace : functions and macros tied with the language; "imported" into every namespace
   */
  private void loadCore(Context cxt, Namespace core, ReaderContext rcxt) {
    defmacro(core, "fn", "creates function that binds args", a -> argsBindMacro(a, Sym.fnseqSymC));
    defmacro(core, "macro", "creates macro that binds args", a -> argsBindMacro(a, Sym.macroseqSymC));

    defmacro(core, "defn", "creates and defs a function; combines def and fn", a
             -> defComposeMacro(a, Sym.fnSymCore));
    defmacro(core, "defmacro", "creates and defs a macro; combines def and macro", a
             -> defComposeMacro(a, Sym.macroSymCore));

    defmacro(core, "cond", "Takes pairs of: (test body); evaluates only body after first successful test;\n"
                           + "example: (cond (0 < v) :pos (0 > v) :neg :else :zero", this::condMacro);
    defmacro(core, "case", "Takes expr pairs (match body); works like cond with =#; evaluates expr once", this::caseMacro);
    defn(core, "gensym", "returns unique* symbol (* in one run of program)", a -> {
      switch (a.boundLength(1)) {
      case 0: return H.uniqueSymbol();
      case 1: return H.uniqueSymbol(H.requireSymbol(a.first()));
      default: throw new IllegalArgumentException("gensym requires arity: 0 | 1 but got: " + a.boundLength(30));
      }
    });

    //I would format it normally but NetBeans won't let me...
    defmacro(core, "require", "imports namespaces", a
             -> H.cons(Sym.doSymC, SeqH.mapSexp(a, x
                                                -> x.unwrap() instanceof Seqable ?
                                                   H.cons(Sym.requirePrimeSymC, H.seqFrom(x)) :
                                                   H.list(Sym.requirePrimeSymC, x))));

    defn(core, "meta", "get meta data of term; (meta term) ->{...}; (meta :key term) ~= (:key (meta term))", a
         -> a.isNil() ? H.NIL.getMeta() :
            (a.rest().isNil() ? a.first().getMeta() :
             a.rest().first().getMeta(a.first().unwrap())));
    defn(core, Sym.firstSym.getNm(), "first of seq (head)", a -> H.seqFrom(arityRequire(1, a, "first").first()).firstOrNil());
    defn(core, Sym.restSym.getNm(), "rest of seq (tail)", a -> H.seqFrom(arityRequire(1, a, "rest").first()).restOrNil());
    defmacro(core, "car", "first of seq (head)", a -> H.cons(Sym.firstSym, a));
    defmacro(core, "cdr", "rest of seq (tail)", a -> H.cons(Sym.restSym, a));
    defmacro(core, "cadr", "(first (rest a))", a -> H.list(Sym.firstSym, H.cons(Sym.restSym, a)));
    defmacro(core, "cddr", "(rest (rest a))", a -> H.list(Sym.restSym, H.cons(Sym.restSym, a)));
    defn(core, "seq", "seq from collection", a -> H.seqFrom(arityRequire(1, a, "seq").first()));
    defn(core, "count", "number of elements in collection; possibly O(N)", a
         -> H.requireNumerable(arityRequire(1, a, "count")).count());
    defn(core, "count'", "number of elements in [2nd arg] collection; O(1), possibly incorrect; "
                         + "if counts, returns maximally [1st arg] specified value"
                         + "(count' 5 (100)) -> Int.MaxValue;"
                         + "(count' 2 [7 8 9 7]) -> 4", a -> {
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

    defn(core, "reduce", "applies ^1 fn on (^2 accumulator and first element in ^3 coll)"
                         + " producing new accumulator, appling on second ...; returns final accumulator", a -> {
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

    defn(core, "lazy'", "creates seq thunk from an invocable argument: must return a seq", a
         -> H.lazy(H.requireInvocable(arityRequire(1, a, "lazy'").first())));

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

    H.eval("(defn + ^\"sums numeric arguments; (+) -> 0\"        a (reduce +# 0 a))", cxt, rcxt);
    H.eval("(defn * ^\"product of numeric arguments; (*) -> 1\"  a (reduce *# 1 a))", cxt, rcxt);
    H.eval("(defn - ^\"negates only argument or substracts others from first\""
           + "([x] (neg x))   ([x & a] (reduce -# x a)))", cxt, rcxt);
    H.eval("(defn / ^\"multiplicative inverse of only arg or divides first by all following\""
           + "([x] (/ 1 x))   ([x & a] (reduce div# x a)))", cxt, rcxt);
    H.eval("(defn min ^\"selects minimal of arguments\""
           + "([x] x)     ([x & a] (reduce min# x a)))", cxt, rcxt);
    H.eval("(defn max ^\"selects maximal of arguments\""
           + "([x] x)     ([x & a] (reduce max# x a)))", cxt, rcxt);

    H.eval("(defmacro and ^\"logical and\"([x] x)([]'t) ([x & y] (let [a (gensym)] " //implementation ~taken from clojure
           + "        `(let [~a ~x] (if ~a (~and ~@y) ~a))    )))", cxt, rcxt);
    H.eval("(defmacro or  ^\"logical and\"([x] x)([]()) ([x & y] (let [a (gensym)] "
           + "        `(let [~a ~x] (if ~a ~a (~or ~@y)))    )))", cxt, rcxt);

    H.eval("(defn = ^\"true iff all arguments are equal\"([x y] (=# x y)) ([x] x) "
           + "      ([x y & a] (and (= x y) (apply = y a))))", cxt, rcxt);

    H.eval("(defmacro lazy ^\"postpones evaluation of argument and returns seq thunk; body must evaluate into seq; "
           + "if 2 arguments given ~= (cons fst-arg (lazy snd-arg))\""
           + "([x] `(lazy' (#/fnseq ~x))) ([h t] `(cons ~h (lazy ~t)) )  )", cxt, rcxt);

    defn(core, Sym.throwAritySymCore.getNm(), "throws exception about unmatched arirty; counts first arg; second is data;"
                                              + "(throw-arity $args \"message\")",
         a -> {
           int argC = H.seqFrom(arityRequire(2, a, Sym.throwAritySymCore.getNm()).first()).boundLength(30);
           throw new IllegalArgumentException("Wrong number of args: " + argC
                                              + "; " + a.rest().first() + " //args: " + a.first());
         });
  }

  /**
   * this does not define macros; but namespace for working with macros : #macro
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
