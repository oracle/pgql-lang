/*
 * Copyright (C) 2013 - 2021 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

public class CheckInvalidJavaComment {

  public static void checkInvalidJavaComment(String queryString, PgqlVersion pgqlVersion) throws PgqlException {
    if (pgqlVersion != PgqlVersion.V_1_0 && pgqlVersion != PgqlVersion.V_1_1_OR_V_1_2) {

      char[] chars = queryString.toCharArray();
      int i = 0;
      while (i < chars.length) {
        if (isDoubleQuote(chars, i)) { // identifiers
          i++;
          while (i < chars.length) {
            if (isEscapedDoubleQuote(chars, i)) {
              i = i + 2;
            } else if (isDoubleQuote(chars, i)) {
              i++;
              break;
            } else {
              i++;
            }
          }
        } else if (isSingleQuote(chars, i)) { // literals
          i++;
          while (i < chars.length) {
            if (isEscapedSingleQuote(chars, i)) {
              i = i + 2;
            } else if (isSingleQuote(chars, i)) {
              i++;
              break;
            } else {
              i++;
            }
          }
        } else if (isMultiLineCommentStart(chars, i)) { // multi-line comment /* .. */
          i = i + 2;
          while (i < chars.length) {
            if (isMultiLineCommentEnd(chars, i)) {
              i = i + 2;
              break;
            } else {
              i++;
            }
          }
        } else if (isSingleLineCommentStart(chars, i)) { // single-line comment // ..
          throw new PgqlException("Use /* .. */ instead of // .. to introduce a comment");
        } else {
          i++;
        }
      }
    }
  }

  private static boolean isDoubleQuote(char[] chars, int index) {
    return isCharAtIndex(chars, index, '"');
  }

  private static boolean isEscapedDoubleQuote(char[] chars, int index) {
    return areCharsAtIndex(chars, index, '"', '"');
  }

  private static boolean isSingleQuote(char[] chars, int index) {
    return isCharAtIndex(chars, index, '\'');
  }

  private static boolean isEscapedSingleQuote(char[] chars, int index) {
    return areCharsAtIndex(chars, index, '\'', '\'');
  }

  private static boolean isMultiLineCommentStart(char[] chars, int index) {
    return areCharsAtIndex(chars, index, '/', '*');
  }

  private static boolean isMultiLineCommentEnd(char[] chars, int index) {
    return areCharsAtIndex(chars, index, '*', '/');
  }

  private static boolean isSingleLineCommentStart(char[] chars, int index) {
    return areCharsAtIndex(chars, index, '/', '/');
  }

  private static boolean isCharAtIndex(char[] chars, int index, char c) {
    if (index >= chars.length) {
      return false;
    } else {
      return chars[index] == c;
    }
  }

  private static boolean areCharsAtIndex(char[] chars, int index, char c1, char c2) {
    if (index + 1 >= chars.length) {
      return false;
    } else {
      return chars[index] == c1 && chars[index + 1] == c2;
    }
  }
}
