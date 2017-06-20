/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public interface SpatialFunction extends QueryExpression {

  class StX extends UnaryExpression implements SpatialFunction {

    public StX(QueryExpression exp) {
      super(exp);
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.ST_X;
    }

    @Override
    public String toString() {
      return "ST_X(" + getExp() + ")";
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
    }
  }

  class StY extends UnaryExpression implements SpatialFunction {

    public StY(QueryExpression exp) {
      super(exp);
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.ST_Y;
    }

    @Override
    public String toString() {
      return "ST_Y(" + getExp() + ")";
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
    }
  }

  class StPointFromText extends UnaryExpression implements SpatialFunction {

    public StPointFromText(QueryExpression wktString) {
      super(wktString);
    }

    @Override
    public ExpressionType getExpType() {
      return ExpressionType.ST_POINT_FROM_TEXT;
    }

    @Override
    public String toString() {
      return "ST_PointFromText(" + getExp() + ")";
    }

    @Override
    public void accept(QueryExpressionVisitor v) {
      v.visit(this);
    }
  }

}
