/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.interop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * built from reflection over Method
 * - converts arguments, on top of calling .invoke on Method
 * <p>
 * @author maartyl
 */
public interface Invoker {

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
  Method getMethod();
}
