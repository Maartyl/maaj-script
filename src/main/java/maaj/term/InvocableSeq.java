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
public interface InvocableSeq extends Invocable {

  @Override
  public Term invokeSeq(Seq args);

}
