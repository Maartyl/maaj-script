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
import maaj.term.visitor.Visitor;
import maaj.util.H;
import maaj.util.Sym;

/**
 * special term that is meant to exist inside quotation. quoted terms are traversed,
 * looking for unquote, so it can evaluate it's contents.
 * <p>
 * @author maartyl
 */
public abstract class Unquote implements Term {
  protected final Term body;

  private Unquote(Term body) {
    this.body = body;
  }

  @Override
  public Term eval(Context c) {
    throw new InvalidOperationException("#macro/unquote outside quotation context");
  }

  @Override
  public Term evalMacros(Context c) {
    //possibly make variant that traverses and expands macros in unquotes...
    //essentially: I only need some tag in context, that would be cheked in Unquote.unquoteTraverse
    //wait... I'm already here...
    ///edit: I'm not : I wouldn't get here, because inside quotation. But essentailly posible.
    //I could just return (unquote (expand body)) ...
    //TODO: expand unquote ^
    return this;
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    throw new InvalidOperationException("#macro/unquote outside quotation context");
  }

  public static Unquote createSimple(Term content) {
    return new UnquoteSimple(content);
  }

  public static Unquote createSplicing(Term content) {
    return new UnquoteSplicing(content);
  }

  private static class UnquoteSimple extends Unquote {

    public UnquoteSimple( Term body) {
      super(body);
    }

    @Override
    public Monad unquoteTraverse(Context c) {
        return H.tuple(body.eval(c));
    }

    @Override
    public void show(Writer w) throws IOException {
      H.list(Sym.unquoteSymC, body).show(w);
    }

    @Override
    public <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
      return v.unquoteSimple(this, arg);
    }
  }

  private static class UnquoteSplicing extends Unquote {

    public UnquoteSplicing(Term body) {
      super(body);
    }

    @Override
    public Monad unquoteTraverse(Context c) {
      return H.requireMonad(body.eval(c));
    }

    @Override
    public void show(Writer w) throws IOException {
      H.list(Sym.unquoteSplicingSymC, body).show(w);
    }

    @Override
    public <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
      return v.unquoteSplicing(this, arg);
    }
  }
}
