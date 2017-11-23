/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.completion.ICompletion;
import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.config.ISourceConfig;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.ITemporaryContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.Project;
import org.metaborg.core.source.AffectedSourceHelper;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.concurrent.IClosableLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import oracle.pgql.lang.completions.PgqlCompletion;
import oracle.pgql.lang.completions.PgqlCompletionContext;
import oracle.pgql.lang.completions.PgqlCompletionGenerator;
import oracle.pgql.lang.ir.GraphQuery;

public class Pgql implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(Pgql.class);

  private static final String NON_BREAKING_WHITE_SPACE_ERROR = "Illegal character '\u00a0' (non-breaking white space)"
      + "; use a normal space instead";

  private static final String ERROR_MESSSAGE_INDENTATION = "\t";

  private static final String SPOOFAX_BINARIES = "pgql-1.1.spoofax-language";

  private final Spoofax spoofax;

  private final ILanguageImpl pgqlLang;

  private final FileObject dummyProjectDir;

  private final IProject dummyProject;

  private final File spoofaxBinaryFile;

  /**
   * Loads PGQL Spoofax binaries if not done already.
   *
   * @throws IOException
   */
  public Pgql() throws PgqlException {
    try {
      // initialize a new Spoofax
      spoofax = new Spoofax(new PgqlConfig());

      // copy the PGQL Spoofax binary to the local file system.
      // IMPORTANT: don't replace this with resolveFile("res:...") or resolve("res:...") because VFS will fail to
      // replicate the resource when it's nested inside multiple JAR or WAR files.
      URL inputUrl = getClass().getResource(File.separator + SPOOFAX_BINARIES);
      spoofaxBinaryFile = File.createTempFile(SPOOFAX_BINARIES, "");
      FileUtils.copyURLToFile(inputUrl, spoofaxBinaryFile);
      FileObject fileObject = spoofax.resourceService.resolve("jar:" + spoofaxBinaryFile.getAbsolutePath() + "!");

      Iterable<ILanguageDiscoveryRequest> requests = spoofax.languageDiscoveryService.request(fileObject);
      Iterable<ILanguageComponent> components = spoofax.languageDiscoveryService.discover(requests);
      Set<ILanguageImpl> implementations = LanguageUtils.toImpls(components);
      pgqlLang = LanguageUtils.active(implementations);
      assert (pgqlLang != null);
      dummyProjectDir = spoofax.resourceService.resolve("ram://pgql/");

      final LanguageIdentifier id = pgqlLang.id();
      dummyProject = new Project(dummyProjectDir, new IProjectConfig() {

        @Override
        public Collection<LanguageIdentifier> sourceDeps() {
          Set<LanguageIdentifier> sourceDeps = new HashSet<>();
          sourceDeps.add(id);
          return sourceDeps;
        }

        @Override
        public Collection<LanguageIdentifier> javaDeps() {
          return Collections.emptySet();
        }

        @Override
        public Collection<LanguageIdentifier> compileDeps() {
          return Collections.emptySet();
        }

        @Override
        public String metaborgVersion() {
          return null;
        }

        @Override
        public Collection<ISourceConfig> sources() {
          return Collections.emptySet();
        }
      });

      parse("SELECT * MATCH (initQuery)"); // make Spoofax initialize the language
    } catch (MetaborgException | IOException e) {
      throw new PgqlException("Failed to initialize PGQL", e);
    }
  }

  public PgqlResult parse(String queryString) throws PgqlException {
    ITemporaryContext context = null;
    FileObject dummyFile = null;
    try {
      dummyFile = getFileObject(queryString);
      ISpoofaxParseUnit parseResult = parseHelper(queryString, dummyFile);

      String prettyMessages = null;
      boolean queryValid = parseResult.success();
      GraphQuery queryGraph = null;
      if (!queryValid) {
        prettyMessages = getMessages(parseResult.messages(), queryString);
      }

      context = spoofax.contextService.getTemporary(dummyFile, dummyProject, pgqlLang);
      ISpoofaxAnalyzeUnit analysisResult = null;
      try (IClosableLock lock = context.write()) {
        analysisResult = spoofax.analysisService.analyze(parseResult, context).result();
      }

      if (queryValid) {
        queryValid = analysisResult.success();
        prettyMessages = getMessages(analysisResult.messages(), queryString);
      }
      queryGraph = SpoofaxAstToGraphQuery.translate(analysisResult.ast());

      return new PgqlResult(queryString, queryValid, prettyMessages, queryGraph, parseResult);
    } catch (IOException | ParseException | AnalysisException | ContextException e) {
      throw new PgqlException("Failed to parse PGQL query", e);
    } finally {
      if (context != null) {
        context.close();
      }
      quietlyDelete(dummyFile);
    }
  }

  private FileObject getFileObject(String queryString) throws UnsupportedEncodingException, IOException {
    String randomFileName = UUID.randomUUID().toString() + ".pgql";
    FileObject dummyFile = spoofax.resourceService.resolve(dummyProjectDir, randomFileName);
    try (OutputStream out = dummyFile.getContent().getOutputStream()) {
      IOUtils.write(queryString.getBytes("UTF-8"), out);
    }
    return dummyFile;
  }

  private ISpoofaxParseUnit parseHelper(String queryString, FileObject fileObject) throws ParseException {

    ISpoofaxInputUnit input = spoofax.unitService.inputUnit(fileObject, queryString, pgqlLang, null);
    return spoofax.syntaxService.parse(input);
  }

  private static void quietlyDelete(FileObject fo) {
    try {
      if (fo != null && fo.exists()) {
        if (fo.delete() == false) {
          LOG.warn("failed to delete temporary query file: " + fo.getURL().toString());
        }
      }
    } catch (IOException e) {
      LOG.warn("got error while trying to delete temporary query file", e);
    }
  }

  private Iterable<ICompletion> spoofaxComplete(ISpoofaxParseUnit parseResult, int cursor) {
    try {
      return spoofax.completionService.get(cursor, parseResult, false);
    } catch (MetaborgException e) {
      // swallow any exceptions; worst case we don't suggest any completions
      LOG.debug("spoofax completion failed: " + e.getMessage());
    }
    return Collections.emptyList();
  }

  /**
   * Pretty-prints messages (i.e. compiler errors/warnings/notes) into an output stream.
   */
  private static String getMessages(final Iterable<IMessage> messages, String sourceText) {
    StringBuilder sb = new StringBuilder();
    int lineNumber = -1;

    // Reverse the messages to have them in the right order (top to bottom)
    Iterator<IMessage> it = Lists.reverse(Lists.newArrayList(messages.iterator())).iterator();
    while (it.hasNext()) {
      IMessage message = it.next();
      if (message.region() != null) { // null when query string is empty (e.g. "")
        int startRow = message.region().startRow() + 1;
        if (lineNumber != startRow) {
          if (lineNumber != -1) {
            sb.append("\n");
          }
          lineNumber = startRow;
          sb.append("Error(s) in line " + startRow + ":");
        }
      }

      String affectedSourceText;
      try {
        affectedSourceText = AffectedSourceHelper.affectedSourceText(message.region(), sourceText,
            ERROR_MESSSAGE_INDENTATION);
      } catch (NullPointerException e) {
        // workaround for Spoofax bug, see GM-5111
        affectedSourceText = null;
      }

      sb.append("\n\n");

      if (affectedSourceText != null) {
        sb.append(affectedSourceText);
      }

      String m = message.message();
      if (m.contains(" ")) {
        m = NON_BREAKING_WHITE_SPACE_ERROR;
      }
      sb.append(ERROR_MESSSAGE_INDENTATION + m);

      if (it.hasNext()) {
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  public List<PgqlCompletion> generateCompletions(String queryString, int cursor, PgqlCompletionContext ctx)
      throws PgqlException {
    PgqlResult pgqlResult = parse(queryString);
    Iterable<ICompletion> spoofaxCompletions = null;
    // spoofaxCompletions = spoofaxComplete(pgqlResult.getSpoofaxParseUnit(), cursor); // not used yet

    return PgqlCompletionGenerator.generate(pgqlResult, spoofaxCompletions, queryString, cursor, ctx);
  }

  @Override
  public void close() {
    if (spoofax != null) {
      spoofax.close();
    }
    if (spoofaxBinaryFile != null) {
      if (!spoofaxBinaryFile.delete()) {
        LOG.warn("failed to delete Spoofax binary file: " + spoofaxBinaryFile.getAbsolutePath());
      }
    }
  }
}
