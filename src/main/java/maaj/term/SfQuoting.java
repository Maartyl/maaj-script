/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.lang.Context;
import maaj.term.visitor.Visitor;
import maaj.util.H;
import maaj.util.Sym;

/**
 * These are special special forms, in that they need to affect not only evaluation, but also macro expansion.
 * ~Cannot (functionally, it can, but it wouldn't .show() correctly...) be defined as lambdas.
 * <p>
 * It is necessary, that Vars with SfQuoting are tagged :macro
 * i.e. handles macro expansion
 * <p>
 * @author maartyl
 */
public abstract class SfQuoting implements Sf {

  /**
   * it differs in that it does not macro expand arguments
   * @param cxt  ignored
   * @param args
   * @return (cons this args)
   */
  @Override
  public Term applyMacro(Context cxt, Seq args) {
    return H.cons(this, args);
  }

  public static final SfQuoting SfQuote = new Quote();
  public static final SfQuoting SfQuoteQualified = new QuoteQualified();

  private static class Quote extends SfQuoting {

    @Override
    public Term apply(Context cxt, Seq args) {
      return args.firstOrNil();
    }

    @Override
    public void show(Writer w) throws IOException {
      Sym.quoteSymC.show(w);
    }

    @Override
    public <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
      return v.sfQuoteSimple(this, arg);
    }
  }

  /**
   * With this implementation: unquotes are not macro expanded.
   * //TODO? unquoteTraverse option to macro expand, instead of evaluate...
   * // - it would actually be simpler: normal traverse (without splicing) is enough
   */
  private static class QuoteQualified extends SfQuoting {

    @Override
    public Term apply(Context cxt, Seq args) {
      return args.isNil() ? H.NIL : H.seqFrom(args.first().unquoteTraverse(cxt)).firstOrNil();
    }

    @Override
    public void show(Writer w) throws IOException {
      Sym.quoteQualifiedSymC.show(w);
    }

    @Override
    public <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
      return v.sfQuoteQualified(this, arg);
    }
  }

}
