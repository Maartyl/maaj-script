/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

/**
 * Persistent half of Persistent/Transient pair
 * <p>
 * @author maartyl
 * @param <PColl> persistent variant of collection
 * @param <TColl> transient variant of collection
 */
//public interface Persistent<
//        PColl extends Persistent<PColl, TColl>, TColl extends Transient<PColl, TColl>>
//        extends TraPer<PColl, TColl> {
//
//  @Override
//  public TColl asTransient();
//
//  @Override
//  public PColl asPersistent();
//
//}
public interface Persistent<PColl, TColl>
        extends TraPer<PColl, TColl> {

  @Override
  public TColl asTransient();

  @Override
  public PColl asPersistent();

}
