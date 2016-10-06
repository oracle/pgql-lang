/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileReplicator;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.config.IProjectConfig;
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

import oracle.pgql.lang.ir.GraphQuery;

public class Pgql {

  private static final Logger LOG = LoggerFactory.getLogger(Pgql.class);
  private static final String NON_BREAKING_WHITE_SPACE_ERROR = "Illegal character '\u00a0' (non-breaking white space)"
      + "; use a normal space instead";
  private static final String SPOOFAX_BINARIES = "pgql-1.0.spoofax-language";

  private final Spoofax spoofax;
  private final ILanguageImpl pgqlLang;
  private final FileObject dummyProjectDir;
  private final IProject dummyProject;

  /**
   * Loads PGQL Spoofax binaries if not done already.
   */
  public Pgql() throws PgqlException {
    // create our own temp dir for storing Spoofax resources such that we can clean up without requiring a Pgql.close()
    // a temp dir should always be random such that there are no conflicts when multiple users use PGQL on the same
    // system
    String baseTmpDir = System.getProperty("java.io.tmpdir");
    File tempDir = new File(baseTmpDir, "vfs_cache" + new Random().nextLong()).getAbsoluteFile();
    try {
      DefaultFileReplicator replicator = new DefaultFileReplicator(tempDir);
      ((DefaultFileSystemManager) VFS.getManager()).setReplicator(replicator);

      // first copy the resource to the local file system.
      // IMPORTANT: don't replace this with VFS.getManager().resolveFile("res:...") because VFS will fail to replicate
      // the resource when it's nested inside multiple JAR or WAR files.
      URL inputUrl = getClass().getResource("/" + SPOOFAX_BINARIES);
      File dest = new File(tempDir, SPOOFAX_BINARIES);
      FileUtils.copyURLToFile(inputUrl, dest);
      FileObject fileObject = VFS.getManager().resolveFile("jar:" + dest.getAbsolutePath() + "!");

      // set up Spoofax
      spoofax = new Spoofax();
      Iterable<ILanguageDiscoveryRequest> requests = spoofax.languageDiscoveryService.request(fileObject);
      Iterable<ILanguageComponent> components = spoofax.languageDiscoveryService.discover(requests);
      Set<ILanguageImpl> implementations = LanguageUtils.toImpls(components);
      pgqlLang = LanguageUtils.active(implementations);
      assert (pgqlLang != null);
      dummyProjectDir = VFS.getManager().resolveFile("ram://pgql/");

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
          return new HashSet<>();
        }

        @Override
        public Collection<LanguageIdentifier> compileDeps() {
          return new HashSet<>();
        }

        @Override
        public String metaborgVersion() {
          return null;
        }

        @Override
        public boolean typesmart() {
          return false;
        }
      });

      parse("select * where (initQuery)"); // make Spoofax initialize the language
    } catch (MetaborgException | IOException e) {
      throw new PgqlException("Failed to initialize PGQL", e);
    } finally {
      try {
        FileUtils.deleteDirectory(tempDir);
      } catch (IOException e) {
        LOG.warn("failed to delete temporary directory: " + tempDir.getAbsolutePath());
      }
    }
  }

  public PgqlResult parse(String queryString) throws PgqlException {
    ITemporaryContext context = null;
    FileObject dummyFile = null;
    try {
      String randomFileName = UUID.randomUUID().toString() + ".pgql";
      dummyFile = VFS.getManager().resolveFile(dummyProjectDir, randomFileName);
      try (OutputStream out = dummyFile.getContent().getOutputStream()) {
        IOUtils.write(queryString.getBytes("UTF-8"), out);
      }

      ISpoofaxInputUnit input = spoofax.unitService.inputUnit(dummyFile, queryString, pgqlLang, null);
      ISpoofaxParseUnit parseResult = spoofax.syntaxService.parse(input);

      String prettyMessages = null;
      boolean queryValid = parseResult.success();
      GraphQuery queryGraph = null;
      if (!queryValid) {
        prettyMessages = getMessages(parseResult.messages(), queryString);
      } else {
        context = spoofax.contextService.getTemporary(dummyFile, dummyProject, pgqlLang);
        ISpoofaxAnalyzeUnit analysisResult = null;
        try (IClosableLock lock = context.write()) {
          analysisResult = spoofax.analysisService.analyze(parseResult, context).result();
        }

        queryValid = analysisResult.success();
        if (queryValid) {
          queryGraph = SpoofaxAstToGraphQuery.translate(analysisResult.ast());
        } else {
          prettyMessages = getMessages(analysisResult.messages(), queryString);
        }
      }

      return new PgqlResult(queryString, queryValid, prettyMessages, queryGraph);
    } catch (IOException | ParseException | AnalysisException | ContextException e) {
      throw new PgqlException("Failed to parse PGQL query", e);
    } finally {
      if (context != null) {
        context.close();
      }
      quietlyDelete(dummyFile);
    }
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

  /**
   * Pretty-prints messages (i.e. compiler errors/warnings/notes) into an output stream. TODO: get rid of this code,
   * Spoofax should natively support it?
   */
  private static String getMessages(final Iterable<IMessage> messages, String sourceText) {
    StringBuilder stringBuilder = new StringBuilder();
    Iterator<IMessage> it = messages.iterator();
    int messageCount = 0;
    while (it.hasNext()) {
      IMessage msg = it.next();
      stringBuilder.append(messageCount + ". " + getMessage(msg, sourceText));
      messageCount++;
    }

    if (messageCount != 0) {
      stringBuilder.append(messageCount + (messageCount == 1 ? " ERROR" : " ERRORS"));
    }
    return stringBuilder.toString();
  }

  /**
   * TODO: get rid of this code, Spoofax should natively support it?
   */
  private static String getMessage(IMessage message, String sourceText) {
    StringBuilder sb = new StringBuilder();
    sb.append(message.severity());
    if (message.region() != null) { // null when query string is empty (e.g. "")
      sb.append(" at line " + message.region().startRow() + ":");
    }

    String affectedSourceText = null;
    try {
      affectedSourceText = AffectedSourceHelper.affectedSourceText(message.region(), sourceText, "\t");
    } catch (NullPointerException e) {
      // workaround for Spoofax bug, see GM-5111
    }

    String m = message.message();
    if (m.contains(" ")) {
      m = NON_BREAKING_WHITE_SPACE_ERROR;
    }

    if (affectedSourceText == null) {
      sb.append("\t" + m);
    } else {
      sb.append("\n");
      sb.append(affectedSourceText);
      sb.append(m);
    }

    sb.append("\n----------");
    return sb.toString();
  }
}
