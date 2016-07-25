module types

imports

  signatures/-

type rules

  VarRef(v) + GroupRef(v) : ty
  where definition of v : ty

  t@InDegree(exp) + t@OutDegree(exp) + t@Labels(exp) : ty
  where exp : ty
    and ty == NodeTy() else error $[Function only defined for vertices] on t

  t@Label(exp) : ty
  where exp : ty
    and ty == EdgeTy() else error $[Function only defined for edges] on t

  t@Has(exp, _) + t@HasLabel(exp, _) : ty
  where exp : ty
    and (ty == NodeTy() or ty == EdgeTy()) else error $[Function only defined for vertices and edges] on t
