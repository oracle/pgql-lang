/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResultSetFormatter {

  private static final String NULL_STRING = "<null>";
  private static final long DEFAULT_PRINT_LIMIT = 1000;

  /**
   * Prints the specified number of results in the ResultSet to the given
   * PrintStream, starting from the provided index.
   * 
   * @param resultSet <code>ResultSet</code> containing results
   * @param printStream <code>PrintStream</code> where the results should be
   *        printed
   * @param numResults number of results
   * @param from index of first result that should be printed
   * @throws PgqlException if a connection error occurs or when this method is
   *         called on a closed result set
   */
  public static void out(ResultSet<? extends ResultAccess> resultSet, PrintStream printStream, long numResults,
      int from) throws PgqlException {
    out(resultSet, printStream, numResults, from, false);
  }

  /**
   * Prints all the results in the ResultSet to <code>System.out</code>.
   * 
   * @param resultSet <code>ResultSet</code> containing results
   * @throws PgqlException if a connection error occurs or when this method is
   *         called on a closed result set
   */
  public static void out(ResultSet<? extends ResultAccess> resultSet) throws PgqlException {
    out(resultSet, System.out, DEFAULT_PRINT_LIMIT, 0, true);
  }

  private static void out(ResultSet<? extends ResultAccess> resultSet, PrintStream printStream, long numResults,
      int from, boolean truncate) throws PgqlException {
    ResultSetMetaData metadata = resultSet.getMetaData();
    int numElements = metadata.getColumnCount();

    int[] columnWidth = new int[numElements];
    for (int i = 0; i < numElements; i++) {
      int length = metadata.getColumnName(i + 1).length();
      if (length > columnWidth[i]) {
        columnWidth[i] = length;
      }
    }

    List<Object[]> rows = new ArrayList<>();
    Iterator<? extends ResultAccess> results = resultSet.iterator();
    for (int i = 0; i < from; i++) {
      resultSet.next();
    }
    int count = 0;
    while (results.hasNext() && count < numResults) {
      ResultAccess result = results.next();
      Object[] row = new Object[numElements];
      rows.add(row);
      for (int i = 0; i < numElements; i++) {
        Object elem = result.getObject(i + 1);
        row[i] = elem;
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
    int countResults = count;
    if (truncate) {
      // Since there is no ResultSet API to get the number of results,
      // we count till the end of the iterator
      while (results.hasNext()) {
        results.next();
        countResults++;
      }

      if (countResults > numResults) {
        truncationMessage = "... truncated " + (countResults - DEFAULT_PRINT_LIMIT) + " rows ...";
        int columnNum = 0;
        while (totalWidth < truncationMessage.length()) {
          if (columnWidth.length > 0) {
            columnWidth[columnNum] += 1;
            columnNum = (columnNum + 1) % columnWidth.length;
          }
          totalWidth++;
        }
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
        printElem(printStream, metadata.getColumnName(i + 1), columnWidth[i]);
      }
    }
    printStream.println();

    printHorizontalLine(printStream, totalWidth);

    for (int idx = 0; idx < count; idx++) {
      printStream.print("|");
      Object[] row = rows.get(idx);
      if (numElements == 0) {
        for (int i = 0; i < totalWidth; i++) {
          printStream.print(" ");
        }
        printStream.print("|"); // zero columns
      } else {
        for (int i = 0; i < numElements; i++) {
          printElem(printStream, row[i], columnWidth[i]);
        }
      }
      printStream.println();
    }

    if (truncate && countResults > numResults) {
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
