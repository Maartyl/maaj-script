/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.interop;

/**
 *
 * @author maartyl
 */
public interface Conversion {

  //0 for id, 1 for simple, possibly more
  int cost();

  //
  Object convert(Object obj);
}
