/*
 * Copyright (C) 2013 - 2022 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermType;

import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.QueryType;
import oracle.pgql.lang.ir.QueryVariable;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.SelectQuery;
import oracle.pgql.lang.ir.QueryExpression.BetweenPredicate;
import oracle.pgql.lang.ir.QueryExpression.DateTimeField;
import oracle.pgql.lang.ir.QueryExpression.ExpressionType;
import oracle.pgql.lang.ir.QueryExpression.ExtractExpression;
import oracle.pgql.lang.ir.QueryExpression.IfElse;
import oracle.pgql.lang.ir.QueryExpression.InPredicate;
import oracle.pgql.lang.ir.QueryExpression.Interval;
import oracle.pgql.lang.ir.QueryExpression.IsNull;
import oracle.pgql.lang.ir.QueryExpression.ScalarSubquery;
import oracle.pgql.lang.ir.QueryExpression.SimpleCase;
import oracle.pgql.lang.ir.QueryExpression.SubstringExpression;
import oracle.pgql.lang.ir.QueryExpression.VarRef;
import oracle.pgql.lang.ir.QueryExpression.WhenThenExpression;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstBoolean;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstDate;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstDecimal;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstInteger;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstString;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstTime;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstTimeWithTimezone;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstTimestamp;
import oracle.pgql.lang.ir.QueryExpression.Constant.ConstTimestampWithTimezone;
import oracle.pgql.lang.ir.QueryExpression.ExtractExpression.ExtractField;
import oracle.pgql.lang.ir.QueryExpression.InPredicate.InValueList;
import oracle.pgql.lang.util.SqlDateTimeFormatter;

import static oracle.pgql.lang.SpoofaxAstToGraphQuery.translate;

public class CommonTranslationUtil {

  private static int LOCAL_OR_SCHEMA_QUALIFIED_NAME_SCHEMA_NAME = 0;

  private static int LOCAL_OR_SCHEMA_QUALIFIED_NAME_LOCAL_NAME = 1;

  private static final int POS_EXP_PLUS_TYPE_EXP = 0;
  private static final int POS_BINARY_EXP_LEFT = 0;
  private static final int POS_BINARY_EXP_RIGHT = 1;
  private static final int POS_UNARY_EXP = 0;
  private static final int POS_TERNARY_EXP1 = 0;
  private static final int POS_TERNARY_EXP2 = 1;
  private static final int POS_TERNARY_EXP3 = 2;
  private static final int POS_AGGREGATE_DISTINCT = 0;
  private static final int POS_AGGREGATE_EXP = 1;
  private static final int POS_AGGREGATE_SEPARATOR = 2;
  private static final int POS_VARREF_VARNAME = 0;
  private static final int POS_VARREF_ORIGIN_OFFSET = 1;
  private static final int POS_PROPREF_VARREF = 0;
  private static final int POS_PROPREF_PROPNAME = 1;
  private static final int POS_CAST_EXP = 0;
  private static final int POS_CAST_TARGET_TYPE_NAME = 1;
  private static final int POS_EXISTS_SUBQUERY = 0;
  private static final int POS_SCALARSUBQUERY_SUBQUERY = 0;
  private static final int POS_SUBQUERY = 0;
  private static final int POS_FUNCTION_CALL_PACKAGE_NAME = 0;
  private static final int POS_FUNCTION_CALL_ROUTINE_NAME = 1;
  private static final int POS_FUNCTION_CALL_PACKAGE_NAME_SCHEMA_PART = 0;
  private static final int POS_FUNCTION_CALL_PACKAGE_NAME_PACKAGE_PART = 1;
  private static final int POS_FUNCTION_CALL_EXPS = 2;
  private static final int POS_EXTRACT_FIELD = 0;
  private static final int POS_EXTRACT_EXP = 1;
  private static final int POS_IN_PREDICATE_EXP = 0;
  private static final int POS_IN_PREDICATE_VALUES = 1;
  private static final int POS_IS_NULL_EXP = 0;
  private static final int POS_IF_ELSE_EXP1 = 0;
  private static final int POS_IF_ELSE_EXP2 = 1;
  private static final int POS_IF_ELSE_EXP3 = 2;
  private static final int POS_SIMPLE_CASE_OPERAND = 0;
  private static final int POS_SIMPLE_CASE_WHENTHEN_EXPS = 1;
  private static final int POS_SIMPLE_CASE_ELSE_EXP = 2;
  private static final int POS_SIMPLE_CASE_IFELSE_ALTERNATIVE_REPRESENTATION = 3;
  private static final int POS_WHENTHEN_WHEN = 0;
  private static final int POS_WHENTHEN_THEN = 1;
  private static final int POS_ELSE_EXP = 0;
  private static final int POS_SUBSTRING_EXP = 0;
  private static final int POS_SUBSTRING_START = 1;
  private static final int POS_SUBSTRING_LENGTH = 2;
  private static final int POS_LENGTH_EXP = 0;
  private static final int POS_INTERVAL_VALUE = 0;
  private static final int POS_INTERVAL_DATETIME_FIELD = 1;

  protected static String getString(IStrategoTerm t) {
    while (t.getType() != TermType.STRING) {
      t = t.getSubterm(0); // data values are often wrapped multiple times, e.g. Some(LimitClause("10"))
    }
    return ((IStrategoString) t).stringValue();
  }

  protected static int getInt(IStrategoTerm t) {
    while (t.getType() != TermType.INT) {
      t = t.getSubterm(0); // data values are often wrapped multiple times, e.g. Some(LimitClause("10"))
    }
    return ((IStrategoInt) t).intValue();
  }

  protected static IStrategoTerm getList(IStrategoTerm t) {
    while (t.getType() != TermType.LIST) {
      t = t.getSubterm(0); // data values are often wrapped multiple times, e.g. Some(OrderElems([...]))
    }
    return t;
  }

  protected static boolean isNone(IStrategoTerm t) {
    return t.getType() == TermType.APPL && ((IStrategoAppl) t).getConstructor().getName().equals("None");
  }

  protected static boolean isSome(IStrategoTerm t) {
    return t.getType() == TermType.APPL && ((IStrategoAppl) t).getConstructor().getName().equals("Some");
  }

  protected static IStrategoTerm getSomeValue(IStrategoTerm t) {
    return t.getSubterm(0);
  }

  protected static String getConstructorName(IStrategoTerm t) {
    return ((IStrategoAppl) t).getConstructor().getName();
  }

  protected static SchemaQualifiedName getSchemaQualifiedName(IStrategoTerm schemaQualifiedNameT) {
    IStrategoTerm schemaNameT = schemaQualifiedNameT.getSubterm(LOCAL_OR_SCHEMA_QUALIFIED_NAME_SCHEMA_NAME);
    String schemaName = null;
    if (isSome(schemaNameT)) {
      schemaName = getString(schemaNameT);
    }
    String localName = getString(schemaQualifiedNameT.getSubterm(LOCAL_OR_SCHEMA_QUALIFIED_NAME_LOCAL_NAME));
    return new SchemaQualifiedName(schemaName, localName);
  }

  protected static QueryExpression translateExp(IStrategoTerm t, TranslationContext ctx) throws PgqlException {

    String cons = ((IStrategoAppl) t).getConstructor().getName();

    switch (cons) {
      case "ExpressionPlusType":
        return translateExp(t.getSubterm(POS_EXP_PLUS_TYPE_EXP), ctx);
      case "Sub":
        QueryExpression exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        QueryExpression exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.ArithmeticExpression.Sub(exp1, exp2);
      case "Add":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.ArithmeticExpression.Add(exp1, exp2);
      case "Mul":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.ArithmeticExpression.Mul(exp1, exp2);
      case "Div":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.ArithmeticExpression.Div(exp1, exp2);
      case "Mod":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.ArithmeticExpression.Mod(exp1, exp2);
      case "UMin":
        QueryExpression exp = translateExp(t.getSubterm(POS_UNARY_EXP), ctx);
        return new QueryExpression.ArithmeticExpression.UMin(exp);
      case "And":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.LogicalExpression.And(exp1, exp2);
      case "Or":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.LogicalExpression.Or(exp1, exp2);
      case "Not":
        exp = translateExp(t.getSubterm(POS_UNARY_EXP), ctx);
        return new QueryExpression.LogicalExpression.Not(exp);
      case "Eq":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.RelationalExpression.Equal(exp1, exp2);
      case "Neq":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.RelationalExpression.NotEqual(exp1, exp2);
      case "Gt":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.RelationalExpression.Greater(exp1, exp2);
      case "Gte":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.RelationalExpression.GreaterEqual(exp1, exp2);
      case "Lt":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.RelationalExpression.Less(exp1, exp2);
      case "Lte":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.RelationalExpression.LessEqual(exp1, exp2);
      case "Cct":
        exp1 = translateExp(t.getSubterm(POS_BINARY_EXP_LEFT), ctx);
        exp2 = translateExp(t.getSubterm(POS_BINARY_EXP_RIGHT), ctx);
        return new QueryExpression.ConcatExpression(exp1, exp2);
      case "Integer":
        long l = parseLong(t);
        return new QueryExpression.Constant.ConstInteger(l);
      case "Decimal":
        double d = Double.parseDouble(getString(t));
        return new QueryExpression.Constant.ConstDecimal(d);
      case "String":
      case "Identifier": // identifier "hello" in (n:hello) becomes string 'hello' in has_label(n,
        // 'hello')
        String s = getString(t);
        return new QueryExpression.Constant.ConstString(s);
      case "True":
        return new QueryExpression.Constant.ConstBoolean(true);
      case "False":
        return new QueryExpression.Constant.ConstBoolean(false);
      case "Date":
        s = getString(t);
        LocalDate date;
        try {
          date = LocalDate.parse(s, SqlDateTimeFormatter.SQL_DATE);
        } catch (DateTimeParseException e) {
          // can return any date here; parser already generated an error message for it
          date = LocalDate.MIN;
        }
        return new QueryExpression.Constant.ConstDate(date);
      case "Time":
        s = getString(t);
        try {
          LocalTime time = LocalTime.parse(s, SqlDateTimeFormatter.SQL_TIME);
          return new QueryExpression.Constant.ConstTime(time);
        } catch (DateTimeParseException e) {
          try {
            OffsetTime timeWithTimezone = OffsetTime.parse(s, SqlDateTimeFormatter.SQL_TIME_WITH_TIMEZONE);
            return new QueryExpression.Constant.ConstTimeWithTimezone(timeWithTimezone);
          } catch (DateTimeParseException e2) {
            // can return any time here; parser already generated an error message for it
            return new QueryExpression.Constant.ConstTime(LocalTime.MIN);
          }
        }
      case "Timestamp":
        s = getString(t);
        try {
          LocalDateTime timestamp = LocalDateTime.parse(s, SqlDateTimeFormatter.SQL_TIMESTAMP);
          return new QueryExpression.Constant.ConstTimestamp(timestamp);
        } catch (DateTimeParseException e) {
          try {
            OffsetDateTime timestampWithTimezone = OffsetDateTime.parse(s,
                SqlDateTimeFormatter.SQL_TIMESTAMP_WITH_TIMEZONE);
            return new QueryExpression.Constant.ConstTimestampWithTimezone(timestampWithTimezone);
          } catch (DateTimeParseException e2) {
            // can return any timestamp here; parser already generated an error message for
            // it
            return new QueryExpression.Constant.ConstTimestamp(LocalDateTime.MIN);
          }
        }
      case "Interval":
        String value = getString(t.getSubterm(POS_INTERVAL_VALUE));
        DateTimeField dateTimeField = DateTimeField.valueOf(
            ((IStrategoAppl) t.getSubterm(POS_INTERVAL_DATETIME_FIELD)).getConstructor().getName().toUpperCase());
        return new Interval(value, dateTimeField);
      case "VarRef":
        QueryVariable var = getVariable(ctx, t);
        return new QueryExpression.VarRef(var);
      case "BindVariable":
        int parameterIndex = getInt(t);
        return new QueryExpression.BindVariable(parameterIndex);
      case "PropRef":
        IStrategoTerm varRefT = t.getSubterm(POS_PROPREF_VARREF);
        VarRef varRef = (VarRef) translateExp(varRefT, ctx);
        String propname = getString(t.getSubterm(POS_PROPREF_PROPNAME));
        return new QueryExpression.PropertyAccess(varRef.getVariable(), propname);
      case "Cast":
        exp = translateExp(t.getSubterm(POS_CAST_EXP), ctx);
        String targetTypeName = getString(t.getSubterm(POS_CAST_TARGET_TYPE_NAME));
        return new QueryExpression.Function.Cast(exp, targetTypeName);
      case "CharacterSubstring":
        exp = translateExp(t.getSubterm(POS_SUBSTRING_EXP), ctx);
        QueryExpression startExp = translateExp(t.getSubterm(POS_SUBSTRING_START), ctx);
        IStrategoTerm lengthExpT = t.getSubterm(POS_SUBSTRING_LENGTH);
        QueryExpression lengthExp = isSome(lengthExpT)
            ? translateExp(getSomeValue(lengthExpT).getSubterm(POS_LENGTH_EXP), ctx)
            : null;
        return new SubstringExpression(exp, startExp, lengthExp);
      case "BetweenPredicate":
        exp1 = translateExp(t.getSubterm(POS_TERNARY_EXP1), ctx);
        exp2 = translateExp(t.getSubterm(POS_TERNARY_EXP2), ctx);
        QueryExpression exp3 = translateExp(t.getSubterm(POS_TERNARY_EXP3), ctx);
        return new BetweenPredicate(exp1, exp2, exp3);
      case "Exists":
        IStrategoTerm subqueryT = t.getSubterm(POS_EXISTS_SUBQUERY);
        SelectQuery selectQuery = translateSubquery(ctx, subqueryT);
        return new QueryExpression.Function.Exists(selectQuery);
      case "ScalarSubquery":
        subqueryT = t.getSubterm(POS_SCALARSUBQUERY_SUBQUERY);
        selectQuery = translateSubquery(ctx, subqueryT);
        return new ScalarSubquery(selectQuery);
      case "CallStatement":
      case "FunctionCall":
        String schemaName = null;
        String packageName = null;
        IStrategoTerm optionalPackageDeclT = t.getSubterm(POS_FUNCTION_CALL_PACKAGE_NAME);
        if (isSome(optionalPackageDeclT)) {
          IStrategoTerm packageDeclT = getSomeValue(optionalPackageDeclT);
          IStrategoTerm schemaT = packageDeclT.getSubterm(POS_FUNCTION_CALL_PACKAGE_NAME_SCHEMA_PART);
          if (isSome(schemaT)) {
            schemaName = getString(schemaT);
          }
          packageName = getString(packageDeclT.getSubterm(POS_FUNCTION_CALL_PACKAGE_NAME_PACKAGE_PART));
        }
        String functionName = getString(t.getSubterm(POS_FUNCTION_CALL_ROUTINE_NAME));
        IStrategoTerm argsT = getList(t.getSubterm(POS_FUNCTION_CALL_EXPS));
        List<QueryExpression> args = varArgsToExps(ctx, argsT);
        return new QueryExpression.FunctionCall(schemaName, packageName, functionName, args);
      case "ExtractExp":
        IStrategoAppl fieldT = (IStrategoAppl) t.getSubterm(POS_EXTRACT_FIELD);
        ExtractField field;
        switch (fieldT.getConstructor().getName()) {
          case "Year":
            field = ExtractField.YEAR;
            break;
          case "Month":
            field = ExtractField.MONTH;
            break;
          case "Day":
            field = ExtractField.DAY;
            break;
          case "Hour":
            field = ExtractField.HOUR;
            break;
          case "Minute":
            field = ExtractField.MINUTE;
            break;
          case "Second":
            field = ExtractField.SECOND;
            break;
          case "TimezoneHour":
            field = ExtractField.TIMEZONE_HOUR;
            break;
          case "TimezoneMinute":
            field = ExtractField.TIMEZONE_MINUTE;
            break;
          default:
            throw new IllegalArgumentException();
        }
        IStrategoTerm expT = t.getSubterm(POS_EXTRACT_EXP);
        exp = translateExp(expT, ctx);
        return new ExtractExpression(field, exp);
      case "InPredicate":
        expT = t.getSubterm(POS_IN_PREDICATE_EXP);
        exp = translateExp(expT, ctx);
        IStrategoTerm inValueListT = t.getSubterm(POS_IN_PREDICATE_VALUES);
        QueryExpression inValueList = translateExp(inValueListT, ctx);
        return new InPredicate(exp, inValueList);
      case "IsNull":
        expT = t.getSubterm(POS_IS_NULL_EXP);
        exp = translateExp(expT, ctx);
        return new IsNull(exp);
      case "IfElse":
        exp1 = translateExp(t.getSubterm(POS_IF_ELSE_EXP1), ctx);
        exp2 = translateExp(t.getSubterm(POS_IF_ELSE_EXP2), ctx);
        exp3 = translateExp(t.getSubterm(POS_IF_ELSE_EXP3), ctx);
        return new IfElse(exp1, exp2, exp3);
      case "SimpleCase":
        QueryExpression operandExp = translateExp(t.getSubterm(POS_SIMPLE_CASE_OPERAND), ctx);
        List<WhenThenExpression> whenThenExps = new ArrayList<>();
        for (IStrategoTerm whenThen : t.getSubterm(POS_SIMPLE_CASE_WHENTHEN_EXPS)) {
          QueryExpression when = translateExp(whenThen.getSubterm(POS_WHENTHEN_WHEN), ctx);
          QueryExpression then = translateExp(whenThen.getSubterm(POS_WHENTHEN_THEN), ctx);
          whenThenExps.add(new WhenThenExpression(when, then));
        }
        QueryExpression elseExp = null;
        IStrategoTerm elseExpT = t.getSubterm(POS_SIMPLE_CASE_ELSE_EXP);
        if (isSome(elseExpT)) {
          elseExp = translateExp(getSomeValue(elseExpT).getSubterm(POS_ELSE_EXP), ctx);
        }
        IfElse ifElseAlterantiveRepresentation = (IfElse) translateExp(
            t.getSubterm(POS_SIMPLE_CASE_IFELSE_ALTERNATIVE_REPRESENTATION), ctx);
        return new SimpleCase(operandExp, whenThenExps, elseExp, ifElseAlterantiveRepresentation);
      case "Null":
      case "IllegalNull": // error recovery
        return null;
      case "Array":
        IStrategoTerm arrayValues = t.getSubterm(0);
        int size = arrayValues.getSubtermCount();

        long[] integerValues = new long[size];
        double[] decimalValues = new double[size];
        boolean[] booleanValues = new boolean[size];
        String[] stringValues = new String[size];
        LocalDate[] dateValues = new LocalDate[size];
        LocalTime[] timeValues = new LocalTime[size];
        LocalDateTime[] timestampValues = new LocalDateTime[size];

        ExpressionType arrayElementType = null;

        for (int i = 0; i < size; i++) {
          QueryExpression literal = translateExp(arrayValues.getSubterm(i), ctx);
          switch (literal.getExpType()) {
            case INTEGER:
              if (arrayElementType == null) {
                arrayElementType = ExpressionType.INTEGER;
              }
              long integerValue = ((ConstInteger) literal).getValue();
              integerValues[i] = integerValue;
              decimalValues[i] = (double) integerValue;
              break;
            case DECIMAL:
              arrayElementType = ExpressionType.DECIMAL;
              decimalValues[i] = ((ConstDecimal) literal).getValue();
              break;
            case BOOLEAN:
              arrayElementType = ExpressionType.BOOLEAN;
              booleanValues[i] = ((ConstBoolean) literal).getValue();
              break;
            case STRING:
              arrayElementType = ExpressionType.STRING;
              stringValues[i] = ((ConstString) literal).getValue();
              break;
            case DATE:
              arrayElementType = ExpressionType.DATE;
              dateValues[i] = ((ConstDate) literal).getValue();
              break;
            case TIME:
              arrayElementType = ExpressionType.TIME;
              timeValues[i] = ((ConstTime) literal).getValue();
              break;
            case TIMESTAMP:
              arrayElementType = ExpressionType.TIMESTAMP;
              timestampValues[i] = ((ConstTimestamp) literal).getValue();
              break;
            case TIME_WITH_TIMEZONE:
              arrayElementType = ExpressionType.TIME;
              timeValues[i] = ((ConstTimeWithTimezone) literal).getValue().withOffsetSameInstant(ZoneOffset.UTC)
                  .toLocalTime();
              break;
            case TIMESTAMP_WITH_TIMEZONE:
              arrayElementType = ExpressionType.TIMESTAMP;
              timestampValues[i] = ((ConstTimestampWithTimezone) literal).getValue()
                  .withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
              break;
            default:
              throw new IllegalArgumentException(literal.getExpType().toString());
          }
        }

        switch (arrayElementType) {
          case INTEGER:
            return new InValueList(integerValues);
          case DECIMAL:
            return new InValueList(decimalValues);
          case BOOLEAN:
            return new InValueList(booleanValues);
          case STRING:
            return new InValueList(stringValues);
          case DATE:
            return new InValueList(dateValues);
          case TIME:
            return new InValueList(timeValues);
          case TIMESTAMP:
            return new InValueList(timestampValues);
          default:
            throw new IllegalArgumentException(arrayElementType.toString());
        }
      case "COUNT":
      case "MIN":
      case "MAX":
      case "SUM":
      case "AVG":
      case "ARRAY-AGG":
      case "LISTAGG":
        exp = translateExp(t.getSubterm(POS_AGGREGATE_EXP), ctx);
        boolean distinct = aggregationHasDistinct(t);
        switch (cons) {
          case "COUNT":
            return new QueryExpression.Aggregation.AggrCount(distinct, exp);
          case "MIN":
            return new QueryExpression.Aggregation.AggrMin(distinct, exp);
          case "MAX":
            return new QueryExpression.Aggregation.AggrMax(distinct, exp);
          case "SUM":
            return new QueryExpression.Aggregation.AggrSum(distinct, exp);
          case "AVG":
            return new QueryExpression.Aggregation.AggrAvg(distinct, exp);
          case "ARRAY-AGG":
            return new QueryExpression.Aggregation.AggrArrayAgg(distinct, exp);
          case "LISTAGG":
            String separator = "";
            IStrategoTerm optionalSeparator = t.getSubterm(POS_AGGREGATE_SEPARATOR);
            if (isSome(optionalSeparator)) {
              IStrategoTerm separatorT = getSomeValue(optionalSeparator);
              separator = getString(separatorT);
            }
            return new QueryExpression.Aggregation.AggrListagg(distinct, exp, separator);
          default:
            throw new IllegalArgumentException(cons);
        }
      case "Star":
        return new QueryExpression.Star();
      default:
        throw new UnsupportedOperationException("Expression unsupported: " + t);
    }
  }

  private static SelectQuery translateSubquery(TranslationContext ctx, IStrategoTerm t) throws PgqlException {
    IStrategoTerm subqueryT = t.getSubterm(POS_SUBQUERY);
    GraphQuery query = translate(subqueryT, ctx);
    if (query.getQueryType() == QueryType.SELECT) {
      return (SelectQuery) query;
    } else {
      return null; // error recovery (translation should succeed even for syntactically invalid
      // queries)
    }
  }

  private static boolean aggregationHasDistinct(IStrategoTerm t) {
    return isSome(t.getSubterm(POS_AGGREGATE_DISTINCT));
  }

  protected static QueryVariable getVariable(TranslationContext ctx, IStrategoTerm varRefT) {
    String varName = getString(varRefT.getSubterm(POS_VARREF_VARNAME));
    IStrategoTerm originOffset = null;
    if (varRefT.getSubtermCount() > 1) {
      originOffset = varRefT.getSubterm(POS_VARREF_ORIGIN_OFFSET);
    } else {
      // dangling reference
      return new QueryVertex(varName, false);
    }

    return ctx.getVariable(originOffset);
  }

  // helper method
  protected static long parseLong(IStrategoTerm t) throws PgqlException {
    try {
      return Long.parseLong(getString(t));
    } catch (NumberFormatException e) {
      throw new PgqlException(getString(t) + " is too large to be stored as long");
    }
  }

  // helper method
  protected static int parseInt(IStrategoTerm t) throws PgqlException {
    try {
      return Integer.parseInt(getString(t));
    } catch (NumberFormatException e) {
      throw new PgqlException(getString(t) + " is too large to be stored as int");
    }
  }

  private static List<QueryExpression> varArgsToExps(TranslationContext ctx, IStrategoTerm expsT) throws PgqlException {
    List<QueryExpression> exps = new ArrayList<>();
    for (IStrategoTerm expT : expsT) {
      exps.add(translateExp(expT, ctx));
    }
    return exps;
  }
}
