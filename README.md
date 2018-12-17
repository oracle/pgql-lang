[PGQL's website](http://pgql-lang.org/) is based on Jekyll and GitHub pages. You can change any of the files and GitHub will automatically build and deploy the changes. Changes are reflected after roughly 30 seconds.

To host the website locally, first install the necessary tools:

 - `sudo apt-get install ruby2.5 ruby2.5-dev bundler`
 - `bundle install` (don't use `sudo`)

Then build and deploy the site:

 - `bundle exec jekyll serve`

Changes to any of the files will automatically be reflected after a few seconds (no need to run the above command again).

After changing section titles or grammar snippets, regenerate necessary menus and hyperlinks as follows:

 - `bash generate-menus-and-hyperlinks.sh`

