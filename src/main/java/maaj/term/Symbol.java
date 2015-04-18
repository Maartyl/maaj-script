/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.util.Objects;
import maaj.lang.Context;

/**
 *
 * @author maartyl
 */
public class Symbol implements Term {
  protected final String name;

  protected Symbol(String name) {
    this.name = name;
  }

  public Str getName() {
    return Str.of(name);
  }

  public Str getNamespace() {
    return Str.EMPTY;
  }

  protected String getNs() {
    return null;
  }

  @Override
  public Term eval(Context c) {
    return c.valAt(this);
  }

  @Override
  public Term evalMacros(Context c) {
    return this;
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Term applyMacros(Context cxt, Seq args) {
    //TODO: determine if represents macro, if so: eval().applyMacros; otherwise default impl
    return Term.super.applyMacros(cxt, args);
  }

  @Override
  public void serialize(java.io.Writer w) throws IOException {
    w.append(toString());
  }

  @Override
  public Str show() {
    return Term.super.show();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.name);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final Symbol other = (Symbol) obj;
    if (!Objects.equals(this.name, other.name)) return false;
    return true;
  }


  //-- STATIC
  public static Symbol of(String str) {
    if ( str.charAt(0) == ':') return Keyword.of(str);
    int slash = findNsEnd(str);
    if (slash < 0) return simple(str);
    return qualified(str.substring(0, slash), str.substring(slash + 1));
  }

  public static Symbol simple(String name) {
    return new Symbol(name);
  }

  public static Symbol qualified(String ns, String name) {
    return new SymbolNs(ns, name);
  }

  protected static int findNsEnd(String str) {
    int slash = str.lastIndexOf('/');
    if (slash == -1) return -1;
    if (slash == str.length() - 1) {
      if (str.charAt(slash - 1) != '/')
        throw new IllegalArgumentException("Symbol that contains only namespace.");
      else return slash - 1;
    }
    return slash;
  }
}
