/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class GenerateMenuFromMarkdown {

  private static final String[] ENTRY_TYPES = { "folders", "folderitems", "subfolderitems" };

  public static void main(String[] args) throws IOException {

    if (args.length != 1) {
      System.out.println("One argument expected:\n - PGQL version (e.g. '1.1')");
      System.exit(1);
    }

    String pgqlVersion = args[0];
    Path markdownFile = Paths.get("pages/pgql-" + pgqlVersion + "-spec.md");
    Path yamlFile = Paths.get("_data/sidebars/spec_" + pgqlVersion.replace(".", "_") + "_sidebar.yml");
    String url_prefix = "/spec/" + pgqlVersion + "/#";
    
    Files.deleteIfExists(yamlFile);

    Iterator<String> headers = Files.lines(markdownFile).filter(m -> m.startsWith("#")).iterator();
    
    String result = "entries:\n" + "- title: Sidebar\n" + "  product: PGQL " + pgqlVersion + " Specification\n" + "  folders:\n";

    int level = 1;

    while (headers.hasNext()) {
      String header = headers.next();

      String title = header;
      int newLevel = 0;
      while (title.startsWith("#")) {
        newLevel++;
        title = title.substring(1);
      }
      title = title.trim();

      String indent = repeatString(" ", newLevel * 2 + 2);

      if (newLevel >= 4) {
        continue; // only three levels supported in the menu
      }

      if (newLevel > level) {
        if (newLevel != level + 1) {
          throw new IllegalArgumentException("Remove one or more '#' from '" + header + "'");
        }

        result += indent.substring(2) + ENTRY_TYPES[level] + ":\n\n";
      } else {
        result += "\n";
      }
      level = newLevel;

      result += indent.substring(2) + "- title: " + title + "\n";
      result += indent + "url: " + url_prefix + title.toLowerCase().replace(" ", "-").replace("*", "").replace("/", "").replace("(", "").replace(")", "").replace(".", "").replace(",", "").replace("_", "") + "\n";
      result += indent + "output: web\n";
    }

    Files.write(yamlFile, result.getBytes());
  }

  private static String repeatString(String s, int times) {
    return new String(new char[times]).replace("\0", s);
  }
}

