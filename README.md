[PGQL's website](http://pgql-lang.org/) is based on Jekyll and GitHub pages. You can change any of the files and GitHub will automatically build and deploy the changes. Changes are reflected after roughly 30 seconds.

To host the website locally, first install the necessary tools:

 - `apt-get install ruby2.3 ruby2.3-dev bundler`
 - `bundle install`

Then build and deploy the site:

 - `bundle exec jekyll serve`

Changes to any of the files will automatically be reflected after a few seconds (no need to run the above command again).

To regenerate the menu for a markdown (.md) file:

 - `javac GenerateMenuFromMarkdown.java; java GenerateMenuFromMarkdown pages/pgql-1.1-spec.md _data/sidebars/spec_1_1_sidebar.yml /spec/1.1/`
