/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public interface QueryExpression {

  public enum ExpressionType {
    ConstInteger, ConstDecimal, ConstString, ConstBoolean, ConstNull, // constants
    Sub, Add, Mul, Div, Mod, UMin, // arithmetic expressions
    And, Or, Not, // logical expressions
    Equal, NotEqual, Greater, GreaterEqual, Less, LessEqual, // relational expressions
    Aggr_count, Aggr_min, Aggr_max, Aggr_sum, Aggr_avg, // aggregates
    VarRef, Star, // other

    // functions:
    Regex, // String
    Id, PropAccess, HasProp, HasLabel, // vertex/edges     note: HasProp will be removed in future version (replaced by 'x.prop != NULL')
    VertexLabels, InDegree, OutDegree, // vertex
    EdgeLabel // edge
  }

  public ExpressionType getExpType();

  public void accept(ExpressionVisitor v);

  public static abstract class UnaryExpression implements QueryExpression {

    public final QueryExpression exp;

    public UnaryExpression(QueryExpression exp) {
      this.exp = exp;
    }

    @Override
    public String toString() {
      return getExpType() + "(" + exp + ")";
    }
    
    @Override
    public void accept(ExpressionVisitor v) {
      v.visit(this);
      v.visit(exp);
    }
  }

  public static abstract class BinaryExpression implements QueryExpression {

    public final QueryExpression exp1;
    public final QueryExpression exp2;

    public BinaryExpression(QueryExpression exp1, QueryExpression exp2) {
      this.exp1 = exp1;
      this.exp2 = exp2;
    }

    @Override
    public String toString() {
      return getExpType() + "(" + exp1 + ", " + exp2 + ")";
    }
    
    @Override
    public void accept(ExpressionVisitor v) {
      v.visit(this);
      v.visit(exp1);
      v.visit(exp2);
    }
  }

  // not yet used, but there will be built-in functions in the future that will need it
  public static abstract class TernaryExpression implements QueryExpression {

    public final QueryExpression exp1;
    public final QueryExpression exp2;
    public final QueryExpression exp3;

    public TernaryExpression(QueryExpression exp1, QueryExpression exp2, QueryExpression exp3) {
      this.exp1 = exp1;
      this.exp2 = exp2;
      this.exp3 = exp3;
    }

    @Override
    public String toString() {
      return getExpType() + "(" + exp1 + ", " + exp2 + ", " + exp3 + ")";
    }
    
    @Override
    public void accept(ExpressionVisitor v) {
      v.visit(this);
      v.visit(exp1);
      v.visit(exp2);
      v.visit(exp3);
    }
  }

  public interface ArithmeticExpression extends QueryExpression {

    class Sub extends BinaryExpression {
      public Sub(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Sub;
      }
    }

    class Add extends BinaryExpression {
      public Add(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Add;
      }
    }

    class Mul extends BinaryExpression {
      public Mul(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Mul;
      }
    }

    class Div extends BinaryExpression {
      public Div(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Div;
      }
    }

    class Mod extends BinaryExpression {
      public Mod(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Mod;
      }
    }

    class UMin extends UnaryExpression {
      public UMin(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.UMin;
      }
    }
  }

  public interface LogicalExpression extends QueryExpression {

    class And extends BinaryExpression {
      public And(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.And;
      }
    }

    class Or extends BinaryExpression {
      public Or(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Or;
      }
    }

    class Not extends UnaryExpression {
      public Not(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Not;
      }
    }
  }

  public interface RelationalExpression extends QueryExpression {

    class Equal extends BinaryExpression {
      public Equal(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Equal;
      }
    }

    class NotEqual extends BinaryExpression {
      public NotEqual(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.NotEqual;
      }
    }

    class Greater extends BinaryExpression {
      public Greater(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Greater;
      }
    }

    class GreaterEqual extends BinaryExpression {
      public GreaterEqual(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.GreaterEqual;
      }
    }

    class Less extends BinaryExpression {
      public Less(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Less;
      }
    }

    class LessEqual extends BinaryExpression {
      public LessEqual(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.LessEqual;
      }
    }
  }

  public static abstract class Constant<T> implements QueryExpression {

    public final T val;

    public Constant(T val) {
      this.val = val;
    }
    
    @Override
    public void accept(ExpressionVisitor v) {
      v.visit(this);
    }

    public static class ConstInteger extends Constant<Long> {
      public ConstInteger(long val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.ConstInteger;
      }
    }

    public static class ConstDecimal extends Constant<Double> {
      public ConstDecimal(double val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.ConstDecimal;
      }
    }

    public static class ConstString extends Constant<String> {
      public ConstString(String val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.ConstString;
      }

      @Override
      public String toString() {
        return "'" + val + "'";
      }
    }

    public static class ConstBoolean extends Constant<Boolean> {

      public ConstBoolean(boolean val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.ConstBoolean;
      }
    }

    @Override
    public String toString() {
      return val.toString();
    }
  }

  public static class ConstNull implements QueryExpression {

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.ConstNull;
    }
    
    @Override
    public void accept(ExpressionVisitor v) {
      v.visit(this);
    }
  }

  public static class VarRef implements QueryExpression {
    public final QueryVar var;

    public VarRef(QueryVar var) {
      this.var = var;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.VarRef;
    }

    @Override
    public String toString() {
      return var.name;
    }
    
    @Override
    public void accept(ExpressionVisitor v) {
      v.visit(this);
    }
  }

  public static class PropAccess implements QueryExpression {
    public final QueryVar var;
    public final String propname;

    public PropAccess(QueryVar var, String propname) {
      this.var = var;
      this.propname = propname;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.PropAccess;
    }

    @Override
    public String toString() {
      return var.name + "." + propname;
    }
    
    @Override
    public void accept(ExpressionVisitor v) {
      v.visit(this);
    }
  }

  public interface Function extends QueryExpression {

    class Regex extends BinaryExpression {
      public Regex(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Regex;
      }
    }

    class Id extends UnaryExpression {

      /**
       * Identifier of a node/edge
       *
       * @param exp an expression of type node or edge
       */
      public Id(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Id;
      }
    }

    class EdgeLabel extends UnaryExpression {

      public EdgeLabel(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.EdgeLabel;
      }
    }

    class VertexLabels extends UnaryExpression {

      public VertexLabels(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.VertexLabels;
      }
    }

    class HasLabel extends BinaryExpression {

      public HasLabel(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.HasLabel;
      }
    }

    class HasProp extends BinaryExpression {

      /**
       * Whether a node/edge has a property.
       *
       * @param exp1 an expression of type node or edge
       * @param exp2 an expression of type String
       */
      public HasProp(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.HasProp;
      }
    }

    class InDegree extends UnaryExpression {
      public InDegree(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.InDegree;
      }
    }

    class OutDegree extends UnaryExpression {
      public OutDegree(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.OutDegree;
      }
    }
  }

  public interface Aggregation extends QueryExpression {

    class AggrCount extends UnaryExpression implements Aggregation {

      public AggrCount(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Aggr_count;
      }
    }

    class AggrMin extends UnaryExpression implements Aggregation {

      public AggrMin(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Aggr_min;
      }
    }

    class AggrMax extends UnaryExpression implements Aggregation {

      public AggrMax(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Aggr_max;
      }
    }

    class AggrSum extends UnaryExpression implements Aggregation {

      public AggrSum(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Aggr_sum;
      }
    }

    class AggrAvg extends UnaryExpression implements Aggregation {

      public AggrAvg(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Aggr_avg;
      }
    }

    class Star implements QueryExpression {

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Star;
      }

      @Override
      public String toString() {
        return "*";
      }

      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
      }
    }
  }
}