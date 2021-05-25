/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
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
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.concurrent.IClosableLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermType;
import org.spoofax.terms.TermVisitor;

import com.google.common.collect.Lists;

import oracle.pgql.lang.completion.PgqlCompletionGenerator;
import oracle.pgql.lang.editor.completion.PgqlCompletion;
import oracle.pgql.lang.editor.completion.PgqlCompletionContext;
import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.StatementType;
import oracle.pgql.lang.metadata.ModifiedParseUnit;

import static oracle.pgql.lang.CheckInvalidJavaComment.checkInvalidJavaComment;
import static oracle.pgql.lang.CommonTranslationUtil.getString;
import static oracle.pgql.lang.CommonTranslationUtil.isSome;

public class Pgql implements Closeable {

  /**
   * Spoofax is not thread safe, so any method that uses Spoofax should use the lock.
   */
  private static final Object lock = new Object();

  private static final Set<Pgql> instances = new HashSet<>();

  private static final Logger LOG = LoggerFactory.getLogger(Pgql.class);

  private static final String NON_BREAKING_WHITE_SPACE_ERROR = "Illegal character '\u00a0' (non-breaking white space)"
      + "; use a normal space instead";

  private static final String ERROR_MESSSAGE_INDENTATION = "\t";

  private static final String SPOOFAX_BINARIES = "pgql.spoofax-language";

  private static final int POS_PGQL_VERSION = 9;

  private static final int POS_BIND_VARIABLE_COUNT = 10;

  private static final PgqlVersion LATEST_VERSION = PgqlVersion.V_1_3_OR_UP;

  private static boolean isGloballyInitialized = false;

  private static Spoofax spoofax;

  private static ILanguageImpl pgqlLang;

  private static FileObject dummyProjectDir;

  private static IProject dummyProject;

  private static File spoofaxBinaryFile;

  private boolean isInitialized;

  /**
   * Loads PGQL Spoofax binaries if not done already.
   *
   * @throws IOException
   */
  public Pgql() throws PgqlException {
    this(new PgqlConfig(), null);
  }

  public Pgql(SpoofaxModule module, String tmpDir) throws PgqlException {
    synchronized (lock) {
      if (!isGloballyInitialized) {
        initializeGlobalInstance(module, tmpDir);
      }
      instances.add(this);
      isInitialized = true;
    }
  }

  private void initializeGlobalInstance(SpoofaxModule spoofaxModule, String tmpDir) throws PgqlException {
    try {
      // initialize a new Spoofax
      spoofax = new Spoofax(spoofaxModule);

      // copy the PGQL Spoofax binary to the local file system.
      // IMPORTANT: don't replace this with resolveFile("res:...") or resolve("res:...") because VFS will fail to
      // replicate the resource when it's nested inside multiple JAR or WAR files.
      URL inputUrl = getClass().getResource("/" + SPOOFAX_BINARIES);
      spoofaxBinaryFile = tmpDir == null ? File.createTempFile(SPOOFAX_BINARIES, UUID.randomUUID().toString())
          : new File(tmpDir, SPOOFAX_BINARIES + UUID.randomUUID());
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

      parseInternal("SELECT * FROM MATCH (initQuery)"); // make Spoofax initialize the language
    } catch (MetaborgException | IOException e) {
      throw new PgqlException("Failed to initialize PGQL", e);
    }

    isGloballyInitialized = true;
  }

  /**
   * Parse a PGQL query (either a SELECT or MODIFY query).
   *
   * @param queryString
   *          PGQL query to parse
   * @return parse result holding either an AST or error messages
   * @throws PgqlException
   *           if the query contains errors
   */
  public PgqlResult parse(String queryString) throws PgqlException {
    synchronized (lock) {
      checkInitialized();
      return parseInternal(queryString);
    }
  }

  private void checkInitialized() throws PgqlException {
    if (!isInitialized) {
      throw new PgqlException("Pgql instance was closed");
    }
  }

