#!/bin/bash
set -e


# regenerate menus for specifications:
javac GenerateMenuFromMarkdown.java; java GenerateMenuFromMarkdown pages/pgql-1.1-spec.md _data/sidebars/spec_1_1_sidebar.yml /spec/1.1/

# regenerate anchor points and links for grammar:
javac GeneratePostProcessingRules.java; java GeneratePostProcessingRules

