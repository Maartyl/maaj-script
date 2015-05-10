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
import maaj.util.SeqH;

/**
 * basic functions that operate on evaluated arguments
 * <p>
 * @author maartyl
 */
public class Fn implements Invocable {
  private final Seq fn;
  private final Context closure;

  protected Fn(Seq fn, Context closure) {
    this.fn = fn;
    this.closure = closure;
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    return invokeSeq(SeqH.mapEval(args, cxt));
  }

  @Override
  public Term invokeSeq(Seq args) {
    return fn.eval(closure.addToScope(H.map(argsSym, args)));
  }

  @Override
  public void show(Writer w) throws IOException {
    H.list(H.symbol("#/fn"), fn).show(w);
  }

  protected static final Symbol argsSym = H.symbol("$args");

  public static Fn of(Seq fn, Context closure) {
    return new Fn(fn.fmap((Invocable1) x -> x.evalMacros(closure)), closure);
  }


}
