/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term.visitor;

import maaj.term.*;

/**
 * By default recursively applies itself on given term and returns 'copy'/self.
 * Is meant for (mainly 1to1) transformations of AST.
 * <p>
 * @author maartyl
 */
public interface Visitor {

  ///starting point
  default Term run(Term t) {
    return t.transform((Invocable1) this::doVisit);
  }

  default Term id(Term t) {
    return t;
  }

  default Term ground(Ground t) {
    return id(t);
  }

  default Term monad(Monad t) {
    return t.fmap((Invocable1) this::mapper);
  }

  default Term mapper(Term t) {
    return t.transform((Invocable1) this::doVisit);
  }

  default Term doVisit(Term t) {
    return t.visit(this);
  }

  //---
  default Term seq(Seq t) {
    return monad(t);
  }

  default Term coll(Collection t) {
    return monad(t);
  }
  
  default Term vec(Vec t) {
    return coll(t);
  }

  default Term map(Map t) {
    return coll(t);
  }

  default Term collT(CollectionT t) {
    return monad(t);
  }

  default Term vecT(VecT t) {
    return collT(t);
  }

  default Term mapT(MapT t) {
    return collT(t);
  }

  //---

  default Term num(Num t) {
    return ground(t);
  }

  default Term character(Char t) {
    return num(t);
  }

  default Term dbl(Dbl t) {
    return num(t);
  }

  default Term integer(Int t) {
    return num(t);
  }

  //---

  default Term invocable(Invocable t) {
    return ground(t);
  }

  default Term fn(Fn t) {
    return invocable(t);
  }

  default Term macro(Macro t) {
    return invocable(t);
  }

  default Term sf(Sf t) {
    return invocable(t);
  }

  //---

  default Term symbol(Symbol t) {
    return id(t);
  }

  default Term symbolSimple(Symbol t) {
    return symbol(t);
  }

  default Term symbolNs(SymbolNs t) {
    return symbol(t);
  }

  default Term keyword(Keyword t) {
    return symbol(t);
  }

  default Term keywordSimple(Keyword t) {
    return keyword(t);
  }

  default Term keywordNs(KeywordNs t) {
    return keyword(t);
  }

  //---
  default Term str(Str t) {
    return id(t);
  }

  default Term var(Var t) {
    return id(t);
  }

  //---

  default Term jwrap(JWrap t) {
    return ground(t);
  }

  default Term nil(Nil t) {
    return ground(t);
  }

  default Term nilSeq(NilSeq t) {
    return id(t);
  }

  default Term recur(Recur t) {
    return id(t); //I don't see how this could be useful, but why not
  }

  //---
  default Term sfQuote(SfQuoting t) {
    return sf(t);
  }

  default Term sfQuoteSimple(SfQuoting t) {
    return sfQuote(t);
  }

  default Term sfQuoteQualified(SfQuoting t) {
    return sfQuote(t);
  }

  default Term unquote(Unquote t) {
    return id(t);
  }

  default Term unquoteSimple(Unquote t) {
    return unquote(t);
  }

  default Term unquoteSplicing(Unquote t) {
    return unquote(t);
  }

}
