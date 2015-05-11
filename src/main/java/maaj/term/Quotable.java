/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;

/**
 *
 * @author maartyl
 */
public interface Quotable {

  /**
   * how this works:
   * when something gets quoted, the "insides" are unquoteTraversed
   * - every collection is "concat mapped" because unquoteSplicing
   * can produce other number of elements then 1
   * - single elements just return itself wrapped in some cheap collection (tuple1)
   * <p>
   * <p>
   * @param c context to eval unquote terms in
   * @return represents ~concat.map : original quoted data or evaluated unquoted
   */
  Monad unquoteTraverse(Context c);
}
