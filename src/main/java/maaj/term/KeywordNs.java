/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.util.Objects;
import maaj.term.visitor.Visitor;

/**
 * keyword with namespace
 * <p>
 * @author maartyl
 */
public class KeywordNs extends Keyword {

  private final String ns;

  public KeywordNs(String ns, String name) {
    super(name);
    this.ns = ns;
  }

  @Override
  public String getNs() {
    return ns;
  }

  @Override
  public boolean isQualified() {
    return true;
  }

  @Override
  public Keyword prependNamespace(String ns) {
    return qualified(ns, this.ns + '/' + name);
  }

  @Override
  protected String composeShow() {
    return ':' + ns + '/' + name;
  }

  @Override
  public <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.keywordNs(this, arg);
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
    if (obj instanceof Term)
      obj = ((Term) obj).unwrap();
    else return false;
    if (getClass() != obj.getClass()) return false;
    final KeywordNs other = (KeywordNs) obj;
    if (!Objects.equals(this.ns, other.ns)) return false;
    if (!Objects.equals(this.name, other.name)) return false;
    return true;
  }


}
