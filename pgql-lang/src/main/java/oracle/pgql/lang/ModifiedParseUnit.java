/*
 * Copyright (C) 2013 - 2023 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgql.lang;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.unit.IUnitContrib;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.UnitWrapper;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class ModifiedParseUnit extends UnitWrapper implements ISpoofaxParseUnit {

  private final ISpoofaxParseUnit originalParseUnit;

  private final IStrategoTerm modifiedAst;

  public ModifiedParseUnit(ISpoofaxParseUnit originalParseUnit, IStrategoTerm modifiedAst) {
    super(null);
    this.originalParseUnit = originalParseUnit;
    this.modifiedAst = modifiedAst;
  }

  @Override
  public long duration() {
    return originalParseUnit.duration();
  }

  @Override
  public boolean isAmbiguous() {
    return originalParseUnit.isAmbiguous();
  }

  @Override
  public Iterable<IMessage> messages() {
    return originalParseUnit.messages();
  }

  @Override
  public boolean success() {
    return originalParseUnit.success();
  }

  @Override
  public boolean valid() {
    return originalParseUnit.valid();
  }

  @Override
  public IStrategoTerm ast() {
    return modifiedAst;
  }

  @Override
  public ISpoofaxInputUnit input() {
    return originalParseUnit.input();
  }

  @Override
  public FileObject source() {
    return originalParseUnit.source();
  }

  @Override
  public boolean detached() {
    return originalParseUnit.detached();
  }

  @Override
  public IUnitContrib unitContrib(String id) {
    return originalParseUnit.unitContrib(id);
  }

  @Override
  public Iterable<IUnitContrib> unitContribs() {
    return originalParseUnit.unitContribs();
  }
}
