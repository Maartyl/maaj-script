/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import maaj.term.visitor.Visitor;

/**
 * symbol with namespace
 * <p>
 * @author maartyl
 */
public class SymbolNs extends Symbol {
  private final String ns;

  SymbolNs(String ns, String name) {
    super(name);
    this.ns = ns;
  }

  @Override
  public String getNs() {
    return ns;
  }

  @Override
  public Symbol prependNamespace(String ns) {
    return qualified(ns, this.ns + '/' + name);
  }

  @Override
  public Symbol asSimple() {
    return simple(name);
  }
  @Override
  public boolean isSimple() {
    return false;
  }
  @Override
  public boolean isQualified() {
    return true;
  }

  @Override
  public Str getNamespace() {
    return Str.of(ns);
  }
  @Override
  public void show(Writer w) throws IOException {
    w.append(ns).append('/').append(name);
  }

  @Override
  public <TR, TA> TR visit(Visitor<TR, TA> v, TA arg) {
    return v.symbolNs(this, arg);
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
    if (obj instanceof Term)
      obj = ((Term) obj).unwrap();
    else return false;
    if (getClass() != obj.getClass()) return false;
    final SymbolNs other = (SymbolNs) obj;
    if (!Objects.equals(this.ns, other.ns)) return false;
    if (!Objects.equals(this.name, other.name)) return false;
    return true;
  }

}
