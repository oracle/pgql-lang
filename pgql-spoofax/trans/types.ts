module types

imports

  signatures/-

type rules

  VarRef(v, _, _) : ty
  where definition of v : ty
    and not ( ty == PathTy() ) else error $[Path variables not supported in PGQL 1.1] on v

  CrossRef(VarRef(outer-var, _, _), inner-var, MatchClause(VertexTy())) :-
  where definition of outer-var : ty
    and ty == VertexTy() else error $[Variable passed from outer query is not a vertex] on inner-var

  CrossRef(VarRef(outer-var, _, _), inner-var, MatchClause(EdgeTy())) :-
  where definition of outer-var : ty
    and ty == EdgeTy() else error $[Variable passed from outer query is not an edge] on inner-var
    and not (ty == EdgeTy()) else error $[Duplicate edge variable: cannot reuse edge variable from outer query] on inner-var

  PropRef(_, _) + BindVariable(_) : UnknownTy()

  Not(exp) : BooleanTy()
//  where exp : ty
//    and (ty == BooleanTy() or ty == UnknownTy()) else error $[Boolean expected here] on exp

  And(exp1, exp2) + Or(exp1, exp2) : BooleanTy()
//  where exp1 : ty1
//    and exp2 : ty2
//    and ((ty1 == BooleanTy() or ty1 == UnknownTy()) else error $[Boolean expected here] on exp1)
//    and ((ty2 == BooleanTy() or ty2 == UnknownTy()) else error $[Boolean expected here] on exp2)

  UMin(exp) : NumericTy()
//  where exp : ty
//    and (ty == NumericTy() or ty == UnknownTy()) else error $[Number expected here] on exp

  Mul(exp1, exp2) + Add(exp1, exp2) + Div(exp1, exp2) + Mod(exp1, exp2) + Sub(exp1, exp2) : NumericTy()
//  where exp1 : ty1
//    and exp2 : ty2
//    and (ty1 == NumericTy() or ty1 == UnknownTy()) else error $[Number expected here] on exp1
//    and (ty2 == NumericTy() or ty2 == UnknownTy()) else error $[Number expected here] on exp2

  t@Eq(exp1, exp2) + t@Neq(exp1, exp2) : BooleanTy()
//  where TODO

  t@Gt(exp1, exp2) + t@Lt(exp1, exp2) + t@Gte(exp1, exp2) + t@Lte(exp1, exp2) : BooleanTy()
//  where TODO

  MIN(_, exp)  + MAX(_, exp)  + SUM(_, exp)  + AVG(_, exp) : ty
  where exp : ty

  COUNT(_, exp) : NumericTy()

  Cast(_, _) + FunctionCall(_, _, _) + Star(): UnknownTy()

  Exists(_) : BooleanTy()

  Subquery(_) : UnknownTy()

  ExpAsVar(exp, var, _, _, _) : ty
  where exp : ty

  ScalarSubquery(Subquery((_, NormalizedQuery(_, SelectClause(_, ExpAsVars([expAsVar|_])), _, _, _, _, _, _, _, _)))) : ty
  where expAsVar : ty

  True() + False()        : BooleanTy()
  Date(_)                 : DateTy()
  Integer(_) + Decimal(_) : NumericTy()
  String(_)               : StringTy()
  Time(_)                 : TimeTy()
  Timestamp(_)            : TimestampTy()

  OrderByElem(exp, _, "v1.1") :-
  where exp : ty
    and not ( ty == VertexTy() ) else error $[Cannot order by vertex] on exp
    and not ( ty == EdgeTy() ) else error $[Cannot order by edge] on exp
