/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;

/**
 * basic functions that operate on evaluated arguments
 * <p>
 * @author maartyl
 */
public class Fn implements Invocable {
  private Seq fn;
  private Context closure;
}
