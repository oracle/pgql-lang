package oracle.pgql.lang.ir;

import java.util.List;

import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;

public class Projection {

  private final List<ExpAsVar> elements;

  public Projection(List<ExpAsVar> elements) {
    this.elements = elements;
  }

  public List<ExpAsVar> getElements() {
    return elements;
  }

  @Override
  public String toString() {
    return printPgqlString(this);
  }
}
