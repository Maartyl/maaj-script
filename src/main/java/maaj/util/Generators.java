/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import maaj.term.Seq;

/**
 *
 * @author maartyl
 */
public class Generators {
  private Generators() {
  }

  public static Seq range() {
    return SeqH.incremental2lazySeq(H::wrap, i -> false);
  }

  public static Seq range(int end) {
    return SeqH.incremental2lazySeq(H::wrap, i -> i >= end);
  }

}
