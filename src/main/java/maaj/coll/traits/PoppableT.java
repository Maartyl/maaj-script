/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

/**
 *
 * @author maartyl
 * @param <P> self
 */
public interface PoppableT<P extends PoppableT<P>> extends Peekable {

  /**
   * Self without what is returned by peek().
   * @return Collection without what returns peek().
   */
  P doPop();
}
