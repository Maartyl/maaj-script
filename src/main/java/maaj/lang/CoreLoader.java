/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import maaj.exceptions.InvalidOperationException;
import maaj.term.Keyword;
import maaj.term.Map;
import maaj.term.Seq;
import maaj.term.Sf;
import maaj.term.Str;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.Var;
import maaj.term.Vec;
import maaj.util.H;

/**
 *
 * @author maartyl
 */
public class CoreLoader extends Namespace.Loader {

  @Override
  public Namespace loadNamespaceFor(Symbol nsName, Context cxt) {
    Namespace ns = createEmptyWithName(nsName);
    Context c = cxt.withNamespace(ns);
    switch (nsName.getNm()) {
    case "#": loadSf(c, ns);
      break;
    case "#core": loadCore(c, ns);
      break;
    case "#macro": loadMacro(c, ns);
      break;
    }
    return ns;
  }

  private void loadSf(Context cxt, Namespace core) {
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
      if (defCheckIsQualified(nu, c.getCurNs().getName())) {
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
    def(core, "let", "(let1 [v exp] (... v ... v ...)); ", (c, a) -> {
      if (a.isNil())
        throw new InvalidOperationException("#/let: requires bindings");
      if (!(a.first().getContent() instanceof Vec))
        throw new InvalidOperationException("#/let: bindings must be a vector");
      return H.cons(doSym, a.rest()).eval(letReduceBindings(c, (Vec) a.first()));
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
      if (!(ignoreSym).equals(k))
        cxt = cxt.addToScope(k, r);
    }
    return cxt;
  }

  /**
   * throws on on any problem; only returns if nu is valid Symbol
   * returns if ((Sym)nu).isQualified
   */
  private boolean defCheckIsQualified(Term nu, Symbol curNs) {
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

  private void loadCore(Context c, Namespace ns) {
    //throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  private void loadMacro(Context c, Namespace ns) {
    //throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  private static final Symbol docSym = H.symbol(":doc");
  private static final Symbol doSym = H.symbol("#", "do");
  private static final Symbol ignoreSym = H.symbol("_");

  private static void def(Namespace ns, String name, String doc, Sf sf) {
    ns.def(H.symbol(name), sf, H.map(docSym, Str.of(doc)));
  }
}
