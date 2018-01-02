/*
 * Copyright (C) 2013 - 2018 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import com.google.inject.Singleton;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.editor.NullEditorRegistry;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.project.SingleFileProjectService;
import org.metaborg.spoofax.core.SpoofaxModule;

class PgqlConfig extends SpoofaxModule {

  // to silence warning about missing EditorRegistry
  @Override
  protected void bindEditor() {
    bind(IEditorRegistry.class) //
        .to(NullEditorRegistry.class) //
        .in(Singleton.class);
  }

  // to silence warning given by the default DummyProjectService.
  @Override
  protected void bindProject() {
    bind(IProjectService.class) //
        .to(SingleFileProjectService.class) //
        .in(Singleton.class);
  }

}