/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import java.util.Collection;
import maaj.term.Map;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.Var;

/**
 * Represents namespaces, i.e. sets of Vars (mutable cells) for storing global 'variables'. (like functions)
 * Also allows access to imported namespaces (if the namespace supports imports).
 * <p>
 * @author maartyl
 */
public interface Namespace {

  /**
   * creates Var under given name
   * - var will have default meta : name, namespace and meta from name
   * - if Var with given name already exists, returns that without modifying it
   * @param name
   * @return
   */
  Var def(Symbol name);

  /**
   * creates Var under given name with given value
   * - will merge meta of name and value
   * - if Var with given name already exists, returns that witch changed contents to value and updated meta
   * @param name
   * @param val
   * @return
   */
  Var def(Symbol name, Term val);

  /**
   * creates Var under given name with given value and meta
   * - will merge meta of name and value
   * - if Var with given name already exists, returns that witch changed contents to value and updated meta
   * @param name
   * @param val
   * @param meta
   * @return
   */
  Var def(Symbol name, Term val, Map meta);

  /**
   *
   * @param name symbol to lookup
   * @return null if not found; corresponding Var otherwise
   */
  Var get(Symbol name);

  /**
   * @return
   */
  //so other namespaces can import unqualified snapshot
  Collection<Var> getAllOwn();

  /**
   * @return
   */
  Symbol getName();

  /**
   * like get, but deosn't search in imported symbols
   * @param name var name to lookup
   * @return null if not found
   */
  Var getOwn(Symbol name);

  /**
   */
  void importFullyQualified(Namespace ns);

  /**
   * Vars will be accessible through just name instead of namespace/name
   * it will ALSO import everything as fully qualified
   * @param ns
   */
  void importNotQualified(Namespace ns);

  /**
   * Vars will be accessible through prefix/name instead of namespace/name
   * it will ALSO import everything as fully qualified
   * @param ns
   * @param prefix
   */
  void importQualified(Namespace ns, Symbol prefix);

  /**
   * namespaces can be created through custom loaders, that implement this interface.
   */
  public static interface Loader {
    public Namespace loadNamespaceFor(Symbol nsName, maaj.lang.Context cxt);
  }

  public static interface ReadOnly extends Namespace {

    @Override
    default Var def(Symbol name) {
      throw new UnsupportedOperationException("This namespace [" + getName() + "] cannot define anything.");
    }

    @Override
    default Var def(Symbol name, Term val) {
      return def(name);
    }

    @Override
    default Var def(Symbol name, Term val, Map meta) {
      return def(name);
    }

    @Override
    default Var get(Symbol name) {
      return getOwn(name); //cannot import anything
    }

    @Override
    default void importFullyQualified(Namespace ns) {
      throw new UnsupportedOperationException("This namespace [" + getName() + "] cannot import anything.");
    }

    @Override
    default void importNotQualified(Namespace ns) {
      importFullyQualified(ns);
    }

    @Override
    default void importQualified(Namespace ns, Symbol prefix) {
      importFullyQualified(ns);
    }
  }
}
