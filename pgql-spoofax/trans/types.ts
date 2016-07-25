module types

imports

  signatures/-

type rules

  VarRef(v) + GroupRef(v) : ty
  where definition of v : ty

  t@InDegree(exp) + t@OutDegree(exp) : IntegerTy()
  where exp : ty
    and ty == NodeTy() else error $[Function only defined for vertices] on t

  t@Labels(exp) : StringSetTy()
  where exp : ty
    and ty == NodeTy() else error $[Function only defined for vertices] on t

  t@Label(exp) : StringTy()
  where exp : ty
    and ty == EdgeTy() else error $[Function only defined for edges] on t

  t@Has(exp, _) + t@HasLabel(exp, _) : BooleanTy()
  where exp : ty
    and (ty == NodeTy() or ty == EdgeTy()) else error $[Function only defined for vertices and edges] on t
