/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import maaj.term.Symbol;
import maaj.term.Var;
import maaj.util.H;

/**
 *
 * @author maartyl
 */
public class Glob {
  private final NamespaceStore store;
  private final Namespace coreAcc;
  private final PathLoader defaultLoader = new PathLoader();
  private final Namespace.Loader emptyLoader = new EmptyLoader();
  private final Context loaderContext = Context.buildStubWithoutNamespace(this);


  private Glob(NamespaceStore store) {
    this.store = store;
    this.coreAcc = loadCore();
  }

  private Namespace loadCore() {
    CoreLoader l = new CoreLoader();
    Namespace core = store.getNamespaceFor(H.symbol("#"), l, loaderContext);
    core.importFullyQualified(store.getNamespaceFor(H.symbol("#macro"), l, loaderContext));
    core.importNotQualified(store.getNamespaceFor(H.symbol("#core"), l, loaderContext));
    return core;
  }

  public Namespace require(Symbol s) {
    return store.getNamespaceFor(s, defaultLoader, loaderContext);
  }

  public Var getVar(Symbol s, Namespace current) {
    Var v = coreAcc.getOwn(s);
    if (v != null) return v;
    v = current.get(s);
    if (v != null) return v;
    v = coreAcc.get(s);
    // if (v != null) return v;
    // no need to search in others: current has imported anything acessible
    return v;
  }

  public Context start(Symbol emptyNsName) {
    return loaderContext.withNamespace(store.getNamespaceFor(emptyNsName, emptyLoader, loaderContext));
  }

  public static Glob create() {
    return new Glob(new NamespaceStore());
  }

}
