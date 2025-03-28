package oracle.pgql.lang;

import static oracle.pgql.lang.CommonTranslationUtil.getString;
import static oracle.pgql.lang.CommonTranslationUtil.isSome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.spoofax.interpreter.core.Pair;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.interpreter.terms.TermType;
import org.spoofax.terms.TermVisitor;

import oracle.pgql.lang.ir.PgqlUtils;
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.metadata.AbstractMetadataProvider;
import oracle.pgql.lang.metadata.BinaryOperation;
import oracle.pgql.lang.metadata.DataTypeSynonym;
import oracle.pgql.lang.metadata.EdgeLabel;
import oracle.pgql.lang.metadata.FunctionSignature;
import oracle.pgql.lang.metadata.GraphSchema;
import oracle.pgql.lang.metadata.Label;
import oracle.pgql.lang.metadata.Property;
import oracle.pgql.lang.metadata.UnaryOperation;
import oracle.pgql.lang.metadata.VertexLabel;

public class MetadataToAstUtil {

  private static final int POS_AST_PLUS_METADATA_AST_EXPRESSIONS = 0;

  private static final String AST_PLUS_METADATA_CONSTRUCTOR_NAME = "AstPlusMetadata";

  static IStrategoTerm addMetadata(IStrategoTerm parseAst, AbstractMetadataProvider metadataProvider, ITermFactory f,
      boolean allowReferencingAnyProperty) {
    PgqlVersion pgqlVersion;
    switch (((IStrategoAppl) parseAst).getConstructor().getName()) {
      case "Query":
        pgqlVersion = PgqlVersion.V_1_3_OR_UP;
        break;
      case "Pgql11Query":
        pgqlVersion = PgqlVersion.V_1_1_OR_V_1_2;
        break;
      case "Pgql10Query":
        pgqlVersion = PgqlVersion.V_1_0;
        break;
      default:
        // for DDL statements and other non-query statement, we don't add metadata
        return parseAst;
    }

    if (metadataProvider == null) {
      return parseAst;
    }

    Set<SchemaQualifiedName> graphNames = extractGraphNames(parseAst, pgqlVersion);
    Optional<GraphSchema> graphSchema;
    if (graphNames.size() > 1) {
      // multiple graph references in single query are currently not supported
      // we already generate an error for that during analysis so we can just return here
      return parseAst;
    } else if (graphNames.size() == 1) {
      SchemaQualifiedName graphName = graphNames.iterator().next();
      graphSchema = metadataProvider.getGraphSchema(graphName);
    } else {
      graphSchema = metadataProvider.getGraphSchema();
    }

    Set<String> allTypes = new HashSet<>();

    Optional<List<DataTypeSynonym>> dataTypeSynonyms = metadataProvider.getDataTypeSynonyms();
    allTypes.addAll(extractDataTypesFromCastStatements(parseAst, dataTypeSynonyms));
    Optional<List<FunctionSignature>> functionSignatures = metadataProvider.getFunctionSignatures();
    allTypes.addAll(extractDataTypesFromUdfs(functionSignatures));

    List<IStrategoTerm> metadataTerm = new ArrayList<>();
    if (graphSchema.isPresent()) {

      List<IStrategoTerm> vertexLabelTerms = new ArrayList<>();
      for (VertexLabel vertexLabel : graphSchema.get().getVertexLabels()) {
        vertexLabelTerms.add(translateLabel(vertexLabel, f, allTypes));
      }
      IStrategoAppl vertexLabelsTerm = f.makeAppl("VertexLabels", f.makeList(vertexLabelTerms));
      metadataTerm.add(vertexLabelsTerm);

      List<IStrategoTerm> edgeLabelTerms = new ArrayList<>();
      for (EdgeLabel edgeLabel : graphSchema.get().getEdgeLabels()) {
        edgeLabelTerms.add(translateLabel(edgeLabel, f, allTypes));
      }
      IStrategoAppl edgeLabelsTerm = f.makeAppl("EdgeLabels", f.makeList(edgeLabelTerms));
      metadataTerm.add(edgeLabelsTerm);
    }

    if (metadataProvider.getDefaultStringType().isPresent()) {
      String type = metadataProvider.getDefaultStringType().get();
      IStrategoAppl defaultStringType = f.makeAppl("DefaultStringType", f.makeString(type));
      metadataTerm.add(defaultStringType);
      allTypes.add(type);
    }

    if (metadataProvider.getDefaultShortIntegerType().isPresent()) {
      String type = metadataProvider.getDefaultShortIntegerType().get();
      IStrategoAppl defaultShortIntegerType = f.makeAppl("DefaultShortIntegerType", f.makeString(type));
      metadataTerm.add(defaultShortIntegerType);
      allTypes.add(type);
    }

    if (metadataProvider.getDefaultLongIntegerType().isPresent()) {
      String type = metadataProvider.getDefaultLongIntegerType().get();
      IStrategoAppl defaultLongIntegerType = f.makeAppl("DefaultLongIntegerType", f.makeString(type));
      metadataTerm.add(defaultLongIntegerType);
      allTypes.add(type);
    }

    if (metadataProvider.getDefaultDecimalType().isPresent()) {
      String type = metadataProvider.getDefaultDecimalType().get();
      IStrategoAppl defaultDecimalType = f.makeAppl("DefaultDecimalType", f.makeString(type));
      metadataTerm.add(defaultDecimalType);
      allTypes.add(type);
    }

    allTypes.add("BOOLEAN");
    allTypes.add("DATE");
    allTypes.add("TIME");
    allTypes.add("TIME WITH TIME ZONE");
    allTypes.add("TIMESTAMP");
    allTypes.add("TIMESTAMP WITH TIME ZONE");
    allTypes.add("VERTEX");
    allTypes.add("EDGE");
    allTypes.add("INTERVAL");

    List<Pair<String, String>> allPairsOfTypes = getAllPairsOfTypes(allTypes);
    List<IStrategoTerm> unionTypes = getUnionCompatibleTypes(allPairsOfTypes, metadataProvider, f);
    if (!unionTypes.isEmpty()) {
      metadataTerm.add(f.makeAppl("UnionTypes", f.makeList(unionTypes)));
    }
    List<IStrategoTerm> unaryOperations = getUnaryOperationsWithTypes(allTypes, metadataProvider, f);
    if (!unaryOperations.isEmpty()) {
      metadataTerm.add(f.makeAppl("UnaryOperations", f.makeList(unaryOperations)));
    }
    List<IStrategoTerm> binaryOperations = getBinaryOperationsWithTypes(allPairsOfTypes, metadataProvider, f);
    if (!binaryOperations.isEmpty()) {
      metadataTerm.add(f.makeAppl("BinaryOperations", f.makeList(binaryOperations)));
    }
    List<IStrategoTerm> dataTypeSynonymTerms = getDataTypeSynonyms(dataTypeSynonyms, f);
    if (!dataTypeSynonymTerms.isEmpty()) {
      metadataTerm.add(f.makeAppl("DataTypeSynonyms", f.makeList(dataTypeSynonymTerms)));
    }

    List<IStrategoTerm> functionSignatureTerms = getFunctionSignatures(functionSignatures, f);
    if (!functionSignatureTerms.isEmpty()) {
      metadataTerm.add(f.makeAppl("FunctionSignatures", f.makeList(functionSignatureTerms)));
    }

    if (allowReferencingAnyProperty) {
      metadataTerm.add(f.makeAppl("AllowReferencingAnyProperty"));
    }

    IStrategoAppl metadataExtendedAst = f.makeAppl(AST_PLUS_METADATA_CONSTRUCTOR_NAME, parseAst,
        f.makeList(metadataTerm));

    return metadataExtendedAst;
  }

