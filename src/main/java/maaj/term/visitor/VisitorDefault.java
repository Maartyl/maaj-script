/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term.visitor;

import maaj.term.*;

/**
 *
 * All return dflt() - to make sense, 'special' should be overriden
 *
 * @author maartyl
 * @param <TR>
 * @param <TA>
 */
public interface VisitorDefault<TR, TA> extends Visitor<TR, TA> {

  //default value returned from all
  TR dflt();

  @Override
  public default TR run(Term t, TA arg) {
    return t.unwrap().visit(this, arg);
  }

  @Override
  public default TR monad(Monad t, TA arg) {
    return dflt();
  }

  @Override
  public default TR ground(Ground t, TA arg) {
    return dflt();
  }

  @Override
  public default TR seq(Seq t, TA arg) {
    return dflt();
  }

  @Override
  public default TR coll(Collection t, TA arg) {
    return dflt();
  }

  @Override
  public default TR vec(Vec t, TA arg) {
    return dflt();
  }

  @Override
  public default TR map(Map t, TA arg) {
    return dflt();
  }

  @Override
  public default TR collT(CollectionT t, TA arg) {
    return dflt();
  }

  @Override
  public default TR vecT(VecT t, TA arg) {
    return dflt();
  }

  @Override
  public default TR mapT(MapT t, TA arg) {
    return dflt();
  }

  @Override
  public default TR num(Num t, TA arg) {
    return dflt();
  }

  @Override
  public default TR character(Char t, TA arg) {
    return dflt();
  }

  @Override
  public default TR dbl(Dbl t, TA arg) {
    return dflt();
  }

  @Override
  public default TR integer(Int t, TA arg) {
    return dflt();
  }

  @Override
  public default TR invocable(Invocable t, TA arg) {
    return dflt();
  }

  @Override
  public default TR fn(Fn t, TA arg) {
    return dflt();
  }

  @Override
  public default TR macro(Macro t, TA arg) {
    return dflt();
  }

  @Override
  public default TR sf(Sf t, TA arg) {
    return dflt();
  }

  @Override
  public default TR symbol(Symbol t, TA arg) {
    return dflt();
  }

  @Override
  public default TR symbolSimple(Symbol t, TA arg) {
    return dflt();
  }

  @Override
  public default TR symbolNs(SymbolNs t, TA arg) {
    return dflt();
  }

  @Override
  public default TR keyword(Keyword t, TA arg) {
    return dflt();
  }

  @Override
  public default TR keywordSimple(Keyword t, TA arg) {
    return dflt();
  }

  @Override
  public default TR keywordNs(KeywordNs t, TA arg) {
    return dflt();
  }

  @Override
  public default TR str(Str t, TA arg) {
    return dflt();
  }

  @Override
  public default TR var(Var t, TA arg) {
    return dflt();
  }

  @Override
  public default TR jwrap(JWrap t, TA arg) {
    return dflt();
  }

  @Override
  public default TR nil(Nil t, TA arg) {
    return dflt();
  }

  @Override
  public default TR recur(Recur t, TA arg) {
    return dflt();
  }

  @Override
  public default TR sfQuote(SfQuoting t, TA arg) {
    return dflt();
  }

  @Override
  public default TR sfQuoteSimple(SfQuoting t, TA arg) {
    return dflt();
  }

  @Override
  public default TR sfQuoteQualified(SfQuoting t, TA arg) {
    return dflt();
  }

  @Override
  public default TR unquote(Unquote t, TA arg) {
    return dflt();
  }

  @Override
  public default TR unquoteSimple(Unquote t, TA arg) {
    return dflt();
  }

  @Override
  public default TR unquoteSplicing(Unquote t, TA arg) {
    return dflt();
  }

}