  private PgqlResult parseInternal(String queryString) throws PgqlException {
    if (queryString.equals("")) {
      String error = "Empty query string";
      return new PgqlResult(queryString, false, error, null, null, LATEST_VERSION, 0);
    }

    ITemporaryContext context = null;
    FileObject dummyFile = null;
    try {
      dummyFile = getFileObject(queryString);
      ISpoofaxParseUnit parseResult = parseHelper(queryString, dummyFile);

      String prettyMessages = null;
      boolean queryValid = parseResult.success();
      PgqlStatement statement = null;
      if (queryValid) {
        checkNoMessages(parseResult.messages(), queryString);
      } else {
        prettyMessages = getMessages(parseResult.messages(), queryString);
      }
      if (!parseResult.valid()) {
        return new PgqlResult(queryString, parseResult.valid(), prettyMessages, statement, parseResult, LATEST_VERSION,
            0);
      }

      List<SchemaQualifiedName> graphNames = extractGraphNames(parseResult.ast());
      SchemaQualifiedName graphName = null;
      if (graphNames.size() == 1) { // multiple graph references in single query are currently not supported; we already
                                    // generate an error for that during analysis
        graphName = graphNames.get(0);
      }
      System.out.println(graphName);

      IStrategoAppl metadataExtendedAst = spoofax.termFactory.makeAppl("AstPlusMetadata", parseResult.ast(),
          spoofax.termFactory.makeAppl("None"));
      ISpoofaxParseUnit extendedParseUnit = new ModifiedParseUnit(parseResult, metadataExtendedAst);

      context = spoofax.contextService.getTemporary(dummyFile, dummyProject, pgqlLang);
      ISpoofaxAnalyzeUnit analysisResult = null;
      try (IClosableLock lock = context.write()) {
        analysisResult = spoofax.analysisService.analyze(extendedParseUnit, context).result();
      }

      if (queryValid) {
        queryValid = analysisResult.success();
        if (queryValid) {
          checkNoMessages(analysisResult.messages(), queryString);
        } else {
          prettyMessages = getMessages(analysisResult.messages(), queryString);
        }
      }

      try {
        statement = SpoofaxAstToGraphQuery.translate(analysisResult.ast());
      } catch (Exception e) {
        if (e instanceof PgqlException) {
          prettyMessages = e.getMessage();
          queryValid = false;
          return new PgqlResult(queryString, queryValid, prettyMessages, statement, parseResult, LATEST_VERSION, 0);
        } else {
          LOG.debug("Translation of PGQL failed because of semantically invalid AST");
        }
      }

      PgqlVersion pgqlVersion = getPgqlVersion(analysisResult.ast(), statement);

      if (queryValid) {
        checkInvalidJavaComment(queryString, pgqlVersion);
      }

      int bindVariableCount = getBindVariableCount(analysisResult.ast(), statement);

      return new PgqlResult(queryString, queryValid, prettyMessages, statement, parseResult, pgqlVersion,
          bindVariableCount);
    } catch (IOException | ParseException | AnalysisException | ContextException e) {
      throw new PgqlException("Failed to parse PGQL query", e);
    } finally {
      if (context != null) {
        context.close();
      }
      quietlyDelete(dummyFile);
    }
  }

  private List<SchemaQualifiedName> extractGraphNames(IStrategoTerm ast) {

    final List<SchemaQualifiedName> graphNames = new ArrayList<>();

    new TermVisitor() {

      @Override
      public void preVisit(IStrategoTerm t) {
        if (t.getType() == TermType.APPL && ((IStrategoAppl) t).getConstructor().getName().equals("OnClause")) {
          IStrategoTerm nameT = t.getSubterm(0);
          IStrategoTerm schemaNameT = nameT.getSubterm(0);
          String schemaName = isSome(schemaNameT) ? identifierToString(schemaNameT.getSubterm(0).getSubterm(0)) : null;
          String localName = identifierToString(nameT.getSubterm(1));
          graphNames.add(new SchemaQualifiedName(schemaName, localName));
        }
      }

    }.visit(ast);

    return graphNames;
  }

