/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

/**
 *
 * @author maartyl
 */
public interface InvocableSeq extends Invocable {

  @Override
  public Term invokeSeq(Seq args);

}
