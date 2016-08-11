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

    if (args.length != 3) {
      System.out.println("Three arguments expected:\n - input .md file (e.g. '/path/to/pages/pgql-1.0-spec.md')\n - destination .yaml file (e.g. 'path/to/_data/sidebars/pgql-1.0-spec.yml')\n - page url (e.g. 'pgql-1.0-specification.html')");
      System.exit(1);
    }
    
    Path markdownFile = Paths.get(args[0]);
    Path yamlFile = Paths.get(args[1]);
    String url_prefix = args[2] + "#";
    
    Files.deleteIfExists(yamlFile);

    Iterator<String> headers = Files.lines(markdownFile).filter(m -> m.startsWith("#")).iterator();
    
    String result = "entries:\n" + "- title: Sidebar\n" + "  product: PGQL 1.0 Specification\n" + "  folders:\n";

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
      result += indent + "url: " + url_prefix + title.toLowerCase().replace(" ", "-").replace("*", "") + "\n";
      result += indent + "output: web\n";
    }

    Files.write(yamlFile, result.getBytes());
  }

  private static String repeatString(String s, int times) {
    return new String(new char[times]).replace("\0", s);
  }
}

