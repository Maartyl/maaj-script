/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.util.H;

/**
 *
 * @author maartyl
 */
public interface Invocable0 extends Invocable {

  @Override
  public default Term invokeSeq(Seq args) {
    //final int arity = 0;
    if (args.isNil())
      return invoke();

    return throwArityFor(args);
  }

  @Override
  Term invoke();
}
