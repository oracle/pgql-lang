/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

import static java.util.stream.Collectors.joining;
import static oracle.pgql.lang.ir.PgqlUtils.printIdentifier;
import static oracle.pgql.lang.ir.PgqlUtils.printPgqlString;
import static oracle.pgql.lang.ir.PgqlUtils.printLiteral;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    // concat expression
    CONCAT,

    // aggregates
    AGGR_COUNT,
    AGGR_MIN,
    AGGR_MAX,
    AGGR_SUM,
    AGGR_AVG,
    AGGR_ARRAY_AGG,
    AGGR_LISTAGG,

    // other
    VARREF,
    BIND_VARIABLE,
    STAR,
    SCALAR_SUBQUERY,

    // built-in functions and predicates
    PROP_ACCESS,
    CAST,
    EXISTS,
    FUNCTION_CALL,
    EXTRACT_EXPRESSION,
    IN_EXPRESSION,
    IN_VALUE_LIST,
    IS_NULL,
    IF_ELSE,
    SIMPLE_CASE,
    SUBSTRING
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
        return "(" + getExp1() + " <> " + getExp2() + ")";
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

  class ConcatExpression extends BinaryExpression {

    public ConcatExpression(QueryExpression exp1, QueryExpression exp2) {
      super(exp1, exp2);
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.CONCAT;
    }

    @Override
    public String toString() {
      return "(" + getExp1() + " || " + getExp2() + ")";
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
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
      public String toString() {
        return printLiteral(value);
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
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
        return printLiteral(value);
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
        return printLiteral(value);
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
        return printLiteral(value);
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
        return printLiteral(value);
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
        return printLiteral(value);
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
        return printLiteral(value);
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
        Cast other = (Cast) obj;
        if (exp == null) {
          if (other.exp != null)
            return false;
        } else if (!exp.equals(other.exp))
          return false;
        if (targetTypeName == null) {
          if (other.targetTypeName != null)
            return false;
        } else if (!targetTypeName.equals(other.targetTypeName))
          return false;
        return true;
      }
    }

    class Exists extends Subquery implements Function {

      public Exists(SelectQuery query) {
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
      String expressions = args.stream().map(QueryExpression::toString).collect(joining(", "));
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

    public void setExtractField(ExtractField field) {
      this.field = field;
    }

    public QueryExpression getExp() {
      return exp;
    }

    public void setExp(QueryExpression exp) {
      this.exp = exp;
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
      ExtractExpression other = (ExtractExpression) obj;
      if (exp == null) {
        if (other.exp != null)
          return false;
      } else if (!exp.equals(other.exp))
        return false;
      if (field != other.field)
        return false;
      return true;
    }
  }

  class InPredicate implements QueryExpression {

    QueryExpression exp;

    QueryExpression inValueList;

    public InPredicate(QueryExpression exp, QueryExpression inValueList) {
      this.exp = exp;
      this.inValueList = inValueList;
    }

    public QueryExpression getExp() {
      return exp;
    }

    public void setExp(QueryExpression exp) {
      this.exp = exp;
    }

    public void setInValueList(QueryExpression inValueList) {
      this.inValueList = inValueList;
    }

    public QueryExpression getInValueList() {
      return inValueList;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.IN_EXPRESSION;
    }

    @Override
    public String toString() {
      return exp + " IN " + inValueList;
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
      InPredicate other = (InPredicate) obj;
      if (exp == null) {
        if (other.exp != null)
          return false;
      } else if (!exp.equals(other.exp))
        return false;
      if (inValueList == null) {
        if (other.inValueList != null)
          return false;
      } else if (!inValueList.equals(other.inValueList))
        return false;
      return true;
    }

    public static class InValueList implements QueryExpression {

      ExpressionType arrayElementType;

      long[] integerValues;

      double[] decimalValues;

      boolean[] booleanValues;

      String[] stringValues;

      LocalDate[] dateValues;

      LocalTime[] timeValues;

      LocalDateTime[] timestampValues;

      public InValueList(long[] integerValues) {
        this.integerValues = integerValues;
        this.arrayElementType = ExpressionType.INTEGER;
      }

      public InValueList(double[] decimalValues) {
        this.decimalValues = decimalValues;
        this.arrayElementType = ExpressionType.DECIMAL;
      }

      public InValueList(boolean[] booleanValues) {
        this.booleanValues = booleanValues;
        this.arrayElementType = ExpressionType.BOOLEAN;
      }

      public InValueList(String[] stringValues) {
        this.stringValues = stringValues;
        this.arrayElementType = ExpressionType.STRING;
      }

      public InValueList(LocalDate[] dateValues) {
        this.dateValues = dateValues;
        this.arrayElementType = ExpressionType.DATE;
      }

      public InValueList(LocalTime[] timeValues) {
        this.timeValues = timeValues;
        this.arrayElementType = ExpressionType.TIME;
      }

      public InValueList(LocalDateTime[] timestampValues) {
        this.timestampValues = timestampValues;
        this.arrayElementType = ExpressionType.TIMESTAMP;
      }

      public ExpressionType getArrayElementType() {
        return arrayElementType;
      }

      public void setArrayElementType(ExpressionType arrayElementType) {
        this.arrayElementType = arrayElementType;
      }

      public long[] getIntegerValues() {
        return integerValues;
      }

      public void setIntegerValues(long[] integerValues) {
        this.integerValues = integerValues;
      }

      public double[] getDecimalValues() {
        return decimalValues;
      }

      public void setDecimalValues(double[] decimalValues) {
        this.decimalValues = decimalValues;
      }

      public boolean[] getBooleanValues() {
        return booleanValues;
      }

      public void setBooleanValues(boolean[] booleanValues) {
        this.booleanValues = booleanValues;
      }

      public String[] getStringValues() {
        return stringValues;
      }

      public void setStringValues(String[] stringValues) {
        this.stringValues = stringValues;
      }

      public LocalDate[] getDateValues() {
        return dateValues;
      }

      public void setDateValues(LocalDate[] dateValues) {
        this.dateValues = dateValues;
      }

      public LocalTime[] getTimeValues() {
        return timeValues;
      }

      public void setTimeValues(LocalTime[] timeValues) {
        this.timeValues = timeValues;
      }

      public LocalDateTime[] getTimestampValues() {
        return timestampValues;
      }

      public void setTimestampValues(LocalDateTime[] timestampValues) {
        this.timestampValues = timestampValues;
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.IN_VALUE_LIST;
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        String values;
        switch (arrayElementType) {
          case INTEGER:
            values = Arrays.stream(integerValues).mapToObj(Long::toString).collect(joining(", "));
            break;
          case DECIMAL:
            values = Arrays.stream(decimalValues).mapToObj(v -> printLiteral(v)).collect(joining(", "));
            break;
          case BOOLEAN:
            values = IntStream.range(0, booleanValues.length)
                .mapToObj(i -> Boolean.valueOf(booleanValues[i]).toString()).collect(joining(", "));
            break;
          case STRING:
            values = Arrays.stream(stringValues).map(v -> printLiteral(v)).collect(joining(", "));
            break;
          case DATE:
            values = Arrays.stream(dateValues).map(v -> printLiteral(v)).collect(joining(", "));
            break;
          case TIME:
            values = Arrays.stream(timeValues).map(v -> printLiteral(v)).collect(joining(", "));
            break;
          case TIMESTAMP:
            values = Arrays.stream(timestampValues).map(v -> printLiteral(v)).collect(joining(", "));
            break;
          default:
            throw new IllegalArgumentException(arrayElementType.toString());
        }

        return "(" + values + ")";
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
        InValueList other = (InValueList) obj;
        if (arrayElementType != other.arrayElementType)
          return false;
        if (!Arrays.equals(booleanValues, other.booleanValues))
          return false;
        if (!Arrays.equals(dateValues, other.dateValues))
          return false;
        if (!Arrays.equals(decimalValues, other.decimalValues))
          return false;
        if (!Arrays.equals(integerValues, other.integerValues))
          return false;
        if (!Arrays.equals(stringValues, other.stringValues))
          return false;
        if (!Arrays.equals(timeValues, other.timeValues))
          return false;
        if (!Arrays.equals(timestampValues, other.timestampValues))
          return false;
        return true;
      }
    }
  }

  public static class IsNull implements QueryExpression {

    QueryExpression exp;

    public IsNull(QueryExpression exp) {
      this.exp = exp;
    }

    public QueryExpression getExp() {
      return exp;
    }

    public void setExp(QueryExpression exp) {
      this.exp = exp;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.IS_NULL;
    }

    @Override
    public String toString() {
      return exp + " IS NULL";
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
      IsNull other = (IsNull) obj;
      if (exp == null) {
        if (other.exp != null)
          return false;
      } else if (!exp.equals(other.exp))
        return false;
      return true;
    }
  }

  public static class IfElse implements QueryExpression {

    QueryExpression exp1;

    QueryExpression exp2;

    QueryExpression exp3;

    public IfElse(QueryExpression exp1, QueryExpression exp2, QueryExpression exp3) {
      this.exp1 = exp1;
      this.exp2 = exp2;
      this.exp3 = exp3;
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

    public QueryExpression getExp3() {
      return exp3;
    }

    public void setExp3(QueryExpression exp3) {
      this.exp3 = exp3;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.IF_ELSE;
    }

    @Override
    public String toString() {
      String elseClause = "";
      if (exp3 != null) {
        elseClause += " ELSE " + exp3;
      }
      return "CASE WHEN " + exp1 + " THEN " + exp2 + elseClause + " END";
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
      IfElse other = (IfElse) obj;
      if (exp1 == null) {
        if (other.exp1 != null)
          return false;
      } else if (!exp1.equals(other.exp1))
        return false;
      if (exp2 == null) {
        if (other.exp2 != null)
          return false;
      } else if (!exp2.equals(other.exp2))
        return false;
      if (exp3 == null) {
        if (other.exp3 != null)
          return false;
      } else if (!exp3.equals(other.exp3))
        return false;
      return true;
    }
  }

  public static class SimpleCase implements QueryExpression {

    QueryExpression caseOperand;

    List<WhenThenExpression> whenThenExps;

    QueryExpression elseExp;

    IfElse ifElseRepresentation;

    public SimpleCase(QueryExpression caseOperand, List<WhenThenExpression> whenThenExps, QueryExpression elseExp,
        IfElse ifElseRepresentation) {
      this.caseOperand = caseOperand;
      this.whenThenExps = whenThenExps;
      this.elseExp = elseExp;
      this.ifElseRepresentation = ifElseRepresentation;
    }

    public QueryExpression getCaseOperand() {
      return caseOperand;
    }

    public void setCaseOperand(QueryExpression caseOperand) {
      this.caseOperand = caseOperand;
    }

    public List<WhenThenExpression> getWhenThenExps() {
      return whenThenExps;
    }

    public void setWhenThenExps(List<WhenThenExpression> whenThenExps) {
      this.whenThenExps = whenThenExps;
    }

    public QueryExpression getElseExp() {
      return elseExp;
    }

    public void setElseExp(QueryExpression elseExp) {
      this.elseExp = elseExp;
    }

    public IfElse getIfElseRepresentation() {
      return ifElseRepresentation;
    }

    public void setIfElseRepresentation(IfElse ifElseRepresentation) {
      this.ifElseRepresentation = ifElseRepresentation;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.SIMPLE_CASE;
    }

    @Override
    public String toString() {
      String result = "CASE " + caseOperand + " ";
      result += whenThenExps.stream() //
          .map(x -> x.toString()) //
          .collect(Collectors.joining(" "));
      if (elseExp != null) {
        result += " ELSE " + elseExp;
      }
      result += " END";
      return result;
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
      SimpleCase other = (SimpleCase) obj;
      if (caseOperand == null) {
        if (other.caseOperand != null)
          return false;
      } else if (!caseOperand.equals(other.caseOperand))
        return false;
      if (elseExp == null) {
        if (other.elseExp != null)
          return false;
      } else if (!elseExp.equals(other.elseExp))
        return false;
      if (ifElseRepresentation == null) {
        if (other.ifElseRepresentation != null)
          return false;
      } else if (!ifElseRepresentation.equals(other.ifElseRepresentation))
        return false;
      if (whenThenExps == null) {
        if (other.whenThenExps != null)
          return false;
      } else if (!whenThenExps.equals(other.whenThenExps))
        return false;
      return true;
    }
  }

  public class WhenThenExpression {

    QueryExpression when;

    QueryExpression then;

    public WhenThenExpression(QueryExpression when, QueryExpression then) {
      this.when = when;
      this.then = then;
    }

    public QueryExpression getWhen() {
      return when;
    }

    public void setWhen(QueryExpression when) {
      this.when = when;
    }

    public QueryExpression getThen() {
      return then;
    }

    public void setThen(QueryExpression then) {
      this.then = then;
    }

    @Override
    public String toString() {
      return "WHEN " + when + " THEN " + then;
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
      WhenThenExpression other = (WhenThenExpression) obj;
      if (then == null) {
        if (other.then != null)
          return false;
      } else if (!then.equals(other.then))
        return false;
      if (when == null) {
        if (other.when != null)
          return false;
      } else if (!when.equals(other.when))
        return false;
      return true;
    }
  }

  class SubstringExpression implements QueryExpression {

    private QueryExpression exp;

    private QueryExpression startPosition;

    private QueryExpression stringLength;

    public SubstringExpression(QueryExpression exp, QueryExpression startPosition, QueryExpression stringLength) {
      this.exp = exp;
      this.startPosition = startPosition;
      this.stringLength = stringLength;
    }

    public QueryExpression getExp() {
      return exp;
    }

    public void setExp(QueryExpression exp) {
      this.exp = exp;
    }

    public QueryExpression getStartPosition() {
      return startPosition;
    }

    public void setStartPosition(QueryExpression startPosition) {
      this.startPosition = startPosition;
    }

    public QueryExpression getStringLength() {
      return stringLength;
    }

    public void setStringLength(QueryExpression stringLength) {
      this.stringLength = stringLength;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.SUBSTRING;
    }

    @Override
    public String toString() {
      String result = "SUBSTRING(" + exp + " FROM " + startPosition;
      if (stringLength != null) {
        result += " FOR " + stringLength;
      }
      result += ")";
      return result; 
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
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      SubstringExpression other = (SubstringExpression) obj;
      if (exp == null) {
        if (other.exp != null) {
          return false;
        }
      } else if (!exp.equals(other.exp)) {
        return false;
      }
      if (startPosition == null) {
        if (other.startPosition != null) {
          return false;
        }
      } else if (!startPosition.equals(other.startPosition)) {
        return false;
      }
      if (stringLength == null) {
        if (other.stringLength != null) {
          return false;
        }
      } else if (!stringLength.equals(other.stringLength)) {
        return false;
      }
      return true;
    }
  }

  interface Aggregation extends QueryExpression {

    abstract class AbstractAggregation extends UnaryExpression implements Aggregation {

      private boolean distinct;

      public AbstractAggregation(boolean distinct, QueryExpression exp) {
        super(exp);
        this.distinct = distinct;
      }

      /**
       * @deprecated Replaced by {@link #isDistinct()}
       */
      @Deprecated
      public boolean hasDistinct() {
        return distinct;
      }

      public boolean isDistinct() {
        return distinct;
      }

      public void setDistinct(boolean distinct) {
        this.distinct = distinct;
      }

      @Override
      public String toString() {
        String result;
        String separator = "";
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
          case AGGR_LISTAGG:
            result = "LISTAGG";
            separator = ((AggrListagg)this).getSeparator();
            if (separator.length() > 0) {
              separator = ", " + printLiteral(separator);
            }
            break;
          default:
            throw new IllegalArgumentException("Unexpected expression type: " + getExpType());
        }
        result += "(" + (distinct ? "DISTINCT " : "") + getExp() + separator + ")";
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

    class AggrListagg extends AbstractAggregation {

      private String separator;

      public AggrListagg(boolean distinct, QueryExpression exp, String separator) {
        super(distinct, exp);
        this.separator = separator;
      }

      public String getSeparator() {
        return separator;
      }

      public void setSeparator(String separator) {
        this.separator = separator;
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AGGR_LISTAGG;
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

    private SelectQuery query;

    public Subquery(SelectQuery query) {
      this.query = query;
    }

    public SelectQuery getQuery() {
      return query;
    }

    public void setQuery(SelectQuery query) {
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

    public ScalarSubquery(SelectQuery query) {
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
