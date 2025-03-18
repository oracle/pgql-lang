/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package pgqllang.strategies;

import org.strategoxt.lang.Context;

public class Main {
  public static void init(Context context) {

  }

  /* This is a copy of oracle.pgql.lang.ir.PgqlUtils.unescapeLegacyPgqlString()
   * We cannot add a dependency between the two projects. */
  public static String unescapeLegacyPgqlString(String input, boolean identifier) {
    if (input == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    int length = input.length();

    for (int i = 0; i < length; i++) {
      char currentChar = input.charAt(i);

      if (currentChar == '\\' && i + 1 < length) {
        char nextChar = input.charAt(i + 1);
        switch (nextChar) {
          case 'n':
            sb.append('\n');
            i++;
            break;
          case 't':
            sb.append('\t');
            i++;
            break;
          case 'b':
            sb.append('\b');
            i++;
            break;
          case 'r':
            sb.append('\r');
            i++;
            break;
          case 'f':
            sb.append('\f');
            i++;
            break;
          case '\'':
            sb.append('\'');
            i++;
            break;
          case '\"':
            sb.append('\"');
            i++;
            break;
          case '\\':
            sb.append('\\');
            i++;
            break;
          default:
            sb.append('\\').append(nextChar);
            i++;
            break;
        }

      } else if (identifier && currentChar == '\"' && i + 1 < length) {
        char nextChar = input.charAt(i + 1);
        if (nextChar == '\"') {
          sb.append('\"');
          i++;
        }

      } else if (!identifier && currentChar == '\'' && i + 1 < length) {
        char nextChar = input.charAt(i + 1);
        if (nextChar == '\'') {
          sb.append('\'');
          i++;
        }
      } else {
        sb.append(currentChar);
      }
    }
    return sb.toString();
  }
}
