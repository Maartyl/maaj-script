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
import maaj.term.visitor.Visitor;
import maaj.util.H;
import maaj.util.SeqH;

/**
 * Special symbols that evaluate to themselves and are mainly used as markers or as keys in maps.
 * <p>
 * @author maartyl
 */
public class Keyword implements Symbolic<Keyword>, Ground {
  protected final String name;

  protected Keyword(String name) {
    this.name = name;
  }

  @Override
  public boolean isKeyword() {
    return true;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public Keyword prependNamespace(String ns) {
    return qualified(ns, name);
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
  public void show(Writer w) throws IOException {
    w.append(composeShow());
  }

  @Override
  public <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.keywordSimple(this, arg);
  }

  @Override
  public int hashCode() {
    return 3 * name.hashCode() - 3;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj instanceof Term)
      obj = ((Term) obj).unwrap();
    else return false;
    if (getClass() != obj.getClass()) return false;
    final Keyword other = (Keyword) obj;
    if (!Objects.equals(this.name, other.name)) return false;
    return true;
  }

  @Override
  public String toString() {
    return composeShow();
  }

  @Override
  public String getNm() {
    return name;
  }

  @Override
  public Keyword withNamespace(String ns) {
    return qualified(ns, name);
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    //this is not function application: it only uses eval() and then calls apply on that fn.
    args = SeqH.mapEval(args, cxt);
    if (args.first().getContent() instanceof Symbolic) {
      //infinite recursion
      throw new InvalidOperationException("applying keyword to a symbolic");
    }
    return args.first().apply(cxt, H.cons(this, args.rest()));
  }

  @Override
  public Monad unquoteTraverse(Context c) {
    return H.tuple(this);
  }

  //-- STATIC
  public static Keyword of(String str) {
    if (str.charAt(0) == ':') str = str.substring(1);
    int slash = Symbolic.findNsEnd(str);
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
