[PGQL's website](http://pgql-lang.org/) is based on Jekyll and GitHub pages. You can change any of the files and GitHub will automatically build and deploy the changes. Changes are reflected after roughly 30 seconds.

To host the website locally, make sure to have Docker installed and have configured the correct proxy settings under Preferences>Resources>Proxies.

Then run:

```
docker run --rm --volume="$PWD:/srv/jekyll:Z" -it -p 4005:4005 jekyll/jekyll jekyll serve
```

Once it shows `Generating...` it may seem stuck but you have to be patient (takes ~10 minutes).

Then open [http://localhost:4005/](http://localhost:4005/) in your browser.

Changes to any of the files will automatically be reflected after 30-60 seconds depending on the size of the change.

After changing section titles or grammar snippets, regenerate necessary menus and hyperlinks as follows:

```
bash generate-menus-and-hyperlinks.sh
```

Once in a while we should update the dependencies:

```
export JEKYLL_VERSION=3.8
docker run --rm --volume="$PWD:/srv/jekyll:Z" -it jekyll/jekyll:$JEKYLL_VERSION bundle update
```
