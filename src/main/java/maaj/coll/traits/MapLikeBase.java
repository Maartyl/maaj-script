/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.KVPair;

/**
 *
 * @author maartyl
 */
public interface MapLikeBase<M extends MapLikeBase<M>> extends AssocGet, Counted, Iterable<KVPair> {

}
