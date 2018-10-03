/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

public class ResultSetFormatter {

  private static final String NULL_STRING = "<null>";
  private static final long DEFAULT_PRINT_LIMIT = 1000;

  /**
   * Prints the specified number of results in the ResultSet to the given PrintStream, starting from
   * the provided index.
   * 
   * @param resultSet <code>ResultSet</code> containing results
   * @param printStream <code>PrintStream</code> where the results should be printed
   * @param numResults number of results 
   * @param from index of first result that should be printed
   * @throws PgqlException if a connection error occurs or when this method is called on a closed result set
   */
  public static void out(ResultSet<? extends ResultAccess> resultSet, PrintStream printStream, long numResults,
      int from) throws PgqlException {
    out(resultSet, printStream, numResults, from, false);
  }

  /**
   * Prints all the results in the ResultSet to <code>System.out</code>.
   * 
   * @param resultSet <code>ResultSet</code> containing results
   * @throws PgqlException if a connection error occurs or when this method is called on a closed result set
   */
  public static void out(ResultSet<? extends ResultAccess> resultSet) throws PgqlException {
    out(resultSet, System.out, DEFAULT_PRINT_LIMIT, 0, true);
  }

  private static void out(ResultSet<? extends ResultAccess> resultSet, PrintStream printStream, long numResults,
      int from, boolean truncate) throws PgqlException {
    List<? extends ResultElement> resultElements = resultSet.getResultElements();
    int numElements = resultElements.size();

    int[] columnWidth = new int[numElements];
    for (int i = 0; i < numElements; i++) {
      int length = resultElements.get(i).getVarName().length();
      if (length > columnWidth[i]) {
        columnWidth[i] = length;
      }
    }

    Iterator<? extends ResultAccess> results = resultSet.iterator();
    for (int i = 0; i < from; i++) {
      resultSet.next();
    }
    int count = 0;
    while (results.hasNext() && count < numResults) {
      ResultAccess result = results.next();
      for (int i = 0; i < numElements; i++) {
        Object elem = result.getObject(i + 1);
        int length = getLengthOfElem(elem);
        if (length > columnWidth[i]) {
          columnWidth[i] = length;
        }
      }
      count++;
    }

    int totalWidth = -1;
    for (int i = 0; i < numElements; i++) {
      totalWidth += columnWidth[i] + 3;
    }

    String truncationMessage = "";
    if (truncate && resultSet.getNumResults() > numResults) {
      truncationMessage = "... truncated " + (resultSet.getNumResults() - DEFAULT_PRINT_LIMIT) + " rows ...";
      int columnNum = 0;
      while (totalWidth < truncationMessage.length()) {
        if (columnWidth.length > 0) {
          columnWidth[columnNum] += 1;
          columnNum = (columnNum + 1) % columnWidth.length;
        }
        totalWidth++;
      }
    }

    printHorizontalLine(printStream, totalWidth);

    printStream.print("|");
    if (numElements == 0) {
      for (int i = 0; i < totalWidth; i++) {
        printStream.print(" ");
      }
      printStream.print("|"); // zero columns
    } else {
      for (int i = 0; i < numElements; i++) {
        printElem(printStream, resultElements.get(i).getVarName(), columnWidth[i]);
      }
    }
    printStream.println();

    printHorizontalLine(printStream, totalWidth);

    results = resultSet.iterator();
    for (int i = 0; i < from; i++) {
      results.next();
    }
    count = 0;
    while (results.hasNext() && count < numResults) {
      printStream.print("|");
      ResultAccess result = results.next();
      if (numElements == 0) {
        for (int i = 0; i < totalWidth; i++) {
          printStream.print(" ");
        }
        printStream.print("|"); // zero columns
      } else {
        for (int i = 0; i < numElements; i++) {
          printElem(printStream, result.getObject(i + 1), columnWidth[i]);
        }
      }
      printStream.println();
      count++;
    }

    if (truncate && resultSet.getNumResults() > numResults) {
      printTruncationMessage(printStream, totalWidth, truncationMessage);
    } else {
      printHorizontalLine(printStream, totalWidth);
    }
  }

  private static void printTruncationMessage(PrintStream printStream, int tableWidth, String truncationMessage) {
    String before = "";
    for (int i = 0; i < Math.floor((tableWidth - truncationMessage.length()) / 2); i++) {
      before += ".";
    }
    String after = "";
    for (int i = 0; i < tableWidth - before.length() - truncationMessage.length(); i++) {
      after += ".";
    }
    printStream.print("+" + before + truncationMessage + after + "+");
    printStream.println();
  }

  private static int getLengthOfElem(Object elem) {
    if (elem == null) {
      return NULL_STRING.length();
    }

    return elem.toString().length();
  }

  private static void printElem(PrintStream printStream, Object elem, int columnWidth) {
    int length = getLengthOfElem(elem);

    printStream.print(" ");
    if (elem == null) {
      printStream.print(NULL_STRING);
    } else {
      printStream.print(elem.toString());
    }

    for (int i = length; i < columnWidth; i++) {
      printStream.print(" ");
    }
    printStream.print(" |");
  }

  private static void printHorizontalLine(PrintStream printStream, int length) {
    printStream.print("+");
    for (int i = 0; i < length; i++) {
      printStream.print("-");
    }
    printStream.println("+");
  }

}
