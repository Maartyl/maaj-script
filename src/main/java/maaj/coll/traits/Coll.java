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
 * @param <C> self
 */
public interface Coll<C extends Coll<C>> extends Reducible, Functor<C>, Iterable<Term>, Numerable {

}
