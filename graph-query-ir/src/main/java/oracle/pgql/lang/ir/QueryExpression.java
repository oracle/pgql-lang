/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;
import static oracle.pgql.lang.ir.PgqlUtils.printTime;
import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;
import static oracle.pgql.lang.ir.PgqlUtils.printPgqlDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.List;
import java.util.stream.Collectors;

public interface QueryExpression {

  enum ExpressionType {

    // constants
    INTEGER,
    DECIMAL,
    STRING,
    BOOLEAN,
    DATE,
    TIME,
    TIMESTAMP,
    TIME_WITH_TIMEZONE,
    TIMESTAMP_WITH_TIMEZONE,

    // arithmetic expressions
    SUB,
    ADD,
    MUL,
    DIV,
    MOD,
    UMIN,

    // logical expressions
    AND,
    OR,
    NOT,

    // relational expressions
    EQUAL,
    NOT_EQUAL,
    GREATER,
    GREATER_EQUAL,
    LESS,
    LESS_EQUAL,

    // aggregates
    AGGR_COUNT,
    AGGR_MIN,
    AGGR_MAX,
    AGGR_SUM,
    AGGR_AVG,
    AGGR_ARRAY_AGG,

    // other
    VARREF,
    BIND_VARIABLE,
    STAR,
    SCALAR_SUBQUERY,

    // built-in functions
    PROP_ACCESS,
    CAST,
    EXISTS,
    FUNCTION_CALL,
    EXTRACT_EXPRESSION
  }

  ExpressionType getExpType();

  void accept(QueryExpressionVisitor v);

  abstract class UnaryExpression implements QueryExpression {

    private QueryExpression exp;

    public UnaryExpression(QueryExpression exp) {
      this.exp = exp;
    }

    public QueryExpression getExp() {
      return exp;
    }

    public void setExp(QueryExpression exp) {
      this.exp = exp;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      UnaryExpression that = (UnaryExpression) o;

      return exp.equals(that.exp);
    }

    @Override
    public int hashCode() {
      return 31;
    }
  }

  abstract class BinaryExpression implements QueryExpression {

    private QueryExpression exp1;

    private QueryExpression exp2;

    public BinaryExpression(QueryExpression exp1, QueryExpression exp2) {
      this.exp1 = exp1;
      this.exp2 = exp2;
    }

    public QueryExpression getExp1() {
      return exp1;
    }

    public void setExp1(QueryExpression exp1) {
      this.exp1 = exp1;
    }

    public QueryExpression getExp2() {
      return exp2;
    }

    public void setExp2(QueryExpression exp2) {
      this.exp2 = exp2;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      BinaryExpression that = (BinaryExpression) o;

      if (!exp1.equals(that.exp1)) {
        return false;
      }
      return exp2.equals(that.exp2);
    }

    @Override
    public int hashCode() {
      return 31;
    }
  }

  interface ArithmeticExpression extends QueryExpression {

