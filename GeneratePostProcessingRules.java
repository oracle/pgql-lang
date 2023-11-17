
/*
 * Copyright (C) 2013 - 2019 Oracle and/or its affiliates. All rights reserved.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneratePostProcessingRules {

  private static final String[] ADDITIONAL_KEYWORDS = { "PATH", "PATHS", "DATE", "TIME", "TIMEZONE", "INTEGER", "BOOLEAN",
      "STRING", "ARRAY_AGG", "LISTAGG", "ID", "LABELS", "LABEL", "HAS_LABEL", "ALL_DIFFERENT", "IN_DEGREE", "OUT_DEGREE",
      "CEIL", "CEILING", "FLOOR", "ROUND", "JAVA_REGEXP_LIKE", "LOWER", "SUBSTRING", "UPPER", "HOUR", "TOP", "SHORTEST",
      "PROPERTIES", "VERTEX", "EDGE", "PROPERTY", "GRAPH", "TABLES", "DESTINATION", "COLUMNS", "CHEAPEST", "COST",
      "ONE", "ROW", "PER", "STEP", "INTERVAL", "PREFIX", "GRAPH_TABLE", "WALK", "ACYCLIC", "SIMPLE", "TRAIL",
      "LABELED", "KEEP" };

  private static final String[] NO_KEYWORDS = { "source", "Class", "c", "location", "g", "level", "Result", "owner", "connection" };

  private static final String RULE_IDENTIFIER = "[A-Za-z][A-Za-z0-9_]*";

  private static final Pattern RULE_DECLARATION_START = Pattern.compile(RULE_IDENTIFIER + "( )+" + "::");

  public static void main(String[] args) throws Exception {

    if (args.length != 0) {
      System.out.println("No arguments expected");
      System.exit(1);
    }

    StringBuilder sb = new StringBuilder();

    for (String keyword : ADDITIONAL_KEYWORDS) {
      String from = "<span class=\"n\">" + keyword + "</span>";
      String to = "<span class=\"k\">" + keyword + "</span>";
      sb.append(toReplacement(from, to));
    }

    for (String keyword : NO_KEYWORDS) {
      String from = "<span class=\"k\">" + keyword + "</span>";
      String to = "<span class=\"n\">" + keyword + "</span>";
      sb.append(toReplacement(from, to));
    }

    generateForVersion("1.1", sb);
    generateForVersion("1.2", sb);
    generateForVersion("1.3", sb);
    generateForVersion("1.4", sb);
    generateForVersion("1.5", sb);
    generateForVersion("2.0", sb);

    String originalContent = new String(Files.readAllBytes(Paths.get("_layouts/page_orig.html")));
    String newContent = originalContent.replaceFirst("\\{\\{content\\}\\}", "{{content\n" + sb.toString() + "}}");

    Files.write(Paths.get("_layouts/page.html"), newContent.getBytes());
  }

  private static void generateForVersion(String pgqlVersion, StringBuilder sb) throws Exception {
    File f = new File("pages/pgql-" + pgqlVersion + "-spec.md");
    BufferedReader b = new BufferedReader(new FileReader(f));

    String line = "";
    while ((line = b.readLine()) != null) {
      String sortDefinitionStart = matchPattern(line, RULE_DECLARATION_START);
      // e.g. "MatchClause ::="
      if (sortDefinitionStart != null) {
        String sortDefinition = sortDefinitionStart.replaceAll(" ", "").replaceAll("::", "");
        String sortDefinitionWithAnchorPoint = sortDefinitionStart.replaceAll(" ::",
            "<a name=\"" + sortDefinition + "\" class=\"pgql-anchor\"> </a>::");
        String sortDefinitionReplacement = toReplacement(sortDefinitionStart, sortDefinitionWithAnchorPoint);
        sb.append(sortDefinitionReplacement);
        String sortReferenceReplacement = toReplacement("&lt;" + sortDefinition + "&gt;",
            "<a href=\"#" + sortDefinition + "\">" + sortDefinition + "</a>");
        sb.append(sortReferenceReplacement);
      }
    }

    b.close();
  }

  private static String matchPattern(String s, Pattern pattern) {
    Matcher matcher = pattern.matcher(s);
    if (matcher.find()) {
      return matcher.group();
    }
    return null;
  }

  private static String toReplacement(String from, String to) {
    return "    | replace:'" + from + "','" + to + "'\n";
  }
}
