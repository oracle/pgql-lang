/**
 * Copyright (C) 2013 - 2016 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.ir;

public abstract class QueryConnection extends QueryVar {

  public final QueryVertex src;
  public final QueryVertex dst;

  public QueryConnection(String name, QueryVertex src, QueryVertex dst) {
    super(name);
    this.src = src;
    this.dst = dst;
  }

  public static class QueryEdge extends QueryConnection {

    public QueryEdge(String name, QueryVertex src, QueryVertex dst) {
      super(name, src, dst);
    }

    @Override
    public String toString() {
      return src.name + "-[" + name + "]-> " + dst.name;
    }

  }

  public static abstract class QueryPath extends QueryConnection {

    public QueryPath(String name, QueryVertex src, QueryVertex dst) {
      super(name, src, dst);
    }

    public static class SimpleQueryPath extends QueryPath {
      public final long minLength;
      public final long maxLength;

      public SimpleQueryPath(String name, QueryVertex src, QueryVertex dst, long minLength, long maxLength) {
        super(name, src, dst);
        this.minLength = minLength;
        this.maxLength = maxLength;
      }

      @Override
      public String toString() {
        return src.name + "-[" + name + "]{" + minLength + ".." + maxLength + "}-> " + dst.name;
      }
    }

    public static class ShortestQueryPath extends QueryPath {

      public ShortestQueryPath(String name, QueryVertex src, QueryVertex dst, boolean matchAll) {
        super(name, src, dst);
      }

      @Override
      public String toString() {
        return src.name + "-[" + name + "]{SHORTEST}-> " + dst.name;
      }
    }
  }
}