  static IStrategoTerm translateLabel(Label label, ITermFactory f, Set<String> allTypes) {
    List<IStrategoTerm> propertyTerms = new ArrayList<>();
    for (Property property : label.getProperties()) {
      propertyTerms.add(f.makeAppl("Property", f.makeString(property.getName()), f.makeString(property.getType())));
      allTypes.add(property.getType());
    }

    return f.makeAppl("Label", f.makeString(label.getLabel()), f.makeList(propertyTerms));
  }

  static IStrategoTerm removeMetadata(IStrategoTerm analysisAst) {
    if (((IStrategoAppl) analysisAst).getConstructor().getName().equals(AST_PLUS_METADATA_CONSTRUCTOR_NAME)) {
      return analysisAst.getSubterm(POS_AST_PLUS_METADATA_AST_EXPRESSIONS);
    } else {
      return analysisAst;
    }
  }

  static Set<SchemaQualifiedName> extractGraphNames(IStrategoTerm ast, PgqlVersion pgqlVersion) {

    final Set<SchemaQualifiedName> graphNames = new HashSet<>();

    new TermVisitor() {

      @Override
      public void preVisit(IStrategoTerm t) {
        if (t.getType() == TermType.APPL) {
          String constructor = ((IStrategoAppl) t).getConstructor().getName();

          boolean graphTable = false;
          if (constructor.equals("GraphTable") && isSome(t.getSubterm(0))) {
            graphTable = true;
            t = t.getSubterm(0);
          }

          if (graphTable || constructor.equals("OnClause") || constructor.equals("IntoClause")
              || constructor.equals("Pgql11FromClause")) {
            IStrategoTerm nameT = t.getSubterm(0);
            IStrategoTerm schemaNameT = nameT.getSubterm(0);
            String schemaName = isSome(schemaNameT)
                ? identifierToString(schemaNameT.getSubterm(0).getSubterm(0), pgqlVersion)
                : null;
            String localName = identifierToString(nameT.getSubterm(1), pgqlVersion);
            graphNames.add(new SchemaQualifiedName(schemaName, localName));
          }
        }
      }

    }.visit(ast);

    return graphNames;
  }

