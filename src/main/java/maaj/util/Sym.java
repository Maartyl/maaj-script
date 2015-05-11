/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import maaj.term.Symbol;

/**
 *
 * @author maartyl
 */
public class Sym {

  private Sym() {
  }
  public static final Symbol derefSymC = H.symbol("#core", "deref");
  public static final Symbol tagSymK = H.symbol(":tag");
  public static final Symbol nameSym = H.symbol("name");
  public static final Symbol doSymC = H.symbol("#", "do");
  public static final Symbol letSymC = H.symbol("#", "let");
  public static final Symbol defSymC = H.symbol("#", "def");
  public static final Symbol docSymK = H.symbol(":doc");
  public static final Symbol ignoreSym = H.symbol("_");
  public static final Symbol ampSym = H.symbol("&");
  public static final Symbol firstSym = H.symbol("first");
  public static final Symbol restSym = H.symbol("rest");
  public static final Symbol quoteSymC = H.symbol("#macro", "quote");
  public static final Symbol unquoteSymC = H.symbol("#macro", "unquote");
  public static final Symbol unquoteSplicingSymC = H.symbol("#macro", "unquote-splicing");
  public static final Symbol quoteQualifiedSymC = H.symbol("#macro", "quote-qualified");
  public static final Symbol argsSym = H.symbol("$args");
  public static final Symbol macroseqSymC = H.symbol("#", "macroseq");
  public static final Symbol fnseqSymC = H.symbol("#", "fnseq");
  public static final Symbol fnSymC = H.symbol("#", "fn");
  public static final Symbol macroSymC = H.symbol("#", "macro");


}
