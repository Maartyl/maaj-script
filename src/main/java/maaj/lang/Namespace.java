/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import java.util.concurrent.ConcurrentHashMap;
import maaj.term.Map;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.Var;

/**
 *
 * @author maartyl
 */
public class Namespace {
  private final Symbol nsName;

  private final java.util.Map<Symbol, Var> vars = new ConcurrentHashMap<>();

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

  public Var get(Symbol name) {
    return vars.get(name);
  }

  public static abstract class Loader {
    public abstract Namespace loadNamespaceFor(Symbol nsName, maaj.lang.Context cxt);

    protected Namespace createEmptyWithName(Symbol nsName) {
      return new Namespace(nsName);
    }
  }
}