  static Set<String> extractDataTypesFromCastStatements(IStrategoTerm ast,
      Optional<List<DataTypeSynonym>> dataTypeSynonyms) {

    final Set<String> dataTypes = new HashSet<>();

    new TermVisitor() {

      @Override
      public void preVisit(IStrategoTerm t) {
        if (t.getType() == TermType.APPL) {
          String constructor = ((IStrategoAppl) t).getConstructor().getName();
          if (constructor.equals("Cast")) {
            IStrategoString dataTypeT = (IStrategoString) t.getSubterm(1);
            String dataType = dataTypeT.stringValue().toUpperCase();

            if (dataTypeSynonyms.isPresent()) {
              // convert aliases to their proper data type names
              for (DataTypeSynonym synonym : dataTypeSynonyms.get()) {
                if (dataType.equals(synonym.getSynonym())) {
                  dataType = synonym.getDataType();
                }
              }
            }

            dataTypes.add(dataType);
          }
        }
      }

    }.visit(ast);

    return dataTypes;
  }

  private static Collection<? extends String> extractDataTypesFromUdfs(
      Optional<List<FunctionSignature>> optionalFunctionSignatures) {
    Set<String> result = new HashSet<>();
    if (optionalFunctionSignatures.isPresent()) {
      List<FunctionSignature> functionSignatures = optionalFunctionSignatures.get();
      for (FunctionSignature signature : functionSignatures) {
        result.addAll(signature.getArgumentTypes());
        result.add(signature.getReturnType());
      }
    }

    return result;
  }

  static String identifierToString(IStrategoTerm t, PgqlVersion pgqlVersion) {
    String constructorName = ((IStrategoAppl) t).getConstructor().getName();
    String identifier = getString(t);
    switch (constructorName) {
      case "RegularIdentifier":
        if (pgqlVersion == PgqlVersion.V_1_0 || pgqlVersion == PgqlVersion.V_1_1_OR_V_1_2) {
          return identifier;
        } else {
          return identifier.toUpperCase();
        }
      case "DelimitedIdentifier":
        String unquotedPart = identifier.substring(1, identifier.length() - 1);
        if (pgqlVersion == PgqlVersion.V_1_0 || pgqlVersion == PgqlVersion.V_1_1_OR_V_1_2) {
          // Java-like escaping rules
          return PgqlUtils.unescapeLegacyPgqlString(unquotedPart, true);
        } else {
          // SQL escaping rules
          return unquotedPart.replaceAll("\"\"", "\"");
        }
      default:
        throw new IllegalStateException("Unsupported identifier type: " + constructorName);
    }
  }

  private static List<Pair<String, String>> getAllPairsOfTypes(Set<String> allTypes) {
    List<Pair<String, String>> result = new ArrayList<>();
    for (String type1 : allTypes) {
      for (String type2 : allTypes) {
        result.add(new Pair<String, String>(type1, type2));
      }
    }
    return result;
  }

  private static List<IStrategoTerm> getUnionCompatibleTypes(List<Pair<String, String>> allPairsOfTypes,
      AbstractMetadataProvider metadataProvider, ITermFactory f) {
    List<IStrategoTerm> unionTypes = new ArrayList<>();
    for (Pair<String, String> pair : allPairsOfTypes) {
      Optional<String> optionalUnionType = metadataProvider.getUnionType(pair.first, pair.second);
      if (optionalUnionType.isPresent()) {
        String unionType = optionalUnionType.get();
        unionTypes
            .add(f.makeAppl("UnionType", f.makeString(pair.first), f.makeString(pair.second), f.makeString(unionType)));
      }
    }
    return unionTypes;
  }

