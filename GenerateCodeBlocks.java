import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateCodeBlocks {
    private static String[] KEYWORDS = { "MATCH", "PATHS", "DATE", "TIME", "TIMEZONE", "INTEGER",
            "BOOLEAN",
            "STRING", "ARRAY_AGG", "LISTAGG", "LABELS", "HAS_LABEL", "LABEL", "ALL_DIFFERENT", "IN_DEGREE",
            "OUT_DEGREE",
            "CEILING", "CEIL", "FLOOR", "ROUND", "JAVA_REGEXP_LIKE", "LOWER", "SUBSTRING", "UPPER", "HOUR", "TOP",
            "SHORTEST",
            "PROPERTIES", "VERTEX", "EDGE", "GRAPH_TABLE", "PROPERTY", "GRAPH", "TABLES", "DESTINATION", "COLUMNS", "CHEAPEST", "COST",
            "ONE", "PER", "STEP", "INTERVAL", "PREFIX", "WALK", "ACYCLIC", "SIMPLE", "TRAIL",
            "LABELED", "KEEP", "ACCESS", "ADD", "ALL", "ALTER", "AND", "ANY", "ASC", "AUDIT", "BETWEEN",
            "BY", "CHAR", "CHECK", "CLUSTER", "COLUMN", "COMMENT", "COMPRESS", "CONNECT",
            "CREATE", "CURRENT", "DATE", "DECIMAL", "DEFAULT", "DELETE", "DESC", "DISTINCT",
            "DROP", "ELSE", "EXCLUSIVE", "EXISTS", "FILE", "FLOAT", "FOR", "FROM", "GRANT",
            "GROUP", "HAVING", "IDENTIFIED", "IN", "INDEX", "INITIAL",
            "INSERT", "INTEGER", "INTERSECT", "INTO", "IS", "LEVEL", "LIKE", "LOCK", "LONG",
            "MINUS", "MODE", "MODIFY",
            "NOT", "NULL", "NUMBER", "OF", "OFFLINE", "ONLINE", "OPTION",
            "ORDER", "OR", "PRIVILEGES", "PUBLIC", "RAW", "RENAME",
            "RESOURCE", "REVOKE", "ROWID", "ROWNUM", "ROWS", "ROW", "SELECT", "SESSION",
            "SET", "SHARE", "SIZE", "SMALLINT", "START", "SUCCESSFUL", "SYNONYM", "SYSDATE",
            "TABLE", "THEN", "TO", "TRIGGER", "UID", "UNION", "UNIQUE", "UPDATE", "USER",
            "VALIDATE", "VALUES", "VARCHAR", "VARCHAR2", "VIEW", "WHENEVER", "WHERE", "WITH", 
            "SUM", "ABS", "ID", "PATH", "ON", "AS"};

    private static String regex;
    
    
    public static class StringLengthComparator implements Comparator<String> { 
        @Override 
        public int compare(String s1, String s2) { 
            return Integer.compare(s2.length(), s1.length()); 
        } 
    }
    
    public static void convertMarkdownToHTML(String inputFilePath, String outputFilePath) {
        try {
            // Create BufferedReader to read input file
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));

            // Create BufferedWriter to write output file
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));

            // Initialize flag to track if within code block
            boolean inCodeBlock = false;

            // Placeholder HTML code
            // String htmlStart = "<div class=\"code-block\">";
            String buttonCode = "<div class=\"tab\">\n" +
                    "<button name=\"sql-button\" class=\"tablinks active\" onclick=\"openTab(event, 'sql')\">PGQL with SQL Standard syntax</button>\n"
                    +
                    "<button name=\"pgql-button\" class=\"tablinks\" onclick=\"openTab(event, 'pgql')\">PGQL with custom syntax</button>\n"
                    +
                    "</div>";
            // String htmlEnd = "</div>";
            String codeBlock = "";
            String pgql = "";
            String sql = "";
            String[] tosplit;
            
            // Loop through each line in the file
            String line;
            while ((line = reader.readLine()) != null) {
                // Check if line starts a code block
                if (line.startsWith("```sql")) {
                    inCodeBlock = true;
                } else if (inCodeBlock && line.startsWith("```")) {
                    tosplit = codeBlock.split("(?i)--SQL");
                    if (tosplit.length == 2) {
                        writer.write(buttonCode);
                        pgql = getHtmlForQuery(tosplit[0].substring(6), true) + "\n";
                        sql = getHtmlForQuery(tosplit[1], false) + "\n";
                    } else {
                        pgql = "```sql\n" + tosplit[0] + "```\n";
                        sql = "";
                    }
                    writer.write(sql);
                    writer.write(pgql);
                    codeBlock = "";
                    inCodeBlock = false;// Reset codeBlock for next block
                } else {
                    // If within a code block, accumulate code lines
                    if (inCodeBlock) {
                        codeBlock += line + "\n"; // Add newline character
                    } else {
                        // If not in a code block, write the line as is
                        if (line.startsWith("#permalink:"))
                            line = line.substring(1);
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }

            // Close reader and writer
            reader.close();
            writer.close();

            System.out.println("Conversion completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getHtmlForQuery(String query, boolean isPgql) {
        return "<div name=\"" + (isPgql ? "pgql" : "sql") + "\" class=\"tab-content" + (isPgql ? "" : " active") + "\">"
                +
                "<div class=\"language-sql highlighter-rouge\"><div class=\"highlight\"><pre class=\"highlight\">"
                + replaceWithHTML(query)
                + "</pre></div></div></div>";
    }


    public static String replaceWithHTML(String input) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String match = matcher.group();
            String replacement;

            if (match.matches("('.*?'|\\b\\d+\\b)")) {
                replacement = "<span class=\"mi\">" + match + "</span>";
            } else if (match.matches("(?=([^'\"]|'[^']*'|\"[^\"]*\")*$)([\\+\\-\\*\\/=%><\\|])")) {
                replacement = "<span class=\"o\">" + match + "</span>";
            } else if (isKeyword(match)) {
                replacement = "<span class=\"k\">" + match + "</span>";
            }else{
                replacement = match;
            }

            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static boolean isKeyword(String str) {
        for (String keyword : KEYWORDS) {
            if (str.equals(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        // Provide input and output file paths
        String inputFilePath = "./pre-pages/pgql-2.0-spec.md";
        String outputFilePath = "./pages/pgql-2.0-spec.md";

        Arrays.sort(KEYWORDS, new StringLengthComparator()); 
        regex =  "('.*?'|\\b\\d+\\b)|(?=([^'\"]|'[^']*'|\"[^\"]*\")*$)([\\+\\-\\*\\/=%><\\|])|(?:" + String.join("|", KEYWORDS) + ")";
        // Call the convertMarkdownToHTML method
        convertMarkdownToHTML(inputFilePath, outputFilePath);
    }
}
