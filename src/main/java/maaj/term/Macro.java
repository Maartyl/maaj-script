/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;

/**
 * A Fn that doesn't evaluate it's args and macro expands it's result.
 * <p>
 * @author maartyl
 */
public class Macro extends Fn {

  protected Macro(Seq fn, Context closure) {
    super(fn, closure);
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    return invokeSeq(args);
  }

  public static Macro of(Seq fn, Context closure) {
    return new Macro(fn.fmap((Invocable1) x -> x.evalMacros(closure)), closure);
  }
}
