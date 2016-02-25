/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term.visitor;

import maaj.term.*;
import maaj.util.H;

/**
 * By default recursively applies itself on given term and returns 'copy'/self.
 * Is meant for (mainly 1to1) transformations of AST.
 * <p>
 * @author maartyl
 * @param <TR> return type of visit-traverse
 * @param <TA> type of extra argument to visit
 */
public interface Visitor<TR, TA> {

  ///starting point
  TR run(Term t, TA arg);

  default TR run(Term t) {
    return run(H.ret1(t, t = null), init());
  }

  default TA init() {
    return null;
  }

  TR monad(Monad t, TA arg);

  TR ground(Ground t, TA arg);

  //---
  TR io(IO t, TA arg);

  TR seq(Seq t, TA arg);

  TR coll(Collection t, TA arg);

  TR vec(Vec t, TA arg);

  TR map(Map t, TA arg);

  TR collT(CollectionT t, TA arg);

  TR vecT(VecT t, TA arg);

  TR mapT(MapT t, TA arg);

  //---
  TR num(Num t, TA arg);

  TR character(Char t, TA arg);

  TR dbl(Dbl t, TA arg);

  TR integer(Int t, TA arg);

  //---
  TR invocable(Invocable t, TA arg);

  TR fn(Fn t, TA arg);

  TR macro(Macro t, TA arg);

  TR sf(Sf t, TA arg);

  //---
  TR symbol(Symbol t, TA arg);

  TR symbolSimple(Symbol t, TA arg);

  TR symbolNs(SymbolNs t, TA arg);

  TR keyword(Keyword t, TA arg);

  TR keywordSimple(Keyword t, TA arg);

  TR keywordNs(KeywordNs t, TA arg);

  //---
  TR str(Str t, TA arg);

  TR var(Var t, TA arg);

  //---
  TR jwrap(JWrap t, TA arg);

  TR nil(Nil t, TA arg);

  TR recur(Recur t, TA arg);

  //---
  TR sfQuote(SfQuoting t, TA arg);

  TR sfQuoteSimple(SfQuoting t, TA arg);

  TR sfQuoteQualified(SfQuoting t, TA arg);

  TR unquote(Unquote t, TA arg);

  TR unquoteSimple(Unquote t, TA arg);

  TR unquoteSplicing(Unquote t, TA arg);

}
