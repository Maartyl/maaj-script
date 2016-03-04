/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import java.util.function.BiFunction;
import java.util.function.Function;
import maaj.coll.traits.AssocUpdate;
import maaj.coll.traits.AssocUpdateT;
import maaj.coll.traits.Counted;
import maaj.coll.traits.Deref;
import maaj.coll.traits.Dissoc;
import maaj.coll.traits.DissocT;
import maaj.coll.traits.Functor;
import maaj.coll.traits.Growable;
import maaj.coll.traits.GrowableT;
import maaj.coll.traits.Numerable;
import maaj.coll.traits.Peekable;
import maaj.coll.traits.SeqLike;
import maaj.coll.traits.Seqable;
import maaj.coll.traits.TraPer;
import maaj.exceptions.InvalidOperationException;
import maaj.reader.ReaderContext;
import maaj.term.*;
import maaj.util.*;

/**
 * loader that does not use file, instead: loads core special forms, macros and functions
 * <p>
 * @author maartyl
 */
public class CoreLoader extends NamespaceNormal.Loader {
  /**
   * can load 4 basic namespaces,
   * # : required by everything, contains most basic special forms
   * normal namespace don't start with '#'
   * @param nsName name of namespace to load: #, #core, #macro, #jvm
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
    case "#core": loadCore(c, ns, new ReaderContext(nsName, "<#core>"));
      break;
    case "#macro": loadMacro(ns);
      break;
    case "#jvm": loadJvmInterop(ns);
      break;
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
    def(core, "if", "evaluates first argument; if non-nil evaluates and returns second;"
                    + "if nil: if third argument is present: evaluates and returns it; otherwise nil", (c, a) -> {
      int len = a.boundLength(3);
      if (len == 2 || len == 3)
        return (H.bool(a.first().eval(c)) ? a.rest() : a.rest().rest()).firstOrNil().eval(c);
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
//      if (nu instanceof Var) {
//        Var v = (Var) nu;
//        if (v.getMeta(Sym.namespaceSym).equals(c.getCurNs().getName())) {
//          v.doSet(val);
//          return v;
//        } else {
//          throw new InvalidOperationException("#/def: cannot change a var from a different namespace:" + nu.print());
//          //nu = v.getMeta(Sym.nameSym);
//        }
//      }
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
      return H.cons(Sym.doSymC, a.rest()).eval(letEvalBindings(c, (Vec) a.first().unwrap()));
    });
    def(core, "fnseq", "(fnseq body body $args body)", (c, a) -> FnSeq.of(a, c));
    def(core, "macroseq", "(macroseq body body $args body)", (c, a) -> MacroSeq.of(a, c));
    def(core, "eval", "evaluates given term", (c, a) -> arityRequire(a, "eval", t -> t.eval(c).eval(c)));
    def(core, "apply", "applies first argument on [last argumet (seq) with other arguments prepended]; "
                       + "(apply + 7 8 [4 5 6]) -> (+ 7 8 4 5 6)",
        (c, a) -> a.isNil() ? H.NIL : SeqH.extend(SeqH.mapEval(a, c)).eval(c));
    def(core, "recur", "repeat function with new arguments : tail recursion optimized;"
                       + " cannot be used in any other context", (c, a) -> Recur.ofArgs(SeqH.mapEval(a, c)));

    def(core, "var", "gets Var itself associated with symbol; not the value in Var",
        (c, a) -> arityRequire(a, "var", sym -> c.getVar(H.requireSymbol(sym.eval(c)))));

    def(core, "require'", "takes symbol with namespace name; then options:\n"
                          + ":* - import all vars from namespace directly\n"
                          + ":as <unqualified symbol> - will be accessible through this instead only original namespace name"
                          + "nothing - imports qualified\n"
                          + "var names - not qualified, reqt qualified, not implemented yet;\n"
                          + "//everything is always also imported as qualified", (c, a) -> {
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

    def(core, "io!", "runs given IO monad, returning it's final value",
        (c, a) -> arityRequire(a, "io!", arg -> H.requireIO(arg.eval(c)).run(c)));

    //--- optimizers

    core.get(Sym.ifSymC).addMeta(Sym.optimizerSymK, (InvocableSeq) s -> {
      switch (s.boundLength(3)) {
      case 2:
      case 3:
        Term test = s.first();
        Term onTrue = s.rest().first();
        Term onFalse = s.rest().rest().firstOrNil();
        if (test.isNil()) //nil will surely evaluate to nil
          return onFalse;
        //if (H.bool(H.isKeyword(test))) //is Ground too
        //  return onTrue;
        if (H.bool(H.isGround(test))) //ground will surely evaluate to itself
          return onTrue;

        //no branching (I don't want to test on full equality big subtrees, though)
        // unnecessary stuff is not necessarily evaluated (unless it's IO)
//        if (onTrue.isNil() && onFalse.isNil()) {
//          if (H.bool(H.isSymbol(test))) //evaluating symbol does not have side-effects
//            return H.NIL;
//          // return H.list(Sym.doSymC, test, H.NIL);
//          return H.list(Sym.doSymC, test, H.NIL);
//        } //IT happens so rarely, it's probably pointless to test: costs more than possible gain
      }
      return H.cons(Sym.ifSymC, s);
    });

    core.get(Sym.doSymC).addMeta(Sym.optimizerSymK, (InvocableSeq) s -> {
      if (SeqH.isSingle(s))
        return s.first();
      if (s.isNil()) //empty 'do evaluates to ()
        return H.NIL;
      return H.cons(Sym.doSymC, s);
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
    Map fnMeta = a.first().getMeta().assoc(Sym.formSymSpecial, a);
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
    /////I just realized ... I'm only using aritys for getting variadic and max (+ checks...)
    /////it deson't even need to store the arities and bodies...-(WRONG>>) (it needs for the checks ... at least arities)

    if (maxArity.asInteger() == Integer.MIN_VALUE) {
      //either : no overloads or only variadic
      //there must be at least 1 overload : can only be 1 variadic function
      //this means : simple function with 1 variadic overload
      assert aritys.containsKey(Sym.variadicSymK) : "expected 1 variadic overload but not present: " + a + ", " + aritys;
      Term body = aritys.valAt(Sym.variadicSymK);
      return  argsBindArityDispatchVariadic(body);
    }
    //get only non-variadic overloads, in the order of declarations
    Seq posData = SeqH.filter(data, x -> H.wrap(((Num) ((SeqLike) x).first().unwrap()).asInteger() >= 0));
    //what to do if no overload matched actuall arity
    Seq notMatched = aritys.containsKey(Sym.variadicSymK) ? //try using variadic, or just throw
             argsBindArityDispatchVariadic(aritys.valAt(Sym.variadicSymK)) :
            H.list(Sym.throwAritySymCore, Sym.argsSymSpecial, Sym.formSymSpecial);
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

  private Seq argsBindArityDispatchVariadic(Term body) {
    //body is already expanded into let : doing it again only "deletes" it
    /*argsBindMacroLet : already done when computing meta*/
    Num minArity = (Num) body.getMeta().valAt(Sym.aritySymK);
    if (minArity.asInteger() == 0) //can take any arity: don't generate check
      return SeqH.extend(H.seqFrom(body));
    // `(~fnType (if (< (count' ~minArity $args) ~minArity) (throw-arity $args ~a) ~@(seq body))
    return (SeqH.extend(H.list(Sym.ifSymC,
                                     H.list(Sym.LTSymCore,
                                      H.list(Sym.countPrimeSymCore, minArity, Sym.argsSymSpecial),                                            minArity),
                               H.list(Sym.throwAritySymCore, Sym.argsSymSpecial, Sym.formSymSpecial),
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
                           + "example: (cond (0 < v) :pos (0 > v) :neg :else :zero)", this::condMacro);
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
    defnArity(core, "deref", "dereferences argument (for cells and boxes)", H::requireDeref, Deref::deref);

    defn(core, "meta", "get meta data of term; (meta term) ->{...}; (meta :key term) ~= (:key (meta term))", a
         -> a.isNil() ? H.NIL.getMeta() :
            (a.rest().isNil() ? a.first().getMeta() :
             a.rest().first().getMeta(a.first().unwrap())));
    defnArity(core, Sym.firstSym.getNm(), "first of seq (head)", H::seqFrom, Seq::firstOrNil);
    defnArity(core, Sym.restSym.getNm(), "rest of seq (tail)", H::seqFrom, Seq::restOrNil);
    defmacro(core, "car", "first of seq (head)", a -> H.cons(Sym.firstSym, a));
    defmacro(core, "cdr", "rest of seq (tail)", a -> H.cons(Sym.restSym, a));
    defmacro(core, "cadr", "(first (rest a))", a -> H.list(Sym.firstSym, H.cons(Sym.restSym, a)));
    defmacro(core, "cddr", "(rest (rest a))", a -> H.list(Sym.restSym, H.cons(Sym.restSym, a)));
    defnArity(core, "seq", "seq from collection", H::seqFrom);

    defnArity(core, "count", "number of elements in collection; possibly O(N)", H::requireNumerable, Numerable::count);
    defnArity(core, "count'", "number of elements in ^2 collection; O(1), possibly incorrect; "
                              + "if counts, returns maximally ^1 specified value; "
                              + "(count' 5 (100)) -> Int.MaxValue; "
                              + "(count' 200 (100)) -> 100; "
                              + "(count' 2 [7 8 9 7]) -> 4",
              H::requireNum, Term::unwrap, (max, coll) -> {
      if (coll instanceof Counted)
        return ((Counted) coll).getCount();
      return H.wrap(H.seqFrom(coll).boundLength(max.asInteger()));
            });

    defnArity(core, ">>=", "monadic bind: (>>= m #(... % ...))", H::requireMonad, H::requireInvocable, Monad::bindM);
    defnArity(core, "retM", "monadic return (first arg is monad of desired type): (retM m a)", H::requireMonad, FnH::id, Monad::retM);
    defnArity(core, "retIO", "retM of type IO: (retIO a)", IO::retM1);

    defnArity(core, "invocable?", "itself if invocable, nil otherwise", H::isInvocable);
    defnArity(core, "symbolic?", "itself if symbolic, nil otherwise", H::isSymbolic);
    defnArity(core, "keyword?", "itself if keyword, nil otherwise", H::isKeyword);
    defnArity(core, "symbol?", "itself if symbol, nil otherwise", H::isSymbol);
    defnArity(core, "sym?", "same as symbol?", H::isSymbol);
    defnArity(core, "kw?", "same as keyword?", H::isKeyword);

    defnArity(core, "string?", "itself if string, nil otherwise", H::isStr);
    defnArity(core, "str?", "same as string?", H::isStr);

    defnArity(core, "num?", "itself if number, nil otherwise", H::isNum);
    defnArity(core, "char?", "itself if char, nil otherwise", H::isChar);
    defnArity(core, "int?", "itself if int, nil otherwise", H::isInt);
    defnArity(core, "dbl?", "itself if dbl, nil otherwise", H::isDbl);

    defnArity(core, "monad?", "itself if monad, nil otherwise", H::isMonad);
    defnArity(core, "io?", "itself if IO, nil otherwise", H::isIO);

    defnArity(core, "coll?", "itself if coll, nil otherwise", H::isCollection);
    defnArity(core, "vec?", "itself if vec, nil otherwise", H::isVec);
    defnArity(core, "map?", "itself if map, nil otherwise", H::isMap);
    defnArity(core, "seq?", "itself if seq, nil otherwise; empty seq is also nil", H::isSeq);

    defnArity(core, "nil?", "(if % () 't)", val -> H.wrap(val.isNil()));
    defnArity(core, "not", "(if % () 't)", val -> H.wrap(val.isNil()));

    defnArity(core, "cons", "prepends to list; O(1)", FnH::id, H::seqFrom, H::cons);

    defnArity(core, "peek", "retrieves last conj-ed element", H::requirePeekable, Peekable::peek);

    defnArity(core, "conj&", "adds to cellection; where depends on collection", H::requireGrowable, FnH::id, Growable::conj);
    defnArity(core, "assoc&", "update collection at given key with given value",
              H::requireAssocUpdate, FnH::id, FnH::id, AssocUpdate::assoc);
    defnArity(core, "dissoc&", "remove from ^1 collection at given ^2 key", H::requireDissoc, FnH::id, Dissoc::dissoc);

    defnArity(core, "transient", "transient version of ^1 collection", H::requireTraPer, TraPer::asTransient);
    defnArity(core, "persistent!", "freeze ^1 transient to behave like persistent", H::requireTraPer, TraPer::asPersistent);

    defnArity(core, "conj!&", "adds to cellection; where depends on collection", H::requireGrowableT, FnH::id, GrowableT::doConj);
    defnArity(core, "assoc!&", "update collection at given key with given value",
              H::requireAssocUpdateT, FnH::id, FnH::id, AssocUpdateT::doAssoc);
    defnArity(core, "dissoc!&", "remove from ^1 collection at given ^2 key", H::requireDissocT, FnH::id, DissocT::doDissoc);


    defnArity(core, "reduce", "applies ^1 fn on (^2 accumulator and first element in ^3 coll)"
                              + " producing new accumulator, appling on second ...; returns final accumulator",
              H::requireInvocable, FnH::id, H::requireReducible, (fn, start, coll) -> {
      if (coll instanceof SeqLike)
        return SeqH.reduce((SeqLike) H.ret1(coll, coll = null), H.ret1(start, start = null), fn);
      //realization: it's impossible anyway: virtual methods (eval) prevent GC of their object...
      
      //this retains entire coll in memory while reducing
      return coll.reduce(start, fn);
    });

    defn(core, "map", "lazily maps ^1 fn over 1 to 3 seqs - fn has to be of the same arity as number of seqs", a -> {
      switch (a.boundLength(4)) {
      case 2: return SeqH.mapLazy(H.seqFrom(a.rest().first()), H.requireInvocable(a.first()));
      case 3: return SeqH.zip(H.requireInvocable(a.first()),
                              H.seqFrom(a.rest().first()),
                              H.seqFrom(a.rest().rest().first()));
      case 4: return SeqH.zip(H.requireInvocable(a.first()),
                              H.seqFrom(a.rest().first()),
                              H.seqFrom(a.rest().rest().first()),
                              H.seqFrom(a.rest().rest().rest().first()));
      default:
        throw new IllegalArgumentException("map requires arity: 2|3|4 but got: " + a.boundLength(30));
      }
    });

    defnArity(core, "fmap", "applies ^2 funtion to a ^1 functor", H::requireFunctor, H::requireInvocable, Functor::fmap);

    defnArity(core, Sym.equalSymCCore.getNm(), "equals?", (l, r) -> H.wrap(l.equals(r)));

    defnArity(core, "lazy'", "creates seq thunk from an invocable argument: must return a seq", H::requireInvocable, H::lazy);

    defnArity(core, "take", "takes first ^1 n elements of a ^2 seq", H::requireNum, H::seqFrom, SeqH::take);

    defn(core, "+&", "adds 2 args", (Num.Num2Op) Num::add);
    defn(core, "-&", "subtracts arg1 from arg0", (Num.Num2Op) Num::sub);
    defn(core, "*&", "multiplies 2 args", (Num.Num2Op) Num::mul);
    defn(core, "div&", "divides arg1 by arg0", (Num.Num2Op) Num::div);
    defn(core, "min&", "(if (< l r) l r)", (Num.Num2Op) Num::min);
    defn(core, "max&", "(if (> l r) l r)", (Num.Num2Op) Num::max);

    defn(core, "inc", "(+ % 1)", (Num.NumOp) Num::inc);
    defn(core, "dec", "(- % 1)", (Num.NumOp) Num::dec);
    defn(core, "neg", "(- 0 %)", (Num.NumOp) Num::neg);

    defn(core, "<", "Num; is first arg less then second?", (Num.NumPred) (Num::lt));
    defn(core, ">", "Num; is first arg greater then second?", (Num.NumPred) (Num::gt));
    defn(core, "==", "Num; is first arg equal to second?", (Num.NumPred) (Num::eq));
    defn(core, "<=", "Num; is first arg less then or equal to second?", (Num.NumPred) (Num::lteq));
    defn(core, ">=", "Num; is first arg greater then or equal to second?", (Num.NumPred) (Num::gteq));

    H.evalAll(
            ""
            + "(defn + ^\"sums numeric arguments; (+) -> 0\"        a (reduce +& 0 a))"
            + "(defn * ^\"product of numeric arguments; (*) -> 1\"  a (reduce *& 1 a))"
            + "(defn - ^\"negates only argument or substracts others from first\""
            + "  ([x] (neg x))"
            + "  ([x & a] (reduce -& x a)))"
            + "(defn / ^\"multiplicative inverse of only arg or divides first by all following\""
            + "  ([x] (/ 1 x))"
            + "  ([x & a] (reduce div& x a)))"
            + "(defn min ^\"selects minimal of arguments\""
            + "  ([x] x)"
            + "  ([x & a] (reduce min& x a)))"
            + "(defn max ^\"selects maximal of arguments\""
            + "  ([x] x)"
            + "  ([x & a] (reduce max& x a)))"
            + "\n"
            + "(defmacro and ^\"logical and; returns last non-nil\""
            + "  ([x] x)"
            + "  ([]'t)"
            + "  ([x & y]" //implementation ~copied from clojure
            + "    `(let [a# ~x] (if a# (~and ~@y) a#))    ))"
            + "(defmacro or  ^\"logical or; returns first non-nil\""
            + "  ([x] x)"
            + "  ([]())"
            + "  ([x & y]"
            + "    `(let [a# ~x] (if a# a# (~or ~@y)))    ))"
            + "\n"
            + "(defmacro = ^\"true iff all arguments are equal\""
            + "  ([x y] `(=& ~x ~y)) "
            + "  ([_] ''t) "
            + "  ([x y & a] `(and (= ~x ~y) (= ~y ~@a))))"
            + "\n"
            + "(defn list ^\"returns list of evaluated arguments\""
            + "  as as)"
            + "(defn id ^\"returns it's first argument\""
            + "  [x] x)"
            + "\n"
            + "\n"
            + "(defmacro lazy ^\"postpones evaluation of argument and returns seq thunk; body must evaluate into seq; "
            + "if 2 arguments given ~= (cons fst-arg (lazy snd-arg))\""
            + "  ([x] `(lazy' (#/fnseq ~x)))"
            + "  ([h t] `(cons ~h (lazy ~t))))"
            + "\n"
            + "(defn conj ^\"conjoins 1 or more ^2 terms to a ^1 collection\""
            + "  ([coll x] (conj& coll x)) ([coll & r] (reduce conj& coll r)))"
            + "(defn assoc ^\"[associative key val] associate key with val in map; there can be multiple key val pairs\""
            + "  ([c k v] (assoc& c k v)) ([c k v & r] (apply recur (assoc& c k v) r)))"
            + "(defn dissoc ^\"removes 1 or more kv by ^2 key from a ^1 map\""
            + "  ([coll x] (dissoc& coll x)) ([coll & r] (reduce dissoc& coll r)))"
            + "(defn conj! ^\"conjoins 1 or more ^2 terms to a transient ^1 collection\""
            + "  ([coll x] (conj!& coll x)) ([coll & r] (reduce conj!& coll r)))"
            + "(defn assoc! ^\"[associative key val] associate key with val in transient map; there can be multiple key val pairs\""
            + "  ([c k v] (assoc!& c k v)) ([c k v & r] (apply recur (assoc!& c k v) r)))"
            + "(defn dissoc! ^\"removes 1 or more kv by ^2 key from a ^1 transient map\""
            + "  ([coll x] (dissoc!& coll x)) ([coll & r] (reduce dissoc!& coll r)))"
            + "\n"
            + "\n"
            + "(defmacro when ^\"if ^1, evaluates ^&2\""
            + "  [test & body] `(if ~test (do ~@body)))"
            + "(defmacro unless ^\"if not ^1, evaluates ^&2; when with negated test\""
            + "  [test & body] `(when (not ~test) ~@body))"
            + "\n"
            + "(defmacro when-let ^\"like when, but binds resulting value if non-nil\""
            + "  [bind & body]                                            \n"
            + "  (unless (vec? bind)         (throw-arg \"when-let: binds has to be a vector\"))\n"
            + "  (unless (== 2 (count bind)) (throw-arg \"when-let: binds requires exactly 1 binding pair\"))\n"
            + "  (let [[b val] bind]                                      \n"
            + "    `(let [tmp# ~val]                                      \n"
            + "       (when tmp# (let [~b tmp#] ~@body)))))               \n"
            + "\n"
            + "\n"
            + "(defmacro for ^\"monadic composition block; like let, but instead of binding results composes monads.\"\n"
            + "  ([binds body]\n"
            + "   (unless (vec? binds)         (throw-arg \"for: binds has to be a vector\"))\n"
            + "   (unless (<= 2 (count binds)) (throw-arg \"for: too few binds: \" binds))\n"
            + "   (let [[b m & br] binds]\n"
            + "     (case b\n"
            + "       :let `(let ~m (for ~br ~body)) ; in case it starts with let\n"
            + "       _    `(let [m# ~m  ; only evaluate m once\n"
            + "                   ret# (macro [arg#] (list `retM m# arg#))]\n"
            + "               (for ret# [~b m# ~@br] ~body))))) \n"
            + "  ([ret binds body]\n"
            + "   (case (count' 1 binds)\n"
            + "     0 (list ret body)\n"
            + "     1 (throw-arg \"odd binds: \" binds)\n"
            + "     _ (let [[b m & rs] binds]\n"
            + "         (case b\n"
            + "           :let `(let ~m (for ~ret ~rs ~body))\n"
            + "           _ (if (and (= 2 (count' 2 binds)) (= b body) (sym? b))\n"
            + "               m\n"
            + "               `(>>= ~m (fn [~b] (for ~ret ~rs ~body)))))))))\n"
            + ""
            + "(defmacro -> ^\"insert first argument as first argument of second form;"
            + " recursively on all; (-> a (+ 5) k) => (k (+ a 5))\"\n"
            + "  ([x] x)\n"
            + "  ([x form & rx] `(-> ~(if (seq? form)\n"
            + "                         `(~(first form) ~x ~@(rest form))\n"
            + "                         `(~form ~x)) ~@rx)))\n"
            + "(defmacro ->> ^\"insert first argument as last argument of second form;"
            + " recursively on all; (-> a (+ 5) k) => (k (+ 5 a))\"\n"
            + "  ([x] x)\n"
            + "  ([x form & rx] `(-> ~(if (seq? form)\n"
            + "                         `(~(first form) ~@(rest form) ~x)\n"
            + "                         `(~form ~x)) ~@rx)))", cxt, rcxt);

    defnArity(core, Sym.throwAritySymCore.getNm(), "throws exception about unmatched arirty; counts first arg; second is data;"
                                                   + "(throw-arity $args \"message\")", H::seqFrom, FnH::id, (args, msg) -> {
           throw new IllegalArgumentException("Wrong number of args: " + args.boundLength(50)
                                              + "; " + msg + " //args: " + SeqH.take(50, args));
    });

    //lambda
    defmacro(core, "\u03BB", "creates function that binds args (same as fn)", a -> H.cons(Sym.fnSymCore, a));
  }

  /**
   * this does not define macros; but namespace for working with macros : #macro
   */
  private void loadMacro(Namespace macro) {
    defq(macro, Sym.quoteSymC.getNm(), "returns first arg without evaluating it", SfQuoting.SfQuote);
    defq(macro, Sym.quoteQualifiedSymC.getNm(), //getNm : names are qualified
         "returns first arg without evaluating it; "
         + "recursively looking for unquote, evaluating those "
         + "and returning them in their original place in the structure",
         SfQuoting.SfQuoteQualified);
    def(macro, "expand", "expand macro without evaluating it", (c, a) -> a.firstOrNil().evalMacros(c));
  }

  private void loadJvmInterop(Namespace jvm) {
    defnArity(jvm, Sym.invokeVirtualSymInterop.getNm(),
              "Invokes method on object with given arguments. (performs implicit conversions) \n"
              + "[obj methodName args-list]; "
              + "methodName: unqualified symbol. "
              + "args-list: any seqable.",
              FnH::id, H::requireSymbol, H::requireSeqable,
              (obj, name, args) -> {
                if (name.isQualified())
                  throw new IllegalArgumentException("invoke-virtual: method name cannot be qualified.");
                final Object content = obj.getContent();
                if (content == null)
                  throw new IllegalArgumentException("invoke-virtual: object cannot be nil");

                return IO.make(c -> c.getInterop().call(obj.getType(), content, name.getNm(), args.seq()));
              });

    defnArity(jvm, Sym.invokeStaticSymInterop.getNm(),
              "Invokes static method with given arguments. (performs implicit conversions) \n"
        + "[type methodName args-list]; "
        + "type: full class name (unqualified symbol). "
        + "methodName: unqualified symbol. "
              + "args-list: any seqable.",
              H::requireSymbol, H::requireSymbol, H::requireSeqable,
              (type, name, args) -> {
          if (name.isQualified())
            throw new IllegalArgumentException("invoke-static: method name cannot be qualified.");
          if (type.isQualified())
            throw new IllegalArgumentException("invoke-static: type name cannot be qualified.");

                return IO.make(c -> c.getInterop().call(typeRequire(type.getNm(), H.tuple(H.wrap("java.lang"))),
                                                        null, name.getNm(), args.seq()));
    });
    defnArity(jvm, Sym.ctorSymInterop.getNm(),
              "Constructs new object with given arguments. (performs implicit conversions) \n"
        + "[typeName args-list]; "
        + "typeName: unqualified symbol. "
              + "args-list: any seqable.",
              H::requireSymbol, H::requireSeqable,
              (type, args) -> {
          if (type.isQualified())
            throw new IllegalArgumentException("ctor: type name cannot be qualified.");

          return IO.make(c -> c.getInterop().ctor(typeRequire(type.getNm(), H.tuple(H.wrap("java.lang"))), args.seq()));
        });

    defnArity(jvm, Sym.instanceFieldGetSymInterop.getNm(),
              "Get's value of field on an instace.\n"
        + "[obj fieldName]; "
              + "fieldName: unqualified symbol. ",
              FnH::id, H::requireSymbol,
              (obj, tname) -> {
          Symbol name = H.requireSymbol(tname);
          if (name.isQualified())
            throw new IllegalArgumentException("instance-field-get: method name cannot be qualified.");
          final Object content = obj.getContent();
          if (content == null)
            throw new IllegalArgumentException("instance-field-get: object cannot be nil");

          return IO.make(c -> c.getInterop().fieldGet(obj.getType(), content, name.getNm()));
        });
    defnArity(jvm, Sym.staticFieldGetSymInterop.getNm(),
              "Get's value of static field.\n"
        + "[type fieldName]; "
        + "type: full class name (unqualified symbol). "
              + "fieldName: unqualified symbol. ",
              H::requireSymbol, H::requireSymbol,
              (type, name) -> {
          if (name.isQualified())
            throw new IllegalArgumentException("static-field-get: method name cannot be qualified.");
          if (type.isQualified())
            throw new IllegalArgumentException("static-field-get: type name cannot be qualified.");

                return IO.make(c -> c.getInterop().fieldGet(typeRequire(type.getNm(), H.tuple(H.wrap("java.lang"))),
                                                            null, name.getNm()));
        });

    defnArity(jvm, Sym.instanceFieldSetSymInterop.getNm(),
              "Sets's value of field on an instace. (performs implicit conversions) \n"
        + "[obj fieldName value]; "
              + "fieldName: unqualified symbol. ",
              FnH::id, H::requireSymbol, FnH::id,
              (obj, name, value) -> {
          if (name.isQualified())
            throw new IllegalArgumentException("instance-field-set: method name cannot be qualified.");
          final Object content = obj.getContent();
          if (content == null)
            throw new IllegalArgumentException("instance-field-set: object cannot be nil");

          return IO.make(c -> {
            c.getInterop().fieldSet(obj.getType(), content, name.getNm(), value);
            return obj;
          });
          
        });
    defnArity(jvm, Sym.staticFieldSetSymInterop.getNm(),
              "Sets's value of static field. (performs implicit conversions) \n"
        + "[type fieldName value]; "
        + "type: full class name (unqualified symbol). "
              + "fieldName: unqualified symbol. ",
              H::requireSymbol, H::requireSymbol, FnH::id,
              (type, name, value) -> {
          if (name.isQualified())
            throw new IllegalArgumentException("static-field-set: method name cannot be qualified.");
          if (type.isQualified())
            throw new IllegalArgumentException("static-field-set: type name cannot be qualified.");

           return IO.make(c -> {
             Class typeCls = typeRequire(type.getNm(), H.tuple(H.wrap("java.lang")));
             c.getInterop().fieldSet(typeCls, null, name.getNm(), value);
             return H.NIL;
           });
        });
  }

  private static Class typeRequire(String name, Iterable prefixes) {
    //prefixes will be vector of strings or something: special var declared in current namespace //or default
    //TODO: var ^
    Class typeCls;

    if ((typeCls = H.classOrNull(name)) != null) return typeCls; // no prefix

    for (Object po : prefixes) {
      String p = po.toString();
      if ("".equals(p)) continue;
      if (!p.endsWith(".")) //so name can be appended
        p += '.';

      if ((typeCls = H.classOrNull(p + name)) != null) return typeCls;
    }

    try {
      return Class.forName(name);
    } catch (ClassNotFoundException e) {
      throw H.sneakyThrow(e); //no prefix matched
    }
  }

  private static Seq arityRequire(int arity, Seq s, String errMsg) {
    if (s.boundLength(arity) != arity)
      throw new IllegalArgumentException(errMsg + " requires arity: " + arity + " but got: " + s.boundLength(30));
    return s;
  }

  private static Term arityRequire(Seq s, String errMsg, Invocable0 fn) {
    return fn.invokeSeq(arityRequire(0, H.ret1(s, s = null), errMsg));
  }

  private static Term arityRequire(Seq s, String errMsg, Invocable1 fn) {
    return fn.invokeSeq(arityRequire(1, H.ret1(s, s = null), errMsg));
  }

  private static Term arityRequire(Seq s, String errMsg, Invocable2 fn) {
    return fn.invokeSeq(arityRequire(2, H.ret1(s, s = null), errMsg));
  }

  private static Term arityRequire(Seq s, String errMsg, Invocable3 fn) {
    return fn.invokeSeq(arityRequire(3, H.ret1(s, s = null), errMsg));
  }

  private static Term arityRequire(Seq s, String errMsg, Invocable4 fn) {
    return fn.invokeSeq(arityRequire(4, H.ret1(s, s = null), errMsg));
  }

  private static Term arityRequire(Seq s, String errMsg, Invocable5 fn) {
    return fn.invokeSeq(arityRequire(5, H.ret1(s, s = null), errMsg));
  }

  private static void def(Namespace ns, String name, String doc, Sf sf) {
    def(ns, H.symbol(name), doc, sf);
  }

  private static void def(Namespace ns, Symbol name, String doc, Sf sf) {
    ns.def(name, sf, H.map(Sym.docSymK, Str.of(doc)));
  }

  private static void defq(Namespace ns, String name, String doc, SfQuoting sf) {
    defq(ns, H.symbol(name), doc, sf);
  }

  private static void defq(Namespace ns, Symbol name, String doc, SfQuoting sf) {
    ns.def(name, sf, H.map(Sym.docSymK, Str.of(doc), Sym.macroSymK, Sym.macroSymK));
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

  private static void defnArity(Namespace ns, String name, String doc, Invocable0 fn) {
    defn(ns, name, doc, a -> arityRequire(H.ret1(a, a = null), name, fn));
  }

  private static void defnArity(Namespace ns, String name, String doc, Invocable1 fn) {
    defn(ns, name, doc, a -> arityRequire(H.ret1(a, a = null), name, fn));
  }

  private static void defnArity(Namespace ns, String name, String doc, Invocable2 fn) {
    defn(ns, name, doc, a -> arityRequire(H.ret1(a, a = null), name, fn));
  }

  private static void defnArity(Namespace ns, String name, String doc, Invocable3 fn) {
    defn(ns, name, doc, a -> arityRequire(H.ret1(a, a = null), name, fn));
  }

  private static void defnArity(Namespace ns, String name, String doc, Invocable4 fn) {
    defn(ns, name, doc, a -> arityRequire(H.ret1(a, a = null), name, fn));
  }

  private static void defnArity(Namespace ns, String name, String doc, Invocable5 fn) {
    defn(ns, name, doc, a -> arityRequire(H.ret1(a, a = null), name, fn));
  }

  private static void defmacroArity(Namespace ns, String name, String doc, Invocable0 m) {
    defmacro(ns, name, doc, a -> arityRequire(H.ret1(a, a = null), name, m));
  }

  private static void defmacroArity(Namespace ns, String name, String doc, Invocable1 m) {
    defmacro(ns, name, doc, a -> arityRequire(H.ret1(a, a = null), name, m));
  }

  private static void defmacroArity(Namespace ns, String name, String doc, Invocable2 m) {
    defmacro(ns, name, doc, a -> arityRequire(H.ret1(a, a = null), name, m));
  }

  private static void defmacroArity(Namespace ns, String name, String doc, Invocable3 m) {
    defmacro(ns, name, doc, a -> arityRequire(H.ret1(a, a = null), name, m));
  }

  private static void defmacroArity(Namespace ns, String name, String doc, Invocable4 m) {
    defmacro(ns, name, doc, a -> arityRequire(H.ret1(a, a = null), name, m));
  }

  private static void defmacroArity(Namespace ns, String name, String doc, Invocable5 m) {
    defmacro(ns, name, doc, a -> arityRequire(H.ret1(a, a = null), name, m));
  }

  private static <T> void defnArity(Namespace ns, String name, String doc, Function<Term, T> arg1, Function<T, Term> fn) {
    defnArity(ns, name, doc, x -> fn.apply(arg1.apply(H.ret1(x, x = null))));
  }

  private static <T1, T2> void defnArity(Namespace ns, String name, String doc,
                                         Function<Term, T1> arg1, Function<Term, T2> arg2,
                                         BiFunction<T1, T2, ? extends Term> fn) {
    defnArity(ns, name, doc, (x, y) -> fn.apply(arg1.apply(H.ret1(x, x = null)), arg2.apply(H.ret1(y, y = null))));
  }

  private static <T1, T2, T3> void defnArity(Namespace ns, String name, String doc,
                                             Function<Term, T1> arg1, Function<Term, T2> arg2, Function<Term, T3> arg3,
                                             TriFunction<T1, T2, T3, ? extends Term> fn) {
    defnArity(ns, name, doc, (x, y, z) -> fn.apply(arg1.apply(H.ret1(x, x = null)),
                                                   arg2.apply(H.ret1(y, y = null)),
                                                   arg3.apply(H.ret1(z, z = null))));
  }

  @FunctionalInterface
  private static interface TriFunction<T, U, S, R> {
    R apply(T t, U u, S s);
  }
}
