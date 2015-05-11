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
public interface Macro extends Invocable {

  @Override
  public default Term apply(Context cxt, Seq args) {
    return invokeSeq(args).evalMacros(cxt).eval(cxt);
  }

  @Override
  public default Term applyMacros(Context cxt, Seq args) {
    //return invokeSeq(args.fmap((Invocable1) x -> x.evalMacros(cxt))).evalMacros(cxt);
    return invokeSeq(args).evalMacros(cxt);
  }


  @Override
  public Term invokeSeq(Seq args);

}
