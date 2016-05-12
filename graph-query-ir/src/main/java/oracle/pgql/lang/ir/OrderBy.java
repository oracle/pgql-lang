package oracle.pgql.lang.ir;

import java.util.Iterator;
import java.util.List;

public class OrderBy {

  private final List<OrderByElem> elements;

  public OrderBy(List<OrderByElem> elements) {
    this.elements = elements;
  }
  
  public List<OrderByElem> getElements() {
    return elements;
  }
  
  @Override
  public String toString() {
    String result = "ORDER BY ";
    Iterator<OrderByElem> it = elements.iterator();
    while (it.hasNext()) {
      result += it.next();
      if (it.hasNext()) {
        result += ", ";
      }
    }
    return result;
  }
}
