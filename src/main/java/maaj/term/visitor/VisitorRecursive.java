/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term.visitor;

import maaj.term.*;
import maaj.util.H;

/**
 *
 * @author maartyl
 */
public interface VisitorRecursive<TR, TA> extends Visitor<TR, TA> {

  TR id(Term t, TA arg);

  @Override
  default TR run(Term t, TA arg) {
    return H.ret1(t, t = null).unwrap().visit(this, arg);
  }

  @Override
  default TR ground(Ground t, TA arg) {
    return id(t, arg);
  }

  //---
  @Override
  default TR io(IO t, TA arg) {
    return monad(t, arg);
  }

  @Override
  default TR seq(Seq t, TA arg) {
    return monad(t, arg);
  }

  @Override
  default TR coll(Collection t, TA arg) {
    return monad(t, arg);
  }

  @Override
  default TR vec(Vec t, TA arg) {
    return coll(t, arg);
  }

  @Override
  default TR map(Map t, TA arg) {
    return coll(t, arg);
  }

  @Override
  default TR collT(CollectionT t, TA arg) {
    return monad(t, arg);
  }

  @Override
  default TR vecT(VecT t, TA arg) {
    return collT(t, arg);
  }

  @Override
  default TR mapT(MapT t, TA arg) {
    return collT(t, arg);
  }

  //---
  @Override
  default TR num(Num t, TA arg) {
    return ground(t, arg);
  }

  @Override
  default TR character(Char t, TA arg) {
    return num(t, arg);
  }

  @Override
  default TR dbl(Dbl t, TA arg) {
    return num(t, arg);
  }

  @Override
  default TR integer(Int t, TA arg) {
    return num(t, arg);
  }

  //---
  @Override
  default TR invocable(Invocable t, TA arg) {
    return ground(t, arg);
  }

  @Override
  default TR fn(Fn t, TA arg) {
    return invocable(t, arg);
  }

  @Override
  default TR macro(Macro t, TA arg) {
    return invocable(t, arg);
  }

  @Override
  default TR sf(Sf t, TA arg) {
    return invocable(t, arg);
  }

  //---
  @Override
  default TR symbol(Symbol t, TA arg) {
    return id(t, arg);
  }

  @Override
  default TR symbolSimple(Symbol t, TA arg) {
    return symbol(t, arg);
  }

  @Override
  default TR symbolNs(SymbolNs t, TA arg) {
    return symbol(t, arg);
  }

  @Override
  default TR keyword(Keyword t, TA arg) {
    return symbol(t, arg);
  }

  @Override
  default TR keywordSimple(Keyword t, TA arg) {
    return keyword(t, arg);
  }

  @Override
  default TR keywordNs(KeywordNs t, TA arg) {
    return keyword(t, arg);
  }

  //---
  @Override
  default TR str(Str t, TA arg) {
    return id(t, arg);
  }

  @Override
  default TR var(Var t, TA arg) {
    return id(t, arg);
  }

  //---
  @Override
  default TR jwrap(JWrap t, TA arg) {
    return ground(t, arg);
  }

  @Override
  default TR nil(Nil t, TA arg) {
    return ground(t, arg);
  }

  @Override
  default TR recur(Recur t, TA arg) {
    return id(t, arg); //I don't see how this could be useful, but why not
  }

  //---
  @Override
  default TR sfQuote(SfQuoting t, TA arg) {
    return sf(t, arg);
  }

  @Override
  default TR sfQuoteSimple(SfQuoting t, TA arg) {
    return sfQuote(t, arg);
  }

  @Override
  default TR sfQuoteQualified(SfQuoting t, TA arg) {
    return sfQuote(t, arg);
  }

  @Override
  default TR unquote(Unquote t, TA arg) {
    return id(t, arg);
  }

  @Override
  default TR unquoteSimple(Unquote t, TA arg) {
    return unquote(t, arg);
  }

  @Override
  default TR unquoteSplicing(Unquote t, TA arg) {
    return unquote(t, arg);
  }

}
