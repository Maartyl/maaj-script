/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import maaj.term.Symbol;

/**
 * This class manages loaded namespaces, so they are not loaded multiple times.
 * <p>
 * @author maartyl
 */
public class NamespaceStore {
  private final Map<Symbol, Namespace> namespaces = new ConcurrentHashMap<>();

  public Namespace getNamespaceFor(Symbol nsName, Namespace.Loader loader, maaj.lang.Context cxt) {
    return namespaces.computeIfAbsent(nsName, n -> loader.loadNamespaceFor(n, cxt));
  }
}
