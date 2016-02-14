/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.interop;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author maartyl
 */
public interface BasicInvoker {
  /**
   * invokes underlying Method + converts arguments
   * <p>
   * @param thisPtr //ignored if static
   * @param args
   * @return return value, null if void method
   * @throws java.lang.IllegalAccessException
   * @throws java.lang.reflect.InvocationTargetException
   */
  Object invoke(Object thisPtr, Object[] args) throws IllegalAccessException, InvocationTargetException;

}
