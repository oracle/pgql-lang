#!/bin/bash
set -e

# regenerate menus for specifications:
javac GenerateMenuFromMarkdown.java
java GenerateMenuFromMarkdown 1.1
java GenerateMenuFromMarkdown 1.2
java GenerateMenuFromMarkdown 1.3

# regenerate anchor points and links for grammar:
javac GeneratePostProcessingRules.java
java GeneratePostProcessingRules

