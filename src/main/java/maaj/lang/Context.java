/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import maaj.coll.traits.Lookup;
import maaj.exceptions.InvalidOperationException;
import maaj.term.Map;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.Var;
import maaj.util.H;
import maaj.util.MapH;
import static maaj.util.Sym.nameSym;

/**
 *
 * @author maartyl
 */
public class Context implements Lookup {

  /*
   what I contain:
   reference to 'global' scope :: Symbol/Term -> ? / Var? / Term
   - all other lisps have Vars - why? - to change stuff? can't I just change the map?
   -- so closures can remember mutable values? - I will look it up repeatedly, no? ... maybe not, then it could help...
   -- EDIT: nope: vars are saved inside fns / stuff and only content changes

   local scope variables map :: Map // my or IPersistentMap<Symbol, Term>
   - using MyMap makes no sense: I wil never need it and it would only be slower

   bindings: in future: only layer before accessing global... ?
   - how different from local vars? - visible inside Vars ... !!!
   -- must be INSIDE VARS not here - ok

   and some reference to truly global scope
   - way to access other objects in JVM

   EDIT:
   - I will need namespaces:
   current namespace + name
   lang namespace: #
   special namespaces that are referenced through # starting names
   - these are lang features / ...
   required namespaces
   - namespace should be more then just a map
   -- in case I need to access ... complicated stuff

   */
  private final Glob glob;

  private Namespace curNs;

  private Map scope;

  private Context(Glob glob, Namespace curNs, Map scope) {
    this.glob = glob;
    this.curNs = curNs;
    this.scope = scope;
  }

  private Context(Context c, Map scope) {
    this(c.glob, c.curNs, scope);
  }

  private Context(Glob glob, Namespace curNs) {
    this(glob, curNs, MapH.emptyPersistent());
  }

  private Context(Context c, Namespace curNs) {
    this(c.glob, curNs, c.scope);
  }

  public Namespace getCurNs() {
    return curNs;
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

  public Context withNamespace(Namespace ns) {
    return new Context(this, ns);
  }

  public Context addToScope(Map let) {
    return new Context(this, MapH.update(scope, let));
  }

  public Context addToScope(Term key, Term val) {
    return new Context(this, scope.assoc(key, val));
  }

  public static Context buildStubWithoutNamespace(Glob g) {
    return new Starting(g);
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
