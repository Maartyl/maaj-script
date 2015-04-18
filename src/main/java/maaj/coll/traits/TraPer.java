/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

/**
 * Base of Persistent/Transient pair
 * <p>
 * @author maartyl
 * @param <PColl> persistent variant of collection
 * @param <TColl> transient variant of collection
 */
//public interface TraPer<PColl extends Persistent<? extends PColl, ? extends TColl>, TColl extends Transient<? extends PColl, ? extends TColl>> {
//
//  PColl asPersistent();
//
//  TColl asTransient();
//}
public interface TraPer<PColl, TColl> {

  PColl asPersistent();

  TColl asTransient();
}
