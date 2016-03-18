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
  }

  public interface ArithmeticExpression extends QueryExpression {

    class Sub extends BinaryExpression implements ArithmeticExpression {
      public Sub(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Sub;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }
    }

    class Add extends BinaryExpression implements ArithmeticExpression {
      public Add(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Add;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }
    }

    class Mul extends BinaryExpression implements ArithmeticExpression {
      public Mul(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Mul;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }
    }

    class Div extends BinaryExpression implements ArithmeticExpression {
      public Div(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Div;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }
    }

    class Mod extends BinaryExpression implements ArithmeticExpression {
      public Mod(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Mod;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }
    }

    class UMin extends UnaryExpression implements ArithmeticExpression {
      public UMin(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.UMin;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp.accept(v);
      }
    }
  }

  public interface LogicalExpression extends QueryExpression {

    class And extends BinaryExpression implements LogicalExpression {
      public And(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.And;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }

    }

    class Or extends BinaryExpression implements LogicalExpression {
      public Or(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Or;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }

    }

    class Not extends UnaryExpression implements LogicalExpression {
      public Not(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Not;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp.accept(v);
      }
    }
  }

  public interface RelationalExpression extends QueryExpression {

    class Equal extends BinaryExpression implements RelationalExpression {
      public Equal(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Equal;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }

    }

    class NotEqual extends BinaryExpression implements RelationalExpression {
      public NotEqual(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.NotEqual;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }

    }

    class Greater extends BinaryExpression implements RelationalExpression {
      public Greater(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Greater;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }

    }

    class GreaterEqual extends BinaryExpression implements RelationalExpression {
      public GreaterEqual(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.GreaterEqual;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }

    }

    class Less extends BinaryExpression implements RelationalExpression {
      public Less(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Less;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }

    }

    class LessEqual extends BinaryExpression implements RelationalExpression {
      public LessEqual(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.LessEqual;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }

    }
  }

  public static abstract class Constant<T> implements QueryExpression {

    public final T val;

    public Constant(T val) {
      this.val = val;
    }

    public static class ConstInteger extends Constant<Long> {
      public ConstInteger(long val) {
        super(val);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.ConstInteger;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
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
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
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
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
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
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
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

    class Regex extends BinaryExpression implements Function {
      public Regex(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.Regex;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }
    }

    class Id extends UnaryExpression implements Function {

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
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp.accept(v);
      }
    }

    class EdgeLabel extends UnaryExpression implements Function {

      public EdgeLabel(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.EdgeLabel;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp.accept(v);
      }
    }

    class VertexLabels extends UnaryExpression implements Function {

      public VertexLabels(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.VertexLabels;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp.accept(v);
      }
    }

    class HasLabel extends BinaryExpression implements Function {

      public HasLabel(QueryExpression exp1, QueryExpression exp2) {
        super(exp1, exp2);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.HasLabel;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }
    }

    class HasProp extends BinaryExpression implements Function {

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
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp1.accept(v);
        exp2.accept(v);
      }
    }

    class InDegree extends UnaryExpression implements Function {
      public InDegree(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.InDegree;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp.accept(v);
      }
    }

    class OutDegree extends UnaryExpression implements Function {
      public OutDegree(QueryExpression exp) {
        super(exp);
      }

      @Override
      public ExpressionType getExpType() {
        return ExpressionType.OutDegree;
      }
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp.accept(v);
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
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp.accept(v);
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
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp.accept(v);
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
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp.accept(v);
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
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp.accept(v);
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
      
      @Override
      public void accept(ExpressionVisitor v) {
        v.visit(this);
        exp.accept(v);
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