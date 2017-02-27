/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public interface QueryExpression {

  enum ExpressionType {
    INTEGER,
    DECIMAL,
    STRING,
    BOOLEAN,
    NULL, // constants
    SUB,
    ADD,
    MUL,
    DIV,
    MOD,
    UMIN, // arithmetic expressions
    AND,
    OR,
    NOT, // logical expressions
    EQUAL,
    NOT_EQUAL,
    GREATER,
    GREATER_EQUAL,
    LESS,
    LESS_EQUAL, // relational expressions
    AGGR_COUNT,
    AGGR_MIN,
    AGGR_MAX,
    AGGR_SUM,
    AGGR_AVG, // aggregates
    VARREF,
    BIND_VARIABLE,
    STAR, // other

    // functions:
    REGEX, // String
    ID,
    PROP_ACCESS,
    HAS_PROP,
    HAS_LABEL, // vertex/edges note: HasProp/HasLabel will be removed in future
    // version (replaced by 'x.prop != NULL')
    VERTEX_LABELS,
    INDEGREE,
    OUTDEGREE, // vertex
    EDGE_LABEL, // edge
    CAST
  }

  ExpressionType getExpType();

  void accept(QueryExpressionVisitor v);

  abstract class UnaryExpression implements QueryExpression {

    private final QueryExpression exp;

    public UnaryExpression(QueryExpression exp) {
      this.exp = exp;
    }

    public QueryExpression getExp() {
      return exp;
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
      return exp.hashCode();
    }
  }

  abstract class BinaryExpression implements QueryExpression {

    private final QueryExpression exp1;
    private final QueryExpression exp2;

    public BinaryExpression(QueryExpression exp1, QueryExpression exp2) {
      this.exp1 = exp1;
      this.exp2 = exp2;
    }

    public QueryExpression getExp1() {
      return exp1;
    }

    public QueryExpression getExp2() {
      return exp2;
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
      int result = exp1.hashCode();
      result = 31 * result + exp2.hashCode();
      return result;
    }
  }

  // not yet used, but there will be built-in functions in the future that will need it
  abstract class TernaryExpression implements QueryExpression {

    private final QueryExpression exp1;
    private final QueryExpression exp2;
    private final QueryExpression exp3;

    public TernaryExpression(QueryExpression exp1, QueryExpression exp2, QueryExpression exp3) {
      this.exp1 = exp1;
      this.exp2 = exp2;
      this.exp3 = exp3;
    }

    public QueryExpression getExp1() {
      return exp1;
    }

    public QueryExpression getExp2() {
      return exp2;
    }

    public QueryExpression getExp3() {
      return exp3;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TernaryExpression that = (TernaryExpression) o;

      if (!exp1.equals(that.exp1)) {
        return false;
      }
      if (!exp2.equals(that.exp2)) {
        return false;
      }
      return exp3.equals(that.exp3);
    }

    @Override
    public int hashCode() {
      int result = exp1.hashCode();
      result = 31 * result + exp2.hashCode();
      result = 31 * result + exp3.hashCode();
      return result;
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
        return "!(" + getExp() + ")";
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

    protected final T value;

    public Constant(T value) {
      this.value = value;
    }

    public T getValue() {
      return value;
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
      return value.hashCode();
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
        return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'";
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
  }

  class ConstNull implements QueryExpression {

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.NULL;
    }

    @Override
    public String toString() {
      return "NULL";
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

  class VarRef implements QueryExpression {
    private final QueryVariable variable;

    public VarRef(QueryVariable variable) {
      this.variable = variable;
    }

    public QueryVariable getVariable() {
      return variable;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.VARREF;
    }

    @Override
    public String toString() {
      return PgqlUtils.printPgqlString(variable);
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
      return variable.hashCode();
    }
  }

  class BindVariable implements QueryExpression {
    private final int parameterIndex;

    public BindVariable(int parameterIndex) {
      this.parameterIndex = parameterIndex;
    }

    public int getParameterIndex() {
      return parameterIndex;
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
      final int prime = 31;
      int result = 1;
      result = prime * result + parameterIndex;
      return result;
    }
  }

  class PropertyAccess implements QueryExpression {
    private final QueryVariable variable;
    private final String propertyName;

    public PropertyAccess(QueryVariable variable, String propertyName) {
      this.variable = variable;
      this.propertyName = propertyName;
    }

    public QueryVariable getVariable() {
      return variable;
    }

    public String getPropertyName() {
      return propertyName;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.PROP_ACCESS;
    }

    @Override
    public String toString() {
      return PgqlUtils.printPgqlString(variable) + "." + propertyName;
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
      int result = variable.hashCode();
      result = 31 * result + propertyName.hashCode();
      return result;
    }
  }

  interface Function extends QueryExpression {

    class Regex extends BinaryExpression implements Function {
      public Regex(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.REGEX;
      }

      @Override
      public String toString() {
        return "(" + getExp1() + " =~ " + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class Id extends UnaryExpression implements Function {

      /**
       * Identifier of a node/edge
       *
       * @param exp
       *          an expression of type node or edge
       */
      public Id(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.ID;
      }

      @Override
      public String toString() {
        return getExp() + ".id()";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class EdgeLabel extends UnaryExpression implements Function {

      public EdgeLabel(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.EDGE_LABEL;
      }

      @Override
      public String toString() {
        return getExp() + ".label()";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class VertexLabels extends UnaryExpression implements Function {

      public VertexLabels(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.VERTEX_LABELS;
      }

      @Override
      public String toString() {
        return getExp() + ".labels()";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class HasLabel extends BinaryExpression implements Function {

      public HasLabel(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.HAS_LABEL;
      }

      @Override
      public String toString() {
        return getExp1() + ".hasLabel(" + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class HasProp extends BinaryExpression implements Function {

      /**
       * Whether a node/edge has a property.
       *
       * @param exp1
       *          an expression of type node or edge
       * @param exp2
       *          an expression of type String
       */
      public HasProp(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.HAS_PROP;
      }

      @Override
      public String toString() {
        return getExp1() + ".has(" + getExp2() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class InDegree extends UnaryExpression implements Function {
      public InDegree(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.INDEGREE;
      }

      @Override
      public String toString() {
        return getExp() + ".indegree()";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class OutDegree extends UnaryExpression implements Function {
      public OutDegree(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.OUTDEGREE;
      }

      @Override
      public String toString() {
        return getExp() + ".outdegree()";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class Cast implements QueryExpression {

      private final QueryExpression exp;
      private final String targetTypeName;

      public Cast(QueryExpression exp, String targetTypeName) {
        this.exp = exp;
        this.targetTypeName = targetTypeName;
      }

      public QueryExpression getExp() {
        return exp;
      }

      public String getTargetTypeName() {
        return targetTypeName;
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.CAST;
      }

      @Override
      public String toString() {
        return "CAST(" + exp + " AS " + targetTypeName + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }
  }

  interface Aggregation extends QueryExpression {

    class AggrCount extends UnaryExpression implements Aggregation {

      public AggrCount(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AGGR_COUNT;
      }

      @Override
      public String toString() {
        return "COUNT(" + getExp() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class AggrMin extends UnaryExpression implements Aggregation {

      public AggrMin(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AGGR_MIN;
      }

      @Override
      public String toString() {
        return "MIN(" + getExp() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class AggrMax extends UnaryExpression implements Aggregation {

      public AggrMax(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AGGR_MAX;
      }

      @Override
      public String toString() {
        return "MAX(" + getExp() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class AggrSum extends UnaryExpression implements Aggregation {

      public AggrSum(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AGGR_SUM;
      }

      @Override
      public String toString() {
        return "SUM(" + getExp() + ")";
      }

      @Override
      public void accept(QueryExpressionVisitor v) {
        v.visit(this);
      }
    }

    class AggrAvg extends UnaryExpression implements Aggregation {

      public AggrAvg(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.AGGR_AVG;
      }

      @Override
      public String toString() {
        return "AVG(" + getExp() + ")";
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
}
