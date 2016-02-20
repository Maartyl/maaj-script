/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import maaj.interop.ConverterCombiner;
import maaj.interop.CvrtId;
import maaj.interop.CvrtNil;
import maaj.interop.ImplicitConversions;
import maaj.interop.Interop;
import maaj.interop.InteropJvm;
import maaj.term.Symbol;
import maaj.term.Var;
import maaj.util.CvrtH;
import maaj.util.H;
import maaj.util.Sym;

/**
 * Global context. Expected to be extended by some connection to system etc.
 * <p>
 * @author maartyl
 */
public class Glob {
  private final NamespaceStore store;
  private final Namespace coreAcc;
  private final PathLoader defaultLoader = new PathLoader();
  private final Namespace.Loader emptyLoader = new EmptyLoader();
  private final Context loaderContext = Context.buildStubWithoutNamespace(this);
  private final Interop interop = defaultInteropSimple();

  private Glob(NamespaceStore store) {
    this.store = store;
    CoreLoader l = new CoreLoader();
    this.coreAcc = store.getNamespaceFor(H.symbol("#"), l, loaderContext);
    coreAcc.importFullyQualified(store.getNamespaceFor(H.symbol("#macro"), l, loaderContext));
    coreAcc.importFullyQualified(store.getNamespaceFor(H.symbol("#jvm"), l, loaderContext));
    coreAcc.importFullyQualified(store.getNamespaceFor(Sym.dotSym, (name, cxt) -> new NsDot(), loaderContext));
    coreAcc.importNotQualified(store.getNamespaceFor(H.symbol("#core"), l, loaderContext));
  }

  /**
   * allows access to JVM
   */
  /*package private*/ Interop getInterop() {
    return interop;
  }

  public Namespace loadNamespace(Symbol s) {
    return store.getNamespaceFor(s, defaultLoader, loaderContext);
  }

  public Var getVar(Symbol s, Namespace current) {
    Var v = coreAcc.getOwn(s);
    if (v != null) return v;
    v = current.get(s);
    if (v != null) return v;
    v = coreAcc.get(s);
    // if (v != null) return v;
    // no need to search in others: current has imported anything accessible
    return v;
  }

  public Context start(Symbol emptyNsName) {
    return loaderContext.withNamespace(store.getNamespaceFor(emptyNsName, emptyLoader, loaderContext));
  }

  public static Glob create() {
    return new Glob(new NamespaceStore());
  }

  private static Interop defaultInteropSimple() {
    return new InteropJvm(CvrtH.combine(
            CvrtId.singleton(),
            ImplicitConversions.singleton(),
            CvrtNil.singleton()));
  }

}
