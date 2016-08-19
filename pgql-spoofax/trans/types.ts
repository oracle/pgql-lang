module types

imports

  signatures/-

type rules // expressions

  VarRef(v) + GroupRef(v) + SelectOrGroupRef(v) : ty
  where definition of v : ty
  
  PropRef(_, _) : UnknownTy()

  Not(_) + And(_, _) + Or(_, _) : BooleanTy()
  
  UMin(_) + Mul(_, _) + Add(_, _) + Div(_, _) + Mod(_, _) + Sub(_, _) : IntegerTy()
  
  Eq(_, _) + Gt(_, _) + Lt(_, _) + Gte(_, _) + Lte(_, _) + Neq(_, _) + Regex(_, _) : BooleanTy()

  COUNT(_, _) + MIN(_, _)  + MAX(_, _)  + SUM(_, _)  + AVG(_, _) : IntegerTy()

  True() + False() : BooleanTy()
  Integer(_)       : IntegerTy()
  Decimal(_)       : DecimalTy()
  String(_)        : StringTy()
  Null()           : UnknownTy()

type rules // built-in functions

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

  t@Id(exp) : IntegerTy()
  where exp : ty
    and (ty == NodeTy() or ty == EdgeTy()) else error $[Function only defined for vertices and edges] on t
