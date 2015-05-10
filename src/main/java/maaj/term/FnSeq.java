/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.lang.Context;
import maaj.util.H;

/**
 * basic functions that operate on evaluated arguments
 * <p>
 * @author maartyl
 */
public final class FnSeq implements Fn {

  private final Seq fn;
  private final Context closure;

  protected FnSeq(Seq fn, Context closure) {
    this.fn = fn;
    this.closure = closure;
  }

  @Override
  public Term invokeSeq(Seq args) {
    return H.cons(doSym, fn).eval(closure.addToScope(H.map(argsSym, args)));
  }

  @Override
  public void show(Writer w) throws IOException {
    H.cons(fnseqSym, fn).show(w);
  }

  private static final Symbol fnseqSym = H.symbol("#", "fnseq");
  private static final Symbol argsSym = H.symbol("$args");
  private static final Symbol doSym = H.symbol("#", "do");

  public static FnSeq of(Seq fn, Context closure) {
    return new FnSeq(fn.fmap((Invocable1) x -> x.evalMacros(closure)), closure);
  }


}
