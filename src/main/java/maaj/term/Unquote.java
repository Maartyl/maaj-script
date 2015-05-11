/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.exceptions.InvalidOperationException;
import maaj.lang.Context;
import maaj.util.H;
import maaj.util.Sym;

/**
 *
 * @author maartyl
 */
public class Unquote implements Term {

  private final boolean isSplicing;
  private final Term body;

  private Unquote(boolean isSplicing, Term body) {
    this.isSplicing = isSplicing;
    this.body = body;
  }

  @Override
  public Monad unquoteTraverse(Context c) {
    if (isSplicing) 
      return H.requireMonad(body.eval(c));
    else 
      return H.tuple(body.eval(c));
  }

  @Override
  public Term eval(Context c) {
    throw new InvalidOperationException("#macro/unquote outside quotation context");
  }

  @Override
  public Term evalMacros(Context c) {
    throw new InvalidOperationException("#macro/unquote outside quotation context");
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    throw new InvalidOperationException("#macro/unquote outside quotation context");
  }


  @Override
  public void show(Writer w) throws IOException {
    H.list(isSplicing ? Sym.unquoteSplicingSymC : Sym.unquoteSymC, body).show(w);
  }

  public static Unquote createSimple(Term content) {
    return new Unquote(false, content);
  }

  public static Unquote createSplicing(Term content) {
    return new Unquote(true, content);
  }
}
