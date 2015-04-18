/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.coll.traits.Growable;

/**
 * Collection of Terms.
 * <p>
 * @author maartyl
 * @param <C> self : final type of collection
 */
public interface Collection<C extends Collection<C>> extends CollectionBase<C>, Growable<C> {

}
