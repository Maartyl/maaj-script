/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.interop;

/**
 * functional interface to be implemented in conversion maps
 * <p>
 * @author maartyl
 */
@FunctionalInterface
public interface CvrtSimple extends Conversion {
  @Override
  public default int cost() {
    return 2;
  }
}
