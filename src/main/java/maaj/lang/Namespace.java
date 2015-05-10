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
    return vars.computeIfAbsent(name, n -> Var.empty());
  }

  public Var def(Symbol name, Term val) {
    return vars.computeIfAbsent(name, n -> Var.of(val));
  }

  public Var def(Symbol name, Term val, Map meta) {
    return vars.computeIfAbsent(name, n -> Var.of(val, meta));
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

  public void importQualified(Namespace ns, Symbol prefix) {
    if (ns.getName().getNm().equals(prefix.getNm()))
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
