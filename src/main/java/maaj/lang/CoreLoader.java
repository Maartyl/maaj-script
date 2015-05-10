/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import maaj.coll.traits.Reducible;
import maaj.exceptions.InvalidOperationException;
import maaj.term.Fn;
import maaj.term.FnSeq;
import maaj.term.Invocable;
import maaj.term.Keyword;
import maaj.term.Macro;
import maaj.term.MacroSeq;
import maaj.term.Num;
import maaj.term.Seq;
import maaj.term.Sf;
import maaj.term.Str;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.Var;
import maaj.term.Vec;
import maaj.util.H;
import maaj.util.Sym;

/**
 *
 * @author maartyl
 */
public class CoreLoader extends Namespace.Loader {

  @Override
  public Namespace loadNamespaceFor(Symbol nsName, Context cxt) {
    if (!nsName.isSimple())
      throw new IllegalArgumentException("invalid namespace name: " + nsName.print());
    Namespace ns = createEmptyWithName(nsName);
    switch (nsName.getNm()) {
    case "#": loadSf(ns);
      break;
    case "#core": loadCore(ns);
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
   */
  private void loadSf(Namespace core) {
    def(core, "if", "", (c, a) -> {
      int len = a.boundLength(3);
      if (len == 2)
        return a.first().isNil() ? H.NIL : a.rest().first().eval(c);
      if (len == 3)
        return !a.first().isNil() ? a.rest().first().eval(c) : a.rest().rest().first().eval(c);
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
    def(core, "let", "(let [v exp] (... v ... v ...)); ", (c, a) -> {
      if (a.isNil())
        throw new InvalidOperationException("#/let: requires bindings");
      if (!(a.first().getContent() instanceof Vec))
        throw new InvalidOperationException("#/let: bindings must be a vector");
      return H.cons(Sym.doSymC, a.rest()).eval(letReduceBindings(c, (Vec) a.first()));
    });
    def(core, "fnseq", "(fnseq body body $args body)", (c, a) -> FnSeq.of(a, c));
    def(core, "macroseq", "(macroseq body body $args body)", (c, a) -> MacroSeq.of(a, c));
    def(core, "eval", "evaluates given term", (c, a) -> {
      arityRequire(1, a, "eval");
      return a.first().eval(c);
    });

  }

  private Context letReduceBindings(Context cxt, Vec v) {
    if (v.getCountAsInteger() % 2 != 0)
      throw new InvalidOperationException("#/let: binding requires even number of terms");
    
    for (Seq s = v.seq(); !s.isNil(); s = s.rest().rest()) {
      Term k = s.first().unwrap();
      if (!(k instanceof Symbol)) //TODO: yup, keywords will be passed, but it's not really that much of a problem...
        throw new InvalidOperationException("#/let: binding requires symbol; got: " + k.print());
      Term exp = s.rest().first();
      Term r = exp.eval(cxt);
      if (!(Sym.ignoreSym).equals(k))
        cxt = cxt.addToScope(k, r);
    }
    return cxt;
  }

  /**
   * throws on on any problem; only returns if nu is valid Symbol
   * returns if ((Sym)nu).isQualified
   */
  private boolean defCheckAndRetIfQualified(Term nu, Symbol curNs) {
    if (nu instanceof Keyword)
      throw new InvalidOperationException("#/def: requires symbol for name, got:" + nu.getType().getName());
    if (!(nu instanceof Symbol))
      throw new InvalidOperationException("#/def: requires symbol for name, got:" + nu.getType().getName());
    Symbol s = (Symbol) nu;
    if (s.isQualified()) {
      if (s.getNs() == null ? curNs.getNm() != null : !s.getNs().equals(curNs.getNm()))
        throw new InvalidOperationException("#/def: cannot modify vars outside current namespace:" + s.print());
      return true;
    }
    return false;
  }

  private void loadCore(Namespace core) {
    defn(core, "meta", "get meta data of term", a -> a.isNil() ? H.NIL.getMeta() : a.first().getMeta());

    defn(core, Sym.firstSym, "first of seq (head)", a -> {
      arityRequire(1, a, "first");
      return (H.seqFrom(a.first())).firstOrNil();
    });
    defn(core, Sym.restSym, "rest of seq (tail)", a -> {
      arityRequire(1, a, "rest");
      return (H.seqFrom(a.first())).restOrNil();
    });
    defmacro(core, "car", "first of seq (head)", a -> H.cons(Sym.firstSym, a));
    defmacro(core, "cdr", "rest of seq (head)", a -> H.cons(Sym.restSym, a));
    defmacro(core, "cadr", "(first (rest a))", a -> H.list(Sym.firstSym, H.cons(Sym.restSym, a)));
    defmacro(core, "cddr", "(rest (rest a))", a -> H.list(Sym.restSym, H.cons(Sym.restSym, a)));

    defn(core, "reduce", "get meta data of term", a -> {
      arityRequire(3, a, "reduce");
      Invocable fn = H.requireInvocable(a.first());
      Term start = a.rest().first();
      Reducible coll = H.requireReducible(a.rest().rest().first());
      return coll.reduce(start, fn);
    });

    defn(core, "+#", "sums 2 args", (Num.Num2Op) Num::plus);
    defn(core, "-#", "subtracts 2 args", (Num.Num2Op) Num::minus);

  }

  /**
   * this does not define macros; but namespace for working with macros
   */
  private void loadMacro(Namespace macro) {
    def(macro, "quote", "returns first arg without evaluating it", (c, a) -> a.isNil() ? H.NIL : a.first());
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
    ns.def(name, m, H.map(Sym.docSymK, Str.of(doc)));
  }

}
