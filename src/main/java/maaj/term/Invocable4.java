/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

/**
 * Invocable0..5,Seq : allows me to use these as lambda functions -- functionalInterface
 * These are not meant to be used for arg type checking: Invokable will mostly be implemented using Fn, not lambdas
 * - Could be useful to prevent invoke(T) vs. invokeSeq lambda mistakes etc...
 * <p>
 * @author maartyl
 */
@FunctionalInterface
public interface Invocable4 extends Invocable {

  @Override
  public default Term invokeSeq(Seq args) {
    final int arity = 4;
    if (args.boundLength(arity) == arity)
      return invoke(args.first(),
                    (args = args.rest()).first(),
                    (args = args.rest()).first(),
                    (args = args.rest()).first());

    return throwArityFor(args);
  }

  @Override
  Term invoke(Term arg1, Term arg2, Term arg3, Term arg4);
}
