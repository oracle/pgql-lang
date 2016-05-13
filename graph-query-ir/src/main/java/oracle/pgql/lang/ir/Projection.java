package oracle.pgql.lang.ir;

import java.util.Iterator;
import java.util.List;

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
    String result = "SELECT ";
    if (elements.isEmpty()) {
      result += "*";
    } else {
      Iterator<ExpAsVar> it = elements.iterator();
      while (it.hasNext()) {
        result += it.next();
        if (it.hasNext()) {
          result += ", ";
        }
      }
    }
    return result;
  }
}
