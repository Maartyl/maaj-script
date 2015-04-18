/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.exceptions.InvalidOperationException;

/**
 *
 * @author maartyl
 */
public interface NilSeq extends Seq {

  @Override
  default boolean isCounted() {
    return true;
  }

  @Override
  default Object getContent() {
    return this;
  }

  @Override
  default boolean isNil() {
    return true;
  }

  @Override
  public default Term first() {
    throw new InvalidOperationException("Head of NilSeq."); //TODO: implement
  }

  @Override
  public default Seq rest() {
    throw new InvalidOperationException("Rest of NilSeq."); //TODO: implement
  }

  @Override
  public default Int count() {
    return Int.of(0);
  }

  @Override
  public default Seq bindM(Invocable fn2Monad) {
    return this;
  }

  @Override
  public default Seq fmap(Invocable mapper) {
    return this;
  }


  public static final NilSeq END = new NilSeq() {

  };
}
