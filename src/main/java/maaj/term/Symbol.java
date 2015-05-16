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
import maaj.util.MapH;
import maaj.util.SeqH;
import maaj.util.Sym;

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
  public Collection unquoteTraverse(Context c) {
    if (isQualified() || isKeyword())
      return H.tuple(this);
    Var v = c.getVar(this);
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
    //TODO: 2 variants: only return macros; "compile" <- everything, espacially native methods
    // yes, that is needed; unless otherwise specified in context in future
    // ...
//    Term rslt = c.valAt(this, this);
//    if (rslt == this) return this;
//    if (!MapH.hasTag(rslt.getMeta(), Sym.macroSymK)) return this; //only "inline" what claims to be a macro
//    return rslt.evalMacros(c);

    //vars should be stored directly in terms (fn...), but it evaluates into their values...
    //TODO: fix overinlining (probably calls unwrap: should change Var unwrap? - maybe not a Mimic?)
    Var v = c.getVar(this);
    if (v != null)
      return v;
    return this;
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
  public Term applyMacro(Context cxt, Seq args) {
    //this must be here, not in evalMacro : why?
    //I only want to get macros if in context of application of 1
    //if just (defn m [a] (inc 5)), m is macro : I don't wan't m to get evaluated into the #/macroseq
    Term rslt = cxt.valAt(this, this);
    //if (rslt == this) return this;
    if (MapH.hasTag(rslt.getMeta(), Sym.macroSymK)) return rslt.applyMacro(cxt, args); //only "inline" what claims to be a macro
    // Term.super: sfmaps evalMacro on args
    return Term.super.applyMacro(cxt, args);
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
    if ("/".equals(str)) return -1; //special case
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
