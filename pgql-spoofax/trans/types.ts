module types

imports

  signatures/-

type rules // expressions

  VarRef(v) + GroupRef(v) + SelectOrGroupRef(v) + VarOrSelectRef(v) : ty
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

  t@Id(exp) + t@InDegree(exp) + t@OutDegree(exp) : IntegerTy()

  t@Labels(exp) : StringSetTy()

  t@Label(exp) : StringTy()

  t@Has(exp, _) + t@HasLabel(exp, _) : BooleanTy()

  Cast(_, _): UnknownTy()