  private String identifierToString(IStrategoTerm t) {
    String constructorName = ((IStrategoAppl) t).getConstructor().getName();
    String identifier = getString(t);
    switch (constructorName) {
      case "RegularIdentifier":
        return identifier.toUpperCase();
      case "DelimitedIdentifier":
        return identifier.substring(1, identifier.length() - 2).replaceAll("\"\"", "\"");
      default:
        throw new IllegalStateException("Unsupported identifier type: " + constructorName);
    }
  }

  private void checkNoMessages(Iterable<IMessage> messages, String queryString) {
    if (messages.iterator().hasNext()) {
      String prettyMessages = getMessages(messages, queryString);
      throw new IllegalStateException("Error messages not expected: " + prettyMessages);
    }
  }

  private PgqlVersion getPgqlVersion(IStrategoTerm ast, PgqlStatement statement) {
    if (statement == null) {
      return LATEST_VERSION;
    }

    if (statement.getStatementType() == StatementType.CREATE_PROPERTY_GRAPH
        || statement.getStatementType() == StatementType.DROP_PROPERTY_GRAPH
        || statement.getStatementType() == StatementType.CREATE_EXTERNAL_SCHEMA
        || statement.getStatementType() == StatementType.DROP_EXTERNAL_SCHEMA) {
      return PgqlVersion.V_1_3_OR_UP;
    }

    String pgqlVersionString = ((IStrategoString) ast.getSubterm(POS_PGQL_VERSION)).stringValue();
    PgqlVersion pgqlVersion;
    switch (pgqlVersionString) {
      case "v1.0":
        pgqlVersion = PgqlVersion.V_1_0;
        break;
      case "v1.1":
        pgqlVersion = PgqlVersion.V_1_1_OR_V_1_2;
        break;
      case "v1.3":
        pgqlVersion = PgqlVersion.V_1_3_OR_UP;
        break;
      default:
        throw new IllegalArgumentException("Version not recognized: " + pgqlVersionString);
    }

    return pgqlVersion;
  }

  private int getBindVariableCount(IStrategoTerm ast, PgqlStatement statement) {
    if (statement == null) {
      return 0;
    }

    if (statement.getStatementType() == StatementType.SELECT
        || statement.getStatementType() == StatementType.GRAPH_MODIFY) {
      return ((IStrategoInt) ast.getSubterm(POS_BIND_VARIABLE_COUNT)).intValue();
    }

    return 0;
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

  /**
   * Generate code completions, given a (partial) query and cursor location.
   */
  public List<PgqlCompletion> complete(String queryString, int cursor, PgqlCompletionContext ctx) throws PgqlException {
    PgqlResult pgqlResult = null;
    try {
      pgqlResult = parse(queryString);
    } catch (PgqlException e) {
      // spoofax e.g. throws exception for query "SELECT * FROM g MATCH "
    }
    Iterable<ICompletion> spoofaxCompletions = null;
    // synchronized (lock) { spoofaxCompletions = spoofaxComplete(pgqlResult.getSpoofaxParseUnit(), cursor); } // not
    // used yet

    return PgqlCompletionGenerator.generate(pgqlResult, spoofaxCompletions, queryString, cursor, ctx);
  }

  @Override
  public void close() {
    synchronized (lock) {
      isInitialized = false;
      instances.remove(this);
      if (instances.isEmpty()) {
        isGloballyInitialized = false;
        LOG.info("closing the global PGQL instance");

        if (System.getProperty("os.name").startsWith("Windows")) {
          return; // Windows issue, also see http://yellowgrass.org/issue/Spoofax/88
        }

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
  }
}
