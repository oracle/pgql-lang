/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.ParseTableReadException;
import org.metaborg.parsetable.ParseTableVariant;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.jsglr2.JSGLR2Failure;
import org.spoofax.jsglr2.JSGLR2Implementation;
import org.spoofax.jsglr2.JSGLR2Result;
import org.spoofax.jsglr2.JSGLR2Success;
import org.spoofax.jsglr2.JSGLR2Variant;
import org.spoofax.jsglr2.imploder.ImploderVariant;
import org.spoofax.jsglr2.parseforest.IParseForest;
import org.spoofax.jsglr2.parseforest.ParseForestConstruction;
import org.spoofax.jsglr2.parseforest.ParseForestRepresentation;
import org.spoofax.jsglr2.parser.ParserVariant;
import org.spoofax.jsglr2.parser.Position;
import org.spoofax.jsglr2.reducing.Reducing;
import org.spoofax.jsglr2.stack.StackRepresentation;
import org.spoofax.jsglr2.stack.collections.ActiveStacksRepresentation;
import org.spoofax.jsglr2.stack.collections.ForActorStacksRepresentation;
import org.spoofax.jsglr2.tokens.TokenizerVariant;
import org.spoofax.terms.TermFactory;
import org.strategoxt.lang.Context;

import oracle.pgql.lang.completion.PgqlCompletionGenerator;
import oracle.pgql.lang.editor.completion.PgqlCompletion;
import oracle.pgql.lang.editor.completion.PgqlCompletionContext;
import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.StatementType;
import oracle.pgql.lang.metadata.AbstractMetadataProvider;
import pgqllang.trans.get_errors_and_offsets_0_0;
import pgqllang.trans.pgql_trans_0_0;
import pgqllang.trans.trans;

import static oracle.pgql.lang.CheckInvalidJavaComment.checkInvalidJavaComment;
import static oracle.pgql.lang.MetadataToAstUtil.addMetadata;

public class Pgql implements Closeable {

  public static final String NON_BREAKING_WHITE_SPACE_ERROR = "Illegal character '\u00a0' (non-breaking white space)"
      + "; use a normal space instead";

  private static final String NON_BREAKING_WHITE_SPACE = "\u00a0";

  private static final String ERROR_MESSSAGE_INDENTATION = "  ";

  private static final int POS_QUERY_ANNOTATIONS = 9;

  private static final int POS_PGQL_VERSION = 1;

  private static final int POS_BIND_VARIABLE_COUNT = 2;

  private static final int POS_SELECTING_ALL_PROPERTIES = 3;

  private static final PgqlVersion LATEST_VERSION = PgqlVersion.V_1_3_OR_UP;

  private static final String ALLOW_REFERENCING_ANY_PROPERTY_FLAG = "/*ALLOW_REFERENCING_ANY_PROPERTY*/";

  private final JSGLR2Implementation<IParseForest, ?, ?, IStrategoTerm, ?, ?> jsglr2;

  private boolean closed = false;

  /**
   * Loads PGQL Spoofax binaries if not done already.
   *
   * @throws IOException
   */
  public Pgql() throws PgqlException {
    try {
      jsglr2 = getParser(getParseTable());
    } catch (ParseTableReadException | IOException e) {
      throw new PgqlException(e);
    }
    parse("SELECT 'dummy' FROM MATCH (n)"); // make it initialize things
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
    return parse(queryString, null);
  }

