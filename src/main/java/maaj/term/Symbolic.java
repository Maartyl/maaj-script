/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.util.Objects;
import maaj.term.visitor.Visitor;

/**
 *
 * @author maartyl
 * @param <TSym> the actual symbolic type
 */
public interface Symbolic<TSym extends Symbolic<TSym>> extends Term {

  String getNm();

  default Str getName() {
    return Str.of(getNm());
  }

  default Str getNamespace() {
    return Str.EMPTY;
  }

  default String getNs() {
    return null;
  }


  default boolean hasSameName(Symbolic other) {
    return Objects.equals(getNm(), other.getNm());
  }

  default boolean hasSameNs(Symbolic other) {
    return !(isQualified() || other.isQualified()) || Objects.equals(getNs(), other.getNs());
  }

  boolean isKeyword();

  default boolean isQualified() {
    return getNs() != null;
  }

  /**
   * not a keyword or qualified
   * @return
   */
  default boolean isSimple() {
    return !(isKeyword() || isQualified());
  }

  TSym prependNamespace(String ns);

  /**
   * Always just return a symbol with the same name and given ns.
   * // not meant to be overriden.
   * @param ns
   * @return
   */
  TSym withNamespace(String ns);

  /**
   * Uses ns.name for new namespace name
   * @param ns
   * @return
   */
  default TSym withNamespace(Symbolic ns) {
    return withNamespace(ns.getNm());
  }

  /**
   * copies namespace from arg.
   * @param ns
   * @return
   */
  default TSym withSameNamespace(Symbolic ns) {
    return withNamespace(ns.getNs());
  }

  @Override
  default <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.symbolic(this, arg);
  }

  //-- STATIC
  public static Symbolic of(String str) {
    return (str.charAt(0) == ':') ? Keyword.of(str) : Symbol.of(str);
  }

  static int findNsEnd(String str) {
    if ("/".equals(str)) return -1; //special case
    int slash = str.lastIndexOf('/');
    if (slash == -1) return -1;
    if (slash == str.length() - 1) {
      if (str.charAt(slash - 1) != '/')
        throw new IllegalArgumentException("Symbolic that contains only namespace.");
      else return slash - 1;
    }
    return slash;
  }

}
