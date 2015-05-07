/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

/**
 *
 * @author maartyl
 */
public interface QueueLike<S extends QueueLike<S>> extends Poppable<S>, Growable<S> {

}
