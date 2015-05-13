/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import maaj.term.Map;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.Var;
import maaj.util.H;
import static maaj.util.Sym.nameSym;
import static maaj.util.Sym.namespaceSym;

/**
 *
 * @author maartyl
 */
public final class Namespace {
  private final Symbol nsName;

  //these symbols are not qualified : they "would" be qualified with this namespace
  private final java.util.Map<Symbol, Var> vars = new ConcurrentHashMap<>();
  //these symbols can be qualified to avoid name collisions
  //all of them are ALSO fully qualified
  //this might not be a good way to do it, but for now should be good enough...
  private final java.util.Map<Symbol, Var> imported = new ConcurrentHashMap<>();

  private Namespace(Symbol name) {
    this.nsName = name;
  }

  public Symbol getName() {
    return nsName;
  }

  public Var def(Symbol name) {
    if (name.isQualified())
      throw new IllegalArgumentException("cannot create var from qualified name");
    return vars.computeIfAbsent(name, n -> Var.empty().addMeta(H.map(nameSym, n, namespaceSym, getName())));
  }

  public Var def(Symbol name, Term val) {
    return def(name, val, val.getMeta());
  }

  public Var def(Symbol name, Term val, Map meta) {
    Var v = def(name).doSet(val);
    v.addMeta(meta);
    return v;
  }
  /**
   *
   * @param name symbol to lookup
   * @return null if not found; corresponding Var otherwise
   */
  public Var get(Symbol name) {
    Var v = vars.get(name);
    if (v == null) {
      v = imported.get(name);
      if (v == null && getName().getNm().equals(name.getNs()))
        v = vars.get(name.asSimple());
    }
    return v;
  }

  /**
   * like get, but deosn't search in imported symbols
   * @param name var name to lookup
   * @return null if not found
   */
  public Var getOwn(Symbol name) {
    return vars.get(name);
  }

  public void importQualified(Namespace ns, Symbol prefix) {
    if (!prefix.isSimple())
      throw new IllegalArgumentException("cannot import qualifying with qualified prefix");
    if (ns.getName().hasSameName(prefix))
      importFullyQualified(ns);
    else
      for (Entry<Symbol, Var> e : ns.vars.entrySet()) {
        imported.put(e.getKey().withNamespace(prefix), e.getValue());
        imported.put(e.getKey().withNamespace(ns.getName()), e.getValue());
      }
  }

  public void importFullyQualified(Namespace ns) {
    for (Entry<Symbol, Var> e : ns.vars.entrySet()) {
      imported.put(e.getKey().withNamespace(ns.getName()), e.getValue());
    }
  }

  public void importNotQualified(Namespace ns) {
    for (Entry<Symbol, Var> e : ns.vars.entrySet()) {
      imported.put(e.getKey().asSimple(), e.getValue());
      imported.put(e.getKey().withNamespace(ns.getName()), e.getValue());
    }
  }

  public static abstract class Loader {
    public abstract Namespace loadNamespaceFor(Symbol nsName, maaj.lang.Context cxt);

    protected Namespace createEmptyWithName(Symbol nsName) {
      return new Namespace(nsName);
    }
  }
}
