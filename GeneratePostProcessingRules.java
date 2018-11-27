import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneratePostProcessingRules {

  private static final String[] ADDITIONAL_KEYWORDS = { "PATH", "DATE", "TIME", "TIMEZONE", "INTEGER", "BOOLEAN",
      "STRING", "ARRAY_AGG" };

  private static final String[] NO_KEYWORDS = { "source", "Class", "c", "location", "g", "level" };

  private static final String RULE_IDENTIFIER = "[A-Za-z][A-Za-z0-9_]*";

  private static final Pattern RULE_DECLARATION_START = Pattern.compile(RULE_IDENTIFIER + "( )+" + "::");

  public static void main(String[] args) throws Exception {

    if (args.length != 1) {
      System.out.println("One argument expected:\n - PGQL version (e.g. '1.1')");
      System.exit(1);
    }

    String pgqlVersion = args[0];

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
    
    String originalContent = new String ( Files.readAllBytes( Paths.get("_layouts/page_orig.html") ) );
    String newContent = originalContent.replaceFirst("\\{\\{content\\}\\}", "{{content\n" + sb.toString() + "}}");
    
    Files.write(Paths.get("_layouts/page.html"), newContent.getBytes());
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
