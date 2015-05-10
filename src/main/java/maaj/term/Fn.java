/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;
import maaj.util.SeqH;

/**
 * basic functions that operate on evaluated arguments
 * <p>
 * @author maartyl
 */
public interface Fn extends Invocable {

  @Override
  public default Term apply(Context cxt, Seq args) {
    return invokeSeq(SeqH.mapEval(args, cxt));
  }

}
