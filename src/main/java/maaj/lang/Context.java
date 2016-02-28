/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import java.util.concurrent.ConcurrentHashMap;
import maaj.coll.traits.Lookup;
import maaj.exceptions.InvalidOperationException;
import maaj.interop.Interop;
import maaj.term.Map;
import maaj.term.Seq;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.Var;
import maaj.util.H;
import maaj.util.MapH;
import static maaj.util.Sym.nameSym;

/**
 * Is passed throughout evaluation and consists of:
 * - reference to Glob
 * - current namespace
 * - local scope (map from simple symbols to associated values)
 * <p>
 * @author maartyl
 */
public class Context implements Lookup {

  private final Glob glob;

  private final Namespace curNs;

  private final Map scope;

  private final java.util.Map<Symbol, Symbol> autoGensyms;

  private Context(Glob glob, Namespace curNs, java.util.Map<Symbol, Symbol> autoGensyms, Map scope) {
    this.glob = glob;
    this.curNs = curNs;
    this.scope = scope;
    this.autoGensyms = autoGensyms;
  }

  private Context(Context c, Map scope) {
    this(c.glob, c.curNs, c.autoGensyms, scope);
  }

  private Context(Glob glob, Namespace curNs) {
    this(glob, curNs, emptyUnquoteAutoGensyms(), MapH.emptyPersistent());
  }

  private Context(Context c, Namespace curNs) {
    this(c.glob, curNs, c.autoGensyms, c.scope);
  }

  private Context(Context c, java.util.Map<Symbol, Symbol> autoGensyms) {
    this(c.glob, c.curNs, autoGensyms, c.scope);
  }

  public Namespace getCurNs() {
    return curNs;
  }

  public Interop getInterop() {
    return glob.getInterop();
  }

  public Var def(Symbol name, Term val, Map meta) {
    meta = meta.assoc(nameSym, name);
    return curNs.def(name, val, meta);
  }

  public Var def(Symbol name, Map meta) {
    return curNs.def(name).addMeta(meta);
  }

  public void importQualified(Namespace ns, Symbol prefix) {
    curNs.importQualified(ns, prefix);
  }

  public void importFullyQualified(Namespace ns) {
    curNs.importFullyQualified(ns);
  }

  public void importNotQualified(Namespace ns) {
    curNs.importNotQualified(ns);
  }

  public Namespace require(Symbol s) {
    return glob.loadNamespace(s);
  }

  public Var getVar(Symbol s) {
    return glob.getVar(s, getCurNs());
  }

  @Override
  public Term valAt(Term key) {
    return valAt(key, H.NIL);
  }

  private Term valAtOrNull(Symbol key) {
    Term t = scope.valAt(key, scope); // scope: only to check if found
    if (t != scope)
      return t;
    Var v = glob.getVar(key, curNs);
    if (v == null)
      return null;
    return v.deref();
  }

  public Term valAt(Symbol key) {
    Term v = valAtOrNull(key);
    if (v == null)
      throw new InvalidOperationException("cannot resolve symbol: " + key);
    return v;
  }

  public Term valAt(Symbol key, Term dflt) {
    Term v = valAtOrNull(key);
    if (v == null)
      return dflt;
    return v;
  }

  @Override
  public Term valAt(Term key, Term dflt) {
    if (key instanceof Symbol)
      return valAt((Symbol) key, dflt);

    Term t = scope.valAt(key, scope);
    if (t != scope)
      return t;
    return dflt;
  }

  public Symbol resolveAutoGensym(Symbol sym) {
    //used inside unqoteTravrse //qualified-quote: symbols that end with #
    return autoGensyms.computeIfAbsent(sym, H::uniqueSymbol);
  }

  public Context withEmptyAutoGensym() {
    //the correct one is shared with all in same quotation
    //laziness doesn't matter: it will be computed when/if necessary but they share the same, correct map
    //this is called from SfQuoting before each new quote
    return new Context(this, emptyUnquoteAutoGensyms());
  }

  public Context withNamespace(Namespace ns) {
    return new Context(this, ns);
  }

  public Context addToScope(Map let) {
    return new Context(this, MapH.update(scope, let));
  }

  public Context addToScope(Term key, Term val) {
    return new Context(this, scope.assoc(key, val));
  }

  public Context scopeKeepOnly(Seq keys) {
    return new Context(this, MapH.keepOnly(scope, keys));
  }

  public static Context buildStubWithoutNamespace(Glob g) {
    return new Starting(g);
  }

  private static java.util.Map<Symbol, Symbol> emptyUnquoteAutoGensyms() {
    return new ConcurrentHashMap<>();
  }

  private static class Starting extends Context {

    public Starting(Glob glob) {
      super(glob, null);
    }

    @Override
    public Term valAt(Symbol key) {
      throw new UnsupportedOperationException("not initialized with namespace");
    }

    @Override
    public Namespace getCurNs() {
      throw new UnsupportedOperationException("not initialized with namespace");
    }

    @Override
    public Term valAt(Term key, Term dflt) {
      throw new UnsupportedOperationException("not initialized with namespace");
    }

    @Override
    public Term valAt(Term key) {
      throw new UnsupportedOperationException("not initialized with namespace");
    }

    @Override
    public Context addToScope(Map let) {
      throw new UnsupportedOperationException("not initialized with namespace");
    }

    @Override
    public Var getVar(Symbol s) {
      throw new UnsupportedOperationException("not initialized with namespace");
    }

    @Override
    public Namespace require(Symbol s) {
      throw new UnsupportedOperationException("not initialized with namespace");
    }

    @Override
    public void importNotQualified(Namespace ns) {
      throw new UnsupportedOperationException("not initialized with namespace");
    }

    @Override
    public void importFullyQualified(Namespace ns) {
      throw new UnsupportedOperationException("not initialized with namespace");
    }

    @Override
    public void importQualified(Namespace ns, Symbol prefix) {
      throw new UnsupportedOperationException("not initialized with namespace");
    }

    @Override
    public Var def(Symbol name, Term val, Map meta) {
      throw new UnsupportedOperationException("not initialized with namespace");
    }

  }
}