  /**
   * Parse a PGQL query (either a SELECT or MODIFY query).
   *
   * @param queryString
   *          PGQL query to parse
   * @param metadataProvider
   *          the metadata provider for enhanced type checking based on graph schema information and other metadata
   * @return parse result holding either an AST or error messages
   * @throws PgqlException
   *           if the query contains errors
   */
  public PgqlResult parse(String queryString, AbstractMetadataProvider metadataProvider) throws PgqlException {
    if (closed) {
      throw new PgqlException("Pgql instance was closed");
    }

    if (queryString.trim().equals("")) {
      String error = "Empty query string";
      return new PgqlResult(queryString, false, error, null, LATEST_VERSION, 0, false, metadataProvider);
    }

    /* Parse */
    JSGLR2Result<IStrategoTerm> parseResult = jsglr2.parseResult(queryString);
    if (!parseResult.isSuccess()) {
      /* Parse error */
      PgqlStatement statement = null;
      Position pos = ((JSGLR2Failure<IStrategoTerm>) parseResult).parseFailure.failureCause.position;
      StringBuilder sb = new StringBuilder();
      toErrorMessage(queryString, pos.line, pos.column - 1, -1, true, null, sb);
      return new PgqlResult(queryString, false, sb.toString(), statement, LATEST_VERSION, 0, false, metadataProvider);
    }
    IStrategoTerm parseAst = ((JSGLR2Success<IStrategoTerm>) parseResult).ast;

    /* Add graph metadata */
    Context c = trans.init(new Context(new ImploderOriginTermFactory(new TermFactory())));
    boolean allowReferencingAnyProperty = queryString.contains(ALLOW_REFERENCING_ANY_PROPERTY_FLAG);
    IStrategoTerm parseAstPlusMetadata = addMetadata(parseAst, metadataProvider, c.getFactory(),
        allowReferencingAnyProperty);

    /* Semantic analysis */
    IStrategoTerm analyzedAstPlusMetadata = pgql_trans_0_0.instance.invoke(c, parseAstPlusMetadata);

    IStrategoTerm errorMessagesT = get_errors_and_offsets_0_0.instance.invoke(c, analyzedAstPlusMetadata);
    String prettyMessages = null;
    boolean queryValid = true;
    if (errorMessagesT.getSubtermCount() > 0) {
      /* Semantic analysis error */
      queryValid = false;
      StringBuilder sb = new StringBuilder();
      int errorNumber = 1;
      for (IStrategoTerm error : errorMessagesT.getSubterms()) {
        if (errorNumber > 1) {
          sb.append("\n\n");
        }
        errorNumber++;

        IStrategoTerm offset = error.getSubterm(0);
        int startOffset = ((IStrategoInt) offset.getSubterm(0)).intValue();
        int endOffset = ((IStrategoInt) offset.getSubterm(1)).intValue();
        int lineNumber = 1;
        int currentOffset = 0;
        int columnNumber = -1;
        int length = -1;
        for (String line : queryString.split("\\n|\\r")) {
          if (currentOffset + line.length() < startOffset) {
            currentOffset += line.length() + 1;
            lineNumber++;
          } else {
            columnNumber = startOffset - currentOffset;
            length = Math.min(endOffset - startOffset, line.length() - columnNumber);
            break;
          }
        }

        String message = ((IStrategoString) error.getSubterm(1)).stringValue();
        toErrorMessage(queryString, lineNumber, columnNumber, length, false, message, sb);
      }

      prettyMessages = sb.toString();
    }
    IStrategoTerm analyzedAst = MetadataToAstUtil.removeMetadata(analyzedAstPlusMetadata);

    PgqlStatement statement = null;
    try {
      statement = SpoofaxAstToGraphQuery.translate(analyzedAst);
    } catch (Exception e) {
      if (e instanceof PgqlException) {
        prettyMessages = e.getMessage();
        queryValid = false;
        return new PgqlResult(queryString, queryValid, prettyMessages, statement, LATEST_VERSION, 0, false,
            metadataProvider);
      } else {
        e.printStackTrace();
      }
    }

    IStrategoTerm queryAnnotations = analyzedAst.getSubtermCount() > POS_QUERY_ANNOTATIONS
        ? analyzedAst.getSubterm(POS_QUERY_ANNOTATIONS)
        : null;
    PgqlVersion pgqlVersion = getPgqlVersion(queryAnnotations, statement);

    if (queryValid) {
      checkInvalidJavaComment(queryString, pgqlVersion);
    }

    int bindVariableCount = getBindVariableCount(queryAnnotations, statement);
    boolean querySelectsAllProperties = querySelectsAllProperties(queryAnnotations, statement);

    return new PgqlResult(queryString, queryValid, prettyMessages, statement, pgqlVersion, bindVariableCount,
        querySelectsAllProperties, metadataProvider);
  }

