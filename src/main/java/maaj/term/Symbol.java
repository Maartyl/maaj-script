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
import maaj.util.MapH;
import maaj.util.SeqH;
import maaj.util.Sym;

/**
 * Maaj identifiers. Generally used as markers or to "reference" other terms.
 * <p>
 * @author maartyl
 */
public class Symbol implements Symbolic<Symbol> {
  protected final String name;

  protected Symbol(String name) {
    this.name = name;
  }

  @Override
  public String getNm() {
    return name;
  }

  @Override
  public boolean isQualified() {
    return false;
  }

  @Override
  public boolean isKeyword() {
    return false;
  }

  /**
   * not a keyword or qualified
   * @return
   */
  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public Symbol prependNamespace(String ns) {
    return qualified(ns, name);
  }

  /**
   * Always just return a symbol with the same name and given ns.
   * // not meant to be overriden.
   * @param ns
   * @return
   */
  @Override
  public Symbol withNamespace(String ns) {
    return qualified(ns, name);
  }

  public Symbol asSimple() {
    return this;
  }

  @Override
  public Collection unquoteTraverse(Context c) {
    if (!isSimple())
      return H.tuple(this);
    Var v = c.getVar(this); //qualify (use this ns or ns it refers to)
    if (v == null)
      return H.tuple(withNamespace(c.getCurNs().getName()));
    return H.tuple(withNamespace(H.requireSymbol(v.getMeta(Sym.namespaceSym))));
  }

  @Override
  public Term eval(Context c) {
    //don't evaluate
    //ok to unwrap: don't reatain meta 
    return c.valAt(this).unwrap();
  }

  @Override
  public Term evalMacros(Context c) {
    //TODO: this is still wrong; macro expansion is only macro expansion, not inlineing
    // - I need another construct (like ... inlineVars or something) that will be run only in functions
    // - this breaks correctness of macros
//    Var v = c.getVar(this);
//    if (v != null)
//      return v;
    return this;
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    //this is not function application: it only uses eval() and then calls apply on that fn.
    args = SeqH.mapEval(args, cxt);
    if (args.first().getContent() instanceof Symbolic) {
      //infinite recursion
      throw new InvalidOperationException("applying symbol to a symbolic");
    }
    return args.first().apply(cxt, H.cons(this, args.rest()));
  }

  @Override
  public Term applyMacro(Context cxt, Seq args) {
    //this must be here, not in evalMacro : why?
    //I only want to get macros if in context of application of the macro
    Var v = cxt.getVar(this);
    if (v != null && MapH.hasTag(v.getMeta(), Sym.macroSymK))
      return v.applyMacro(cxt, args);
    //return Symbolic.super.applyMacro(cxt, args);
    return SeqH.cons(this, args.fmap((Invocable1) x -> x.evalMacros(cxt)));
  }

  @Override
  public void show(Writer w) throws IOException {
    w.append(name);
  }

  @Override
  public <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.symbolSimple(this, arg);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.name);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj instanceof Term)
      obj = ((Term) obj).unwrap();
    else return false;
    if (getClass() != obj.getClass()) return false;
    final Symbol other = (Symbol) obj;
    return Objects.equals(this.name, other.name);
  }

  @Override
  public String toString() {
    return print();
  }



  //-- STATIC
  public static Symbol of(String str) {
    assert str.charAt(0) != ':' : "Symbol.of used for reading keyword: " + str;
    int slash = Symbolic.findNsEnd(str);
    if (slash < 0) return simple(str);
    return qualified(str.substring(0, slash), str.substring(slash + 1));
  }

  public static Symbol simple(String name) {
    return new Symbol(name);
  }

  public static Symbol qualified(String ns, String name) {
    return new SymbolNs(ns, name);
  }
}
