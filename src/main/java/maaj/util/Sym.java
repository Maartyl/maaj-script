/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import maaj.term.Map;
import maaj.term.Symbol;
import maaj.term.Symbolic;
import maaj.term.Term;

/**
 * Contains symbols that are used in multiple places or multiple times, so they don't have to be created multiple times.
 * <p>
 * @author maartyl
 */
public class Sym {

  private Sym() {
  }
  /*
   SymK : keyword (:...)
   SymC : #/...
   SymCore: #core/...
   SymCCore: #core/...#
   aaaAaa : aaa-aaa
   */
  
  public static final Symbolic tagSymK = H.symbolic(":tag");
  public static final Symbolic infoSymK = H.symbolic(":info");
  public static final Symbolic numSymK = H.symbolic(":num");
  public static final Symbolic elseSymK = H.symbolic(":else");
  public static final Symbolic maxSymK = H.symbolic(":max");
  public static final Symbolic srcSymK = H.symbolic(":src");
  public static final Symbolic asSymK = H.symbolic(":as");
  public static final Symbolic asteriskSymK = H.symbolic(":*");
  public static final Symbolic fileRowSymK = H.symbolic(":file-row");
  public static final Symbolic fileColSymK = H.symbolic(":file-col");
  public static final Symbolic fileNameSymK = H.symbolic(":file-name");
  public static final Symbolic typeSymK = H.symbolic(":type");
  public static final Symbolic variadicSymK = H.symbolic(":variadic");
  public static final Symbolic qnameSymK = H.symbolic(":qname");
  public static final Symbolic aritySymK = H.symbolic(":arity");
  public static final Symbolic bodySymK = H.symbolic(":body");
  public static final Symbolic macroSymK = H.symbolic(":macro");
  public static final Symbolic docSymK = H.symbolic(":doc");
  public static final Symbolic optimizerSymK = H.symbolic(":optimizer");

  public static final Symbol derefSymCore = H.symbol("#core", "deref");
  public static final Symbol throwAritySymCore = H.symbol("#core", "throw-arity");
  public static final Symbol nameSym = H.symbol("name");
  public static final Symbol namespaceSym = H.symbol("namespace");
  public static final Symbol patternSym = H.symbol("pattern");
  public static final Symbol doSymC = H.symbol("#", "do");
  public static final Symbol ifSymC = H.symbol("#", "if");
  public static final Symbol varSymC = H.symbol("#", "var");
  public static final Symbol condSymCore = H.symbol("#core", "cond");
  public static final Symbol caseSymCore = H.symbol("#core", "case");
  public static final Symbol equalSymCCore = H.symbol("#core", "=#");
  public static final Symbol LTSymCore = H.symbol("#core", "<");
  public static final Symbol countPrimeSymCore = H.symbol("#core", "count'");
  public static final Symbol listSymCore = H.symbol("#core", "list");
  public static final Symbol letSymC = H.symbol("#", "let");
  public static final Symbol defSymC = H.symbol("#", "def");
  public static final Symbol ignoreSym = H.symbol("_");
  public static final Symbol ampSym = H.symbol("&");
  public static final Symbol dotSym = H.symbol(".");
  public static final Symbol firstSym = H.symbol("first");
  public static final Symbol restSym = H.symbol("rest");
  public static final Symbol quoteSymC = H.symbol("#macro", "quote");
  public static final Symbol unquoteSymC = H.symbol("#macro", "unquote");
  public static final Symbol unquoteSplicingSymC = H.symbol("#macro", "unquote-splicing");
  public static final Symbol quoteQualifiedSymC = H.symbol("#macro", "quote-qualified");
  public static final Symbol macroseqSymC = H.symbol("#", "macroseq");
  public static final Symbol fnseqSymC = H.symbol("#", "fnseq");
  public static final Symbol requirePrimeSymC = H.symbol("#", "require'");
  public static final Symbol fnSymCore = H.symbol("#core", "fn");
  public static final Symbol macroSymCore = H.symbol("#core", "macro");

  public static final Symbol invokeStaticSymInterop = H.symbol("#jvm", "invoke-static");
  public static final Symbol invokeVirtualSymInterop = H.symbol("#jvm", "invoke-virtual");
  public static final Symbol ctorSymInterop = H.symbol("#jvm", "ctor");

  public static final Symbol argsSymSpecial = H.symbol("&args");
  public static final Symbol formSymSpecial = H.symbol("&form");

  public static final Map macroMapTag = H.map(macroSymK, macroSymK);
  public static final Term TRUE = H.symbol("t");

}