  @SuppressWarnings("unchecked")
  private JSGLR2Implementation<IParseForest, ?, ?, IStrategoTerm, ?, ?> getParser(IParseTable parseTable) {
    final ParserVariant parserVariant = new ParserVariant(ActiveStacksRepresentation.standard(),
        ForActorStacksRepresentation.standard(), ParseForestRepresentation.standard(),
        ParseForestConstruction.standard(), StackRepresentation.standard(), Reducing.standard(), false);
    final JSGLR2Variant jsglr2Variant = new JSGLR2Variant(parserVariant, ImploderVariant.standard(),
        TokenizerVariant.standard());
    return (JSGLR2Implementation<IParseForest, ?, ?, IStrategoTerm, ?, ?>) jsglr2Variant.getJSGLR2(parseTable);
  }

  private IParseTable getParseTable() throws ParseTableReadException, IOException {
    final InputStream parseTableInputStream = Pgql.class.getClassLoader().getResourceAsStream("sdf.tbl");
    final ParseTableVariant tableVariant = new ParseTableVariant();
    return tableVariant.parseTableReader().read(parseTableInputStream);
  }

  private void toErrorMessage(String queryString, int lineNumber, int columnNumber, int length, boolean parseError,
      String message, StringBuilder sb) {
    sb.append("Error(s) in line " + lineNumber + ":\n\n");
    String line = queryString.split("\\n|\\r")[lineNumber - 1];
    sb.append(ERROR_MESSSAGE_INDENTATION + line + "\n");

    String originText;
    if (parseError) {
      if (line.contains(NON_BREAKING_WHITE_SPACE)
          && line.substring(columnNumber - 1, columnNumber).equals(NON_BREAKING_WHITE_SPACE)) {
        columnNumber--;
        originText = NON_BREAKING_WHITE_SPACE;
        length = 1;
        message = NON_BREAKING_WHITE_SPACE_ERROR;
      } else {
        // parse errors are always 1 character in length, which we improve to include the entire next token
        String[] tokens = line.substring(columnNumber).split("\\s+");
        if (tokens.length > 0) {
          originText = tokens[0];
          message = "Syntax error, '" + originText + "' not expected";
        }
        else {
          originText = line.substring(columnNumber);
          message = "Unexpected end of query";
        }

        length = originText.length();
      }
    } else {
      originText = line.substring(columnNumber, columnNumber + length);
    }

    sb.append(ERROR_MESSSAGE_INDENTATION);
    repeatString(" ", columnNumber, sb);
    repeatString("^", length, sb);
    sb.append("\n");
    sb.append(ERROR_MESSSAGE_INDENTATION + message);
  }

  private static String repeatString(String s, int times, StringBuilder sb) {
    for (int i = 0; i < times; i++) {
      sb.append(s);
    }
    return sb.toString();
  }

  private PgqlVersion getPgqlVersion(IStrategoTerm queryAnnotations, PgqlStatement statement) {
    if (statement == null || (statement.getStatementType() != StatementType.SELECT
        && statement.getStatementType() != StatementType.GRAPH_MODIFY)) {
      return LATEST_VERSION;
    }

    String pgqlVersionString = ((IStrategoString) queryAnnotations.getSubterm(POS_PGQL_VERSION)).stringValue();
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

  private int getBindVariableCount(IStrategoTerm queryAnnotations, PgqlStatement statement) {
    if (statement == null) {
      return 0;
    }

    if (statement.getStatementType() == StatementType.SELECT
        || statement.getStatementType() == StatementType.GRAPH_MODIFY) {
      return ((IStrategoInt) queryAnnotations.getSubterm(POS_BIND_VARIABLE_COUNT)).intValue();
    }

    return 0;
  }

  private boolean querySelectsAllProperties(IStrategoTerm queryAnnotations, PgqlStatement statement) {
    if (statement == null) {
      return false;
    }

    if (statement.getStatementType() == StatementType.SELECT
        || statement.getStatementType() == StatementType.GRAPH_MODIFY) {
      IStrategoAppl selectingAllPropertiesT = (IStrategoAppl) queryAnnotations.getSubterm(POS_SELECTING_ALL_PROPERTIES);
      return selectingAllPropertiesT.getConstructor().getName().equals("True");
    }

    return false;
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
    // Iterable<ICompletion> spoofaxCompletions = null;
    // synchronized (lock) { spoofaxCompletions = spoofaxComplete(pgqlResult.getSpoofaxParseUnit(), cursor); } // not
    // used yet

    return PgqlCompletionGenerator.generate(pgqlResult, queryString, cursor, ctx);
  }

  @Override
  public void close() {
    closed = true;
  }
}
