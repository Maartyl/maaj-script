/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.interop;

/**
 * built from reflection over Method
 * - converts arguments, on top of calling .invoke on Method
 * <p>
 * @author maartyl
 */
public interface Invoker extends BasicInvoker {

  /**
   * sum of costs of all conversions
   * <p>
   * @return sum of costs of all conversions
   */
  int cost();

  /**
   * internally used method object
   * <p>
   * @return internally used method object
   */
  String getMethodString();
}
