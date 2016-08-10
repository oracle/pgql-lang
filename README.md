PGQL's website is based on Jekyll and GitHub pages. You can change any of the files and GitHub will automatically build and deploy the changes.

To host the website locally, first install the necessary tools:

 - `apt-get install ruby2.3 ruby2.3-dev bundler`
 - `bundle install`

Then build and deploy the site:

 - `bundle exec jekyll serve`

Changes to any of the files will automatically be reflected after a few seconds (no need to run the above command again).

To regenerate the menu for a markdown (.md) file:

 - `javac GenerateMenuForMarkdown.java`
 - `java GenerateMenuForMarkdown pages/pgql-1.0-spec.md _data/sidebars/home_sidebar.yml pgql-1.0-specification.html`
