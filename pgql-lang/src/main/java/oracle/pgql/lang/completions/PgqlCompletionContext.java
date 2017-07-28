/**
 * Copyright (C) 2013 - 2017 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang.completions;

import java.util.List;

public interface PgqlCompletionContext {

  List<String> getVertexProperties();

  List<String> getEdgeProperties();

  List<String> getVertexLabels();

  List<String> getEdgeLabels();
}
