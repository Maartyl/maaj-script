/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Term;

/**
 *
 * @author maartyl
 */
public interface Peekable {
  /**
   * Has some well defined way to look at element of collection in particualr place.
   * (Most likely one of ends)
   * @return element of collection
   */
  Term peek();

}
