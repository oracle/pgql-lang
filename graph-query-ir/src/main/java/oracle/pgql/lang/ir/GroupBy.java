package oracle.pgql.lang.ir;

import java.util.Iterator;
import java.util.List;

public class GroupBy {

  private final List<ExpAsVar> elements;
  
  public GroupBy(List<ExpAsVar> elements) {
    this.elements = elements;
  }
  
  public List<ExpAsVar> getElements() {
    return elements;
  }
  
  @Override
  public String toString() {
    String result = "GROUP BY ";
    Iterator<ExpAsVar> it = elements.iterator();
    if (it.hasNext()) {
      result += it.next();
      if (it.hasNext()) {
        result += ", ";
      }
    }
    return result;
  }
}
