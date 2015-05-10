/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.exceptions.InvalidOperationException;
import maaj.lang.Context;

/**
 * special forms (like Fn/Macro, but accesses context)
 * more like macros in that it operates on ~AST itself
 * <p>
 * @author maartyl
 */
public interface Sf extends Invocable {

  @Override
  public Term apply(Context cxt, Seq args);

  @Override
  public default Term invokeSeq(Seq args) {
    throw new InvalidOperationException("Cannot directly invoke special form.");
  }

}
