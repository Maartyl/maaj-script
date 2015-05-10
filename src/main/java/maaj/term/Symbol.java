/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import maaj.exceptions.InvalidOperationException;
import maaj.lang.Context;
import maaj.util.H;
import maaj.util.SeqH;

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

  public String getNs() {
    return null;
  }

  public String getNm() {
    return name;
  }


  public boolean isQualified() {
    return false;
  }

  public boolean isKeyword() {
    return false;
  }

  /**
   * not a keyword or qualified
   * @return
   */
  public boolean isSimple() {
    return true;
  }

  public Symbol prependNamespace(String ns) {
    return qualified(ns, name);
  }

  /**
   * Always just return a symbol with the same name and given ns.
   * // not meant to be overriden.
   * @param ns
   * @return
   */
  public Symbol withNamespace(String ns) {
    return qualified(ns, name);
  }

  /**
   * Uses ns.name for new namespace name
   * @param ns
   * @return
   */
  public Symbol withNamespace(Symbol ns) {
    return qualified(ns.getNm(), name);
  }
  /**
   * copies namespace from arg.
   * @param ns
   * @return
   */
  public Symbol withSameNamespace(Symbol ns) {
    return qualified(ns.getNs(), name);
  }

  public Symbol asSimple() {
    return this;
  }

  public boolean hasSameNs(Symbol other) {
    return !(isQualified() || other.isQualified()) || Objects.equals(getNs(), other.getNs());
  }

  public boolean hasSameName(Symbol other) {
    return Objects.equals(getNm(), other.getNm());
  }

  @Override
  public Term eval(Context c) {
    //don't evaluate
    //TODO: only unwrap vars!!! //or something that represents that...
    return c.valAt(this).unwrap();
  }

  @Override
  public Term evalMacros(Context c) {
    return c.valAt(this, this);
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    //this is not function application: it only uses eval() and then calls apply on that fn.
    args = SeqH.mapEval(args, cxt);
    if (args.first().getContent() instanceof Symbol) {
      //infinite recursion
      throw new InvalidOperationException("applying symbol to a symbol");
    }
    return args.first().apply(cxt, H.cons(this, args.rest()));
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
  public void show(Writer w) throws IOException {
    w.append(name);
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

  @Override
  public String toString() {
    return print();
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
