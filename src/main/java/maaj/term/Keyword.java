/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

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
  public Term evalMacros(Context c) {
    return this;
  }

  @Override
  public Term eval(Context c) {
    return this;
  }

  protected String composeShow() {
    return ':' + name;
  }

  @Override
  public Str show() {
    return Str.of(composeShow());
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
