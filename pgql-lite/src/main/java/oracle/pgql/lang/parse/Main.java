package oracle.pgql.lang.parse;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.ParseTableReadException;
import org.metaborg.parsetable.ParseTableVariant;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.jsglr2.*;
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
import pgqllang.trans.trans;
import pgqllang.trans.pgql_trans_0_0;
import pgqllang.trans.get_errors_and_offsets_0_0;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class Main {

  private static final String ERROR_MESSSAGE_INDENTATION = "\t";

  public static void main(String[] args) throws IOException, ParseTableReadException {
    if (args.length != 1) {
      System.out.println("USAGE: provide 1 input file as argument.");
      System.exit(1);
    }

    String queryText = Files.readString(Path.of(args[0]));
    IStrategoTerm ast = parseAndImplode(queryText, getParser(getParseTable()));

    Context c = trans.init(new Context(new ImploderOriginTermFactory(new TermFactory())));
    IStrategoTerm analysisResult = pgql_trans_0_0.instance.invoke(c, ast);
    System.out.println(analysisResult);
    System.out.println();

    IStrategoTerm errorMessagesT = get_errors_and_offsets_0_0.instance.invoke(c, analysisResult);
    String errorMessages = "";
    for (IStrategoTerm error : errorMessagesT.getSubterms()) {
      if (!errorMessages.isEmpty()) {
        errorMessages += "\n\n";
      }

      IStrategoTerm offset = error.getSubterm(0);
      int startOffset = ((IStrategoInt) offset.getSubterm(0)).intValue();
      int endOffset = ((IStrategoInt) offset.getSubterm(1)).intValue();
      int lineNumber = 1;
      int currentOffset = 0;
      int columnNumber = -1;
      int length = -1;
      for (String line : queryText.lines().collect(Collectors.toList())) {
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
      errorMessages += toErrorMessage(queryText, lineNumber, columnNumber, length, false, message);
    }
    System.out.println(errorMessages);
  }

  private static JSGLR2Implementation<IParseForest, ?, ?, IStrategoTerm, ?, ?> getParser(IParseTable parseTable) {
    final ParserVariant parserVariant = new ParserVariant(ActiveStacksRepresentation.standard(),
        ForActorStacksRepresentation.standard(), ParseForestRepresentation.standard(),
        ParseForestConstruction.standard(), StackRepresentation.standard(), Reducing.standard(), false);
    final JSGLR2Variant jsglr2Variant = new JSGLR2Variant(parserVariant, ImploderVariant.standard(),
        TokenizerVariant.standard());
    return (JSGLR2Implementation<IParseForest, ?, ?, IStrategoTerm, ?, ?>) jsglr2Variant.getJSGLR2(parseTable);
  }

  private static IParseTable getParseTable() throws ParseTableReadException, IOException {
    final InputStream parseTableInputStream = Main.class.getClassLoader().getResourceAsStream("sdf.tbl");
    final ParseTableVariant tableVariant = new ParseTableVariant();
    return tableVariant.parseTableReader().read(parseTableInputStream);
  }

  private static IStrategoTerm parseAndImplode(String query,
      JSGLR2Implementation<?, ?, ?, IStrategoTerm, ?, ?> jsglr2) {
    JSGLR2Result<IStrategoTerm> result = jsglr2.parseResult(query);

    if (result.isSuccess()) {
      return ((JSGLR2Success<IStrategoTerm>) result).ast;
    } else {
      Position pos = ((JSGLR2Failure<IStrategoTerm>) result).parseFailure.failureCause.position;
      System.out.println(toErrorMessage(query, pos.line, pos.column - 1, -1, true, null));
      System.exit(1);
      return null;
    }
  }

  private static String toErrorMessage(String query, int lineNumber, int columnNumber, int length, boolean parseError,
      String message) {
    if (query.isBlank()) {
      return "Query text is empty";
    }

    String result = "Error(s) in line " + lineNumber + ":\n\n";
    String line = query.lines().skip(lineNumber - 1).findFirst().get();
    result += ERROR_MESSSAGE_INDENTATION + line + "\n";

    String originText;
    if (parseError) {
      // parse errors are always 1 character in length, which we improve to include the entire next token
      originText = line.substring(columnNumber).split("\\s+")[0];
      length = originText.length();
    } else {
      originText = line.substring(columnNumber, columnNumber + length);
    }

    result += ERROR_MESSSAGE_INDENTATION + " ".repeat(columnNumber) + "^".repeat(length) + "\n";

    if (parseError)
      result += ERROR_MESSSAGE_INDENTATION + "Syntax error, '" + originText + "' not expected";
    else
      result += message;

    return result;
  }
}
