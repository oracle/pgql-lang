#!/bin/bash
set -e

# regenerate menus for specifications:
javac GenerateMenuFromMarkdown.java
java GenerateMenuFromMarkdown 1.1
java GenerateMenuFromMarkdown 1.2
java GenerateMenuFromMarkdown 1.3
java GenerateMenuFromMarkdown 1.4
java GenerateMenuFromMarkdown 1.5
java GenerateMenuFromMarkdown 2.0

# regenerate anchor points and links for grammar:
javac GeneratePostProcessingRules.java
java GeneratePostProcessingRules

