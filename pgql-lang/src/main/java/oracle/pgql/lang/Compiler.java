/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.IContext;
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

import oracle.pgql.lang.ir.QueryGraph;

public class Compiler {

  private static final Logger LOG = LoggerFactory.getLogger(Compiler.class);
  private static final String SPOOFAX_BINARY = "pgqllang-0.9.5.spoofax-language";

  private final Spoofax spoofax;
  private final ILanguageImpl pgqlLang;
  private final FileObject dummyProjectDir;
  private final IProject dummyProject;
  
  /**
   * Loads PGQL Spoofax binaries if not done already.
   */
  public Compiler() throws CompileException {
    try {
      spoofax = new Spoofax();
      String jarLocation = URLDecoder
          .decode(Compiler.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
      FileObject jarFile = spoofax.resourceService.resolve("jar:" + jarLocation + "!/" + SPOOFAX_BINARY);
      assert (jarFile.exists());
      FileObject spoofaxBinary = VFS.getManager().createFileSystem("jar", jarFile);
      assert (spoofaxBinary.exists());
      Iterable<ILanguageDiscoveryRequest> requests = spoofax.languageDiscoveryService.request(spoofaxBinary);
      Iterable<ILanguageComponent> components = spoofax.languageDiscoveryService.discover(requests);
      Set<ILanguageImpl> implementations = LanguageUtils.toImpls(components);
      pgqlLang = LanguageUtils.active(implementations);
      assert (pgqlLang != null);
      dummyProjectDir = VFS.getManager().resolveFile("ram://pgql/");
    } catch (MetaborgException | IOException e) {
      throw new CompileException("Failed to initialize PGQL", e);
    }

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
    });
  }

  public Compilation compile(String queryString) throws CompileException {
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
      QueryGraph queryGraph = null;
      if (!queryValid) {
        prettyMessages = getMessages(parseResult.messages(), queryString);
      } else {
        IContext context = spoofax.contextService.get(dummyFile, dummyProject, pgqlLang);
        ISpoofaxAnalyzeUnit analysisResult = null;
        try (IClosableLock lock = context.write()) {
          analysisResult = spoofax.analysisService.analyze(parseResult, context).result();
        }

        queryValid = analysisResult.success();
        if (queryValid) {
          queryGraph = SpoofaxAstToQueryGraph.translate(analysisResult.ast());
        } else {
          prettyMessages = getMessages(analysisResult.messages(), queryString);
        }
      }

      return new Compilation(queryString, queryValid, prettyMessages, queryGraph);
    } catch (IOException | ParseException | AnalysisException | ContextException e) {
      throw new CompileException("Failed to parse PGQL query", e);
    } finally {
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
    sb.append(" at line " + message.region().startRow() + ":");

    String affectedSourceText = null;
    try {
      affectedSourceText = AffectedSourceHelper.affectedSourceText(message.region(), sourceText, "\t");
    } catch (NullPointerException e) {
      // workaround for Spoofax bug, see GM-5111
    }

    if (affectedSourceText == null) {
      sb.append("\t" + message);
    } else {
      sb.append("\n");
      sb.append(affectedSourceText);
      sb.append(message);
    }

    sb.append("\n----------");
    return sb.toString();
  }
}
