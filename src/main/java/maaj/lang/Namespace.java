/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import java.util.Collection;
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
 * This class has 2 jobs:<br/>
 * - storing Vars of current namespace, making them accessible through their name.<br/>
 * - storing references to Vars from other namespaces, that are imported to this namespace.<br/>
 * <p>
 * @author maartyl
 */
public final class Namespace {

  //unqualified symbol
  private final Symbol nsName;

  //these symbols are not qualified : they "would" be qualified with this namespace
  private final java.util.Map<Symbol, Var> vars = new ConcurrentHashMap<>();
  //NONE of them are qualified (see importedNs)
  private final java.util.Map<Symbol, Var> imported = new ConcurrentHashMap<>();

  //for fully qualified and ns-aliased access
  //- keys are namespace names
  //- also contains this namespace to unify lookup
  private final java.util.Map<String, Namespace> qualified = new ConcurrentHashMap<>();

  private Namespace(Symbol name) {
    this.nsName = name;
    qualified.put(name.getNm(), self());
  }

  private Namespace self() {
    return this;
  }

  public Symbol getName() {
    return nsName;
  }

  //so other namespaces can import unqualified snapshot
  public Collection<Var> getAllOwn() {
    return vars.values();
  }

  /**
   * creates Var under given name
   * - var will have default meta : name, namespace and meta from name
   * - if Var with given name already exists, returns that without modifying it
   */
  public Var def(Symbol name) {
    if (name.isQualified())
      throw new IllegalArgumentException("cannot create var from qualified name");
    return vars.computeIfAbsent(name, n -> Var.empty().addMeta(H.map(nameSym, n, namespaceSym, getName())));
  }
  /**
   * creates Var under given name with given value
   * - will merge meta of name and value
   * - if Var with given name already exists, returns that witch changed contents to value and updated meta
   */
  public Var def(Symbol name, Term val) {
    return def(name, val, val.getMeta());
  }
  /**
   * creates Var under given name with given value and meta
   * - will merge meta of name and value
   * - if Var with given name already exists, returns that witch changed contents to value and updated meta
   */
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
    Var v = getOwn(name);
//    if (v == null) {
//      v = imported.get(name);
//      if (v == null && getName().getNm().equals(name.getNs()))
//        v = vars.get(name.asSimple());
//    }

    //new version
    if (v == null)
      if (name.isQualified()) {
        Namespace ns = qualified.get(name.getNs());
        if (ns != null)
          return ns.getOwn(name.asSimple());
      } else 
        return imported.get(name);
      
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

  private void importTested(Namespace ns, String name) {
    Namespace saved = qualified.putIfAbsent(name, ns);
    if (saved != null && saved != ns)
      throw new IllegalArgumentException("Different namespace already saved under: " + name + " (current: " + saved.getName() + ")");
  }
  /**
   * Vars will be accessible through prefix/name instead of namespace/name
   * it will ALSO import everything as fully qualified
   */
  public void importQualified(Namespace ns, Symbol prefix) {
    if (!prefix.isSimple())
      throw new IllegalArgumentException("cannot import qualifying with qualified prefix");
    importTested(ns, prefix.getNm());
    importFullyQualified(ns);
  }

  public void importFullyQualified(Namespace ns) {
    importTested(ns, ns.getName().getNm());
  }
  /**
   * Vars will be accessible through just name instead of namespace/name
   * it will ALSO import everything as fully qualified
   */
  public void importNotQualified(Namespace ns) {
    importFullyQualified(ns);
    //will rewrite any previous imported under the same name
    ns.getAllOwn().stream().forEach(v -> imported.put(nameOfVar(v), v)); 
  }

  private Symbol nameOfVar(Var v) {
    return H.requireSymbol(v.getMeta(nameSym));
  }

  /**
   * namespaces can be created through custom loaders, that inherit this class.
   */
  public static abstract class Loader {
    public abstract Namespace loadNamespaceFor(Symbol nsName, maaj.lang.Context cxt);

    protected Namespace createEmptyWithName(Symbol nsName) {
      return new Namespace(nsName);
    }
  }
}
