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
public interface Invocable2 extends Invocable {

  @Override
  public default Term invokeSeq(Seq args) {
    final int arity = 2;
    if (args.boundLength(arity) == arity)
      return invoke(args.first(), args.rest().first());

    return throwArityFor(args);
  }

  @Override
  Term invoke(Term arg1, Term arg2);
}
