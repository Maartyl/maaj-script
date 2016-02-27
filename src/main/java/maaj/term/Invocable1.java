/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.util.SeqH;

/**
 * Invocable0..5,Seq : allows me to use these as lambda functions -- functionalInterface
 * These are not meant to be used for arg type checking: Invokable will mostly be implemented using Fn, not lambdas
 * - Could be useful to prevent invoke(T) vs. invokeSeq lambda mistakes etc...
 * <p>
 * @author maartyl
 */
@FunctionalInterface
public interface Invocable1 extends Invocable {

  @Override
  public default Term invokeSeq(Seq args) {
    if (SeqH.isSingle(args))
      return invoke(args.first());

    return throwArityFor(args);
  }

  @Override
  Term invoke(Term arg1);

}
