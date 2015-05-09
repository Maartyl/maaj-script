/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.lang.Context;

/**
 *
 * @author maartyl
 */
public class Keyword extends Symbol {

  protected Keyword(String name) {
    super(name);
  }

  @Override
  public boolean isKeyword() {
    return true;
  }

  @Override
  public Symbol prependNamespace(String ns) {
    return qualified(ns, name);
  }

  @Override
  public Term evalMacros(Context c) {
    return qualified(c.getCurrentNamespaceName(), name);
  }

  @Override
  public Term eval(Context c) {
    return this;
  }

  protected String composeShow() {
    return ':' + name;
  }

  @Override
  public void show(Writer w) throws IOException {
    w.append(composeShow());
  }

  @Override
  public int hashCode() {
    return 3 * name.hashCode() - 3;
  }

  @Override
  public String toString() {
    return composeShow();
  }

  //-- STATIC
  public static Keyword of(String str) {
    if (str.charAt(0) == ':') str = str.substring(1);
    int slash = findNsEnd(str);
    if (slash < 0) return simple(str);
    return qualifiedNonNull(str.substring(0, slash), str.substring(slash + 1));
  }

  public static Keyword simple(String name) {
    return new Keyword(name);
  }

  private static Keyword qualifiedNonNull(String ns, String name) {
    return new KeywordNs(ns, name);
  }

  public static Keyword qualified(String ns, String name) {
    if (ns == null) return simple(name);
    return qualifiedNonNull(ns, name);
  }
}
