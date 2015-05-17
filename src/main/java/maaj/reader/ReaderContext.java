/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.reader;

import maaj.term.Symbol;

/**
 * "arguments" for reader (MaajReader).
 * - Includes information that might be needed while reading terms.
 * - It is likely to grow with more functionality in MaajReader.
 * <p>
 * @author maartyl
 */
public class ReaderContext {

  private final Symbol namespace;
  private final String fileName;

  public ReaderContext(Symbol namespace, String file) {
    this.namespace = namespace;
    this.fileName = file;
  }

  public Symbol getNamespace() {
    return namespace;
  }

  public String getFileName() {
    return fileName;
  }

  public String getCurrentNamespaceName() {
    return namespace.getNm();
  }

}