    class Sub extends BinaryExpression implements ArithmeticExpression {
      public Sub(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.SUB;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " - " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class Add extends BinaryExpression implements ArithmeticExpression {
      public Add(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.ADD;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " + " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class Mul extends BinaryExpression implements ArithmeticExpression {
      public Mul(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.MUL;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " * " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class Div extends BinaryExpression implements ArithmeticExpression {
      public Div(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.DIV;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " / " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class Mod extends BinaryExpression implements ArithmeticExpression {
      public Mod(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.MOD;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " % " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class UMin extends UnaryExpression implements ArithmeticExpression {
      public UMin(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.UMIN;
      }

      @Override
      public String toString() {
        return "-(" + getExp() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }
  }

  interface LogicalExpression extends QueryExpression {

    class And extends BinaryExpression implements LogicalExpression {
      public And(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AND;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " AND " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class Or extends BinaryExpression implements LogicalExpression {
      public Or(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.OR;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " OR " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class Not extends UnaryExpression implements LogicalExpression {
      public Not(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.NOT;
      }

      @Override
      public String toString() {
        return "(NOT " + getExp() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }
  }

  interface RelationalExpression extends QueryExpression {

    class Equal extends BinaryExpression implements RelationalExpression {
      public Equal(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.EQUAL;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " = " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class NotEqual extends BinaryExpression implements RelationalExpression {
      public NotEqual(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.NOT_EQUAL;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " != " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class Greater extends BinaryExpression implements RelationalExpression {
      public Greater(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.GREATER;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " > " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class GreaterEqual extends BinaryExpression implements RelationalExpression {
      public GreaterEqual(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.GREATER_EQUAL;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " >= " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class Less extends BinaryExpression implements RelationalExpression {
      public Less(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.LESS;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " < " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class LessEqual extends BinaryExpression implements RelationalExpression {
      public LessEqual(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.LESS_EQUAL;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " <= " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }
  }

  abstract class Constant<T> implements QueryExpression {

    protected T value;

    public Constant(T value) {
      this.value = value;
    }

    public T getValue() {
      return value;
    }

    public void setValue(T value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Constant<?> constant = (Constant<?>) o;

      return value.equals(constant.value);
    }

    @Override
    public int hashCode() {
      return 31;
    }

    public static class ConstInteger extends Constant<Long> {
      public ConstInteger(long val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.INTEGER;
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    public static class ConstDecimal extends Constant<Double> {
      public ConstDecimal(double val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.DECIMAL;
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return printPgqlDecimal(getValue());
      }
    }

    public static class ConstString extends Constant<String> {
      public ConstString(String val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.STRING;
      }

      @Override
      public String toString() {
        return printPgqlString(value);
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    public static class ConstBoolean extends Constant<Boolean> {

      public ConstBoolean(boolean val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.BOOLEAN;
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    public static class ConstDate extends Constant<LocalDate> {

      public ConstDate(LocalDate val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.DATE;
      }

      @Override
      public String toString() {
        return "DATE '" + value + "'";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    public static class ConstTime extends Constant<LocalTime> {

      public ConstTime(LocalTime val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.TIME;
      }

      @Override
      public String toString() {
        return "TIME '" + printTime(value) + "'";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    public static class ConstTimestamp extends Constant<LocalDateTime> {

      public ConstTimestamp(LocalDateTime val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.TIMESTAMP;
      }

      @Override
      public String toString() {
        return "TIMESTAMP '" + value.toLocalDate() + " " + printTime(value.toLocalTime()) + "'";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    public static class ConstTimeWithTimezone extends Constant<OffsetTime> {

      public ConstTimeWithTimezone(OffsetTime val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.TIME_WITH_TIMEZONE;
      }

      @Override
      public String toString() {
        return "TIME '" + printTime(value.toLocalTime()) + value.getOffset() + "'";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    public static class ConstTimestampWithTimezone extends Constant<OffsetDateTime> {

      public ConstTimestampWithTimezone(OffsetDateTime val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.TIMESTAMP_WITH_TIMEZONE;
      }

      @Override
      public String toString() {
        return "TIMESTAMP '" + value.toLocalDate() + " " + printTime(value.toLocalTime()) + value.getOffset() + "'";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }
  }

  class VarRef implements QueryExpression {
    private QueryVariable variable;

    public VarRef(QueryVariable variable) {
      this.variable = variable;
    }

    public QueryVariable getVariable() {
      return variable;
    }

    public void setVariable(QueryVariable variable) {
      this.variable = variable;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.VARREF;
    }

    @Override
    public String toString() {
      return printPgqlString(variable);
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      VarRef varRef = (VarRef) o;

      return variable.equals(varRef.variable);
    }

    @Override
    public int hashCode() {
      return 31;
    }
  }

  class BindVariable implements QueryExpression {
    private int parameterIndex;

    public BindVariable(int parameterIndex) {
      this.parameterIndex = parameterIndex;
    }

    public int getParameterIndex() {
      return parameterIndex;
    }

    public void setParameterIndex(int parameterIndex) {
      this.parameterIndex = parameterIndex;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.BIND_VARIABLE;
    }

    @Override
    public String toString() {
      return "?";
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      BindVariable other = (BindVariable) obj;
      if (parameterIndex != other.parameterIndex)
        return false;
      return true;
    }

    @Override
    public int hashCode() {
      return 31;
    }
  }

  class PropertyAccess implements QueryExpression {
    private QueryVariable variable;
    private String propertyName;

    public PropertyAccess(QueryVariable variable, String propertyName) {
      this.variable = variable;
      this.propertyName = propertyName;
    }

    public QueryVariable getVariable() {
      return variable;
    }

    public void setVariable(QueryVariable variable) {
      this.variable = variable;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public void setPropertyName(String propertyName) {
      this.propertyName = propertyName;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.PROP_ACCESS;
    }

    @Override
    public String toString() {
      return printPgqlString(variable) + "." + printIdentifier(propertyName);
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      PropertyAccess that = (PropertyAccess) o;

      if (!variable.equals(that.variable)) {
        return false;
      }
      return propertyName.equals(that.propertyName);
    }

    @Override
    public int hashCode() {
      return 31;
    }
  }

  interface Function extends QueryExpression {

    class Cast implements Function {

      private QueryExpression exp;

      private String targetTypeName;

      public Cast(QueryExpression exp, String targetTypeName) {
        this.exp = exp;
        this.targetTypeName = targetTypeName;
      }

      public QueryExpression getExp() {
        return exp;
      }

      public void setExp(QueryExpression exp) {
        this.exp = exp;
      }

      public String getTargetTypeName() {
        return targetTypeName;
      }

      public void setTargetTypeName(String targetTypeName) {
        this.targetTypeName = targetTypeName;
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.CAST;
      }

      @Override
      public String toString() {
        String normalizedType = targetTypeName.replace("TIMEZONE", "TIME ZONE"); // TIMEZONE is used in java.sql.*, but
                                                                                 // TIME ZONE is standard SQL
        return "CAST(" + exp + " AS " + normalizedType + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class Exists extends Subquery implements Function {

      public Exists(GraphQuery query) {
        super(query);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.EXISTS;
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return "EXISTS" + super.toString();
      }
    }
  }

  class FunctionCall implements QueryExpression {

    private String packageName;

    private String functionName;

    private List<QueryExpression> args;

    public FunctionCall(String functionName, List<QueryExpression> exps) {
      this(null, functionName, exps);
    }

    public FunctionCall(String packageName, String functionName, List<QueryExpression> exps) {
      this.packageName = packageName;
      this.functionName = functionName;
      this.args = exps;
    }

    public String getPackageName() {
      return packageName;
    }

    public void setPackageName(String packageName) {
      this.packageName = packageName;
    }

    public String getFunctionName() {
      return functionName;
    }

    public void setFunctionName(String functionName) {
      this.functionName = functionName;
    }

    public List<QueryExpression> getArgs() {
      return args;
    }

    public void setArgs(List<QueryExpression> args) {
      this.args = args;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.FUNCTION_CALL;
    }

    @Override
    public String toString() {
      String expressions = args.stream().map(QueryExpression::toString).collect(Collectors.joining(", "));
      String packageNamePart = packageName == null ? "" : packageName + ".";
      return packageNamePart + functionName + "(" + expressions + ")";
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
    }

    @Override
    public int hashCode() {
      return 31;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      FunctionCall other = (FunctionCall) obj;
      if (args == null) {
        if (other.args != null)
          return false;
      } else if (!args.equals(other.args))
        return false;
      if (functionName == null) {
        if (other.functionName != null)
          return false;
      } else if (!functionName.equals(other.functionName))
        return false;
      if (packageName == null) {
        if (other.packageName != null)
          return false;
      } else if (!packageName.equals(other.packageName))
        return false;
      return true;
    }
  }

  class ExtractExpression implements QueryExpression {

    ExtractField field;

    QueryExpression exp;

    public enum ExtractField {
      YEAR,
      MONTH,
      DAY,
      HOUR,
      MINUTE,
      SECOND,
      TIMEZONE_HOUR,
      TIMEZONE_MINUTE
    }

    public ExtractExpression(ExtractField field, QueryExpression exp) {
      this.field = field;
      this.exp = exp;
    }

    public ExtractField getField() {
      return field;
    }

    public QueryExpression getExp() {
      return exp;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.EXTRACT_EXPRESSION;
    }

    @Override
    public String toString() {
      return "EXTRACT(" + getField() + " FROM " + getExp() + ")";
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
    }
  }

  interface Aggregation extends QueryExpression {

    abstract class AbstractAggregation extends UnaryExpression implements Aggregation {

      private boolean distinct;

      public AbstractAggregation(boolean distinct, QueryExpression exp) {
        super(exp);
        this.distinct = distinct;
      }

      public boolean hasDistinct() {
        return distinct;
      }

      public void setDistinct(boolean distinct) {
        this.distinct = distinct;
      }

      @Override
      public String toString() {
        String result;
        switch (getExpType()) {
          case AGGR_COUNT:
            result = "COUNT";
            break;
          case AGGR_MIN:
            result = "MIN";
            break;
          case AGGR_MAX:
            result = "MAX";
            break;
          case AGGR_AVG:
            result = "AVG";
            break;
          case AGGR_SUM:
            result = "SUM";
            break;
          case AGGR_ARRAY_AGG:
            result = "ARRAY_AGG";
            break;
          default:
            throw new IllegalArgumentException("Unexpected expression type: " + getExpType());
        }
        result += "(" + (distinct ? "DISTINCT " : "") + getExp() + ")";
        return result;
      }

      @Override
      public int hashCode() {
        return 31;
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj)
          return true;
        if (!super.equals(obj))
          return false;
        if (getClass() != obj.getClass())
          return false;
        AbstractAggregation other = (AbstractAggregation) obj;
        if (distinct != other.distinct)
          return false;
        return true;
      }
    }

    class AggrCount extends AbstractAggregation {

      public AggrCount(boolean distinct, QueryExpression exp) {
        super(distinct, exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AGGR_COUNT;
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class AggrMin extends AbstractAggregation {

      public AggrMin(boolean distinct, QueryExpression exp) {
        super(distinct, exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AGGR_MIN;
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class AggrMax extends AbstractAggregation {

      public AggrMax(boolean distinct, QueryExpression exp) {
        super(distinct, exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AGGR_MAX;
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class AggrSum extends AbstractAggregation {

      public AggrSum(boolean distinct, QueryExpression exp) {
        super(distinct, exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AGGR_SUM;
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class AggrAvg extends AbstractAggregation {

      public AggrAvg(boolean distinct, QueryExpression exp) {
        super(distinct, exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AGGR_AVG;
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class AggrArrayAgg extends AbstractAggregation {

      public AggrArrayAgg(boolean distinct, QueryExpression exp) {
        super(distinct, exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AGGR_ARRAY_AGG;
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }
  }

  class Star implements QueryExpression {

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.STAR;
    }

    @Override
    public String toString() {
      return "*";
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 31;
    }
  }

  abstract class Subquery implements QueryExpression {

    private GraphQuery query;

    public Subquery(GraphQuery query) {
      this.query = query;
    }

    public GraphQuery getQuery() {
      return query;
    }

    public void setQuery(GraphQuery query) {
      this.query = query;
    }

    public String toString() {
      return "( " + query + " )";
    }

    @Override
    public int hashCode() {
      return 31;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Subquery other = (Subquery) obj;
      if (query == null) {
        if (other.query != null)
          return false;
      } else if (!query.equals(other.query))
        return false;
      return true;
    }
  }

  class ScalarSubquery extends Subquery {

    public ScalarSubquery(GraphQuery query) {
      super(query);
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.SCALAR_SUBQUERY;
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
    }
  }
}
