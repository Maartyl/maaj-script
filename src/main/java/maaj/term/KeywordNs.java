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
public class KeywordNs extends Keyword {

  private final String ns;

  public KeywordNs(String ns, String name) {
    super(name);
    this.ns = ns;
  }

  @Override
  protected String getNs() {
    return ns;
  }

  @Override
  protected String composeShow() {
    return ':' + ns + '/' + name;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 89 * hash + Objects.hashCode(this.ns);
    hash = 89 * hash + Objects.hashCode(this.name);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final KeywordNs other = (KeywordNs) obj;
    if (!Objects.equals(this.ns, other.ns)) return false;
    if (!Objects.equals(this.name, other.name)) return false;
    return true;
  }


}
