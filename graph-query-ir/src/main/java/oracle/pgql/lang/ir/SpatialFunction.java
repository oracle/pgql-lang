/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public interface SpatialFunction extends QueryExpression {

  class StX implements SpatialFunction {

    private final QueryExpression exp;

    public StX(QueryExpression exps) {
      this.exp = exps;
    }

    public QueryExpression getExp() {
      return exp;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.ST_X;
    }

    @Override
    public String toString() {
      return "ST_X(" + exp + ")";
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
    }
  }

  class StY implements SpatialFunction {

    private final QueryExpression exp;

    public StY(QueryExpression exps) {
      this.exp = exps;
    }

    public QueryExpression getExp() {
      return exp;
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.ST_Y;
    }

    @Override
    public String toString() {
      return "ST_Y(" + exp + ")";
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
    }
  }


}
