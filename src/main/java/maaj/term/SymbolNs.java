/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.util.Objects;

/**
 *
 * @author maartyl
 */
public class SymbolNs extends Symbol {
  private final String ns;

  SymbolNs(String ns, String name) {
    super(name);
    this.ns = ns;
  }

  @Override
  protected String getNs() {
    return ns;
  }

  @Override
  public Str getNamespace() {
    return Str.of(ns);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + Objects.hashCode(this.ns);
    hash = 89 * hash + Objects.hashCode(this.name);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final SymbolNs other = (SymbolNs) obj;
    if (!Objects.equals(this.ns, other.ns)) return false;
    if (!Objects.equals(this.name, other.name)) return false;
    return true;
  }

}
