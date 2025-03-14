package oracle.pgql.lang.parse;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.ParseTableReadException;
import org.metaborg.parsetable.ParseTableVariant;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;


public class Main {

  private static final String ERROR_MESSSAGE_INDENTATION = "\t";

  public static void main(String[] args) throws IOException, ParseTableReadException {
    if (args.length != 1) {
      System.out.println("USAGE: provide 1 input file as argument.");
      System.exit(1);
    }

    IStrategoTerm ast = parseAndImplode(Files.readString(Path.of(args[0])), getParser(getParseTable()));

    final Context c = trans.init(new Context(new ImploderOriginTermFactory(new TermFactory())));
    final IStrategoTerm result = pgql_trans_0_0.instance.invoke(c, ast);
    System.out.println(result);
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
      System.out.println(toErrorMessage(query, ((JSGLR2Failure<IStrategoTerm>) result).parseFailure.failureCause.position));
      System.exit(1);
      return null;
    }
  }
  
  private static String toErrorMessage(String query, Position position) {
    if (query.isBlank()) {
      return "Query text is empty";
    }

    String result = "Error(s) in line " + position.line + ":\n\n";
    String line = query.lines().skip(position.line - 1).findFirst().get();
    result += ERROR_MESSSAGE_INDENTATION + line + "\n";
    String token = line.substring(position.column - 1).split("\\s+")[0];
    result += ERROR_MESSSAGE_INDENTATION + " ".repeat(position.column - 1) + "^".repeat(token.length()) + "\n";
    result += ERROR_MESSSAGE_INDENTATION + "Syntax error, '" + token + "' not expected";
    return result;
  }
}