  private static List<IStrategoTerm> getUnaryOperationsWithTypes(Set<String> allTypes,
      AbstractMetadataProvider metadataProvider, ITermFactory f) {
    List<IStrategoTerm> unaryOperationsWithTypes = new ArrayList<>();

    for (String type : allTypes) {
      UnaryOperation[] unaryOperations = UnaryOperation.values();
      for (int i = 0; i < unaryOperations.length; i++) {
        UnaryOperation operation = unaryOperations[i];
        Optional<String> optionalReturnType = metadataProvider.getOperationReturnType(operation, type);
        if (optionalReturnType.isPresent()) {
          String returnType = optionalReturnType.get();
          String constructorName;
          switch (operation) {
            case NOT:
              constructorName = "Not";
              break;
            case UMIN:
              constructorName = "UMin";
              break;
            case SUM:
            case MIN:
            case MAX:
            case AVG:
            case LISTAGG:
              constructorName = operation.name();
              break;
            case ARRAY_AGG:
              constructorName = "ARRAY-AGG";
              break;
            default:
              throw new UnsupportedOperationException("Unsupported operation: " + operation);
          }
          unaryOperationsWithTypes.add(f.makeAppl("UnaryOperation", f.makeString(constructorName), f.makeString(type),
              f.makeString(returnType)));
        }
      }
    }
    return unaryOperationsWithTypes;
  }

  private static List<IStrategoTerm> getBinaryOperationsWithTypes(List<Pair<String, String>> allPairsOfTypes,
      AbstractMetadataProvider metadataProvider, ITermFactory f) {
    List<IStrategoTerm> binaryOperationsWithTypes = new ArrayList<>();
    for (Pair<String, String> pair : allPairsOfTypes) {
      BinaryOperation[] binaryOperations = BinaryOperation.values();
      for (int i = 0; i < binaryOperations.length; i++) {
        BinaryOperation operation = binaryOperations[i];
        Optional<String> optionalReturnType = metadataProvider.getOperationReturnType(operation, pair.first,
            pair.second);
        if (optionalReturnType.isPresent()) {
          String returnType = optionalReturnType.get();
          String constructorName;
          switch (operation) {
            case ADD:
              constructorName = "Add";
              break;
            case SUB:
              constructorName = "Sub";
              break;
            case MUL:
              constructorName = "Mul";
              break;
            case DIV:
              constructorName = "Div";
              break;
            case MOD:
              constructorName = "Mod";
              break;
            case EQUAL:
              constructorName = "Eq";
              break;
            case NOT_EQUAL:
              constructorName = "Neq";
              break;
            case GREATER:
              constructorName = "Gt";
              break;
            case GREATER_EQUAL:
              constructorName = "Gte";
              break;
            case LESS:
              constructorName = "Lt";
              break;
            case LESS_EQUAL:
              constructorName = "Lte";
              break;
            case AND:
              constructorName = "And";
              break;
            case OR:
              constructorName = "Or";
              break;
            case STRING_CONCAT:
              constructorName = "Cct";
              break;
            default:
              throw new UnsupportedOperationException("Unsupported operation: " + operation);
          }
          binaryOperationsWithTypes.add(f.makeAppl("BinaryOperation", f.makeString(constructorName),
              f.makeString(pair.first), f.makeString(pair.second), f.makeString(returnType)));
        }
      }
    }
    return binaryOperationsWithTypes;
  }

  private static List<IStrategoTerm> getDataTypeSynonyms(Optional<List<DataTypeSynonym>> optionalDataTypeSynonyms,
      ITermFactory f) {
    List<IStrategoTerm> dataTypeSynonyms = new ArrayList<>();
    if (optionalDataTypeSynonyms.isPresent()) {
      for (DataTypeSynonym synonym : optionalDataTypeSynonyms.get()) {
        dataTypeSynonyms.add(
            f.makeAppl("DataTypeSynonym", f.makeString(synonym.getSynonym()), f.makeString(synonym.getDataType())));
      }
    }
    return dataTypeSynonyms;
  }

  private static List<IStrategoTerm> getFunctionSignatures(Optional<List<FunctionSignature>> optionalFunctionSignatures,
      ITermFactory f) {
    List<IStrategoTerm> functionSignatures = new ArrayList<>();
    if (optionalFunctionSignatures.isPresent()) {
      for (FunctionSignature function : optionalFunctionSignatures.get()) {
        IStrategoTerm schemaName = function.getSchemaName() == null ? f.makeAppl("None")
            : f.makeAppl("Some", f.makeString(function.getSchemaName()));
        IStrategoTerm packageName = function.getPackageName() == null ? f.makeAppl("None")
            : f.makeAppl("Some", f.makeString(function.getPackageName()));
        IStrategoTerm functionName = f.makeString(function.getFunctionName());

        List<IStrategoTerm> argumentTypes = new ArrayList<>();
        for (String argumentType : function.getArgumentTypes()) {
          argumentTypes.add(f.makeString(argumentType));
        }

        IStrategoTerm returnType = f.makeString(function.getReturnType());
        functionSignatures.add(f.makeAppl("FunctionSignature", schemaName, packageName, functionName,
            f.makeList(argumentTypes), returnType));
      }
    }
    return functionSignatures;
  }
}
