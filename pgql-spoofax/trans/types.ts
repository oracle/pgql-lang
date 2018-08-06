module types

imports

  signatures/-

type rules

  VarRef(v, _) : ty
  where definition of v : ty
    and not ( ty == PathTy() ) else error $[Path variables not supported in PGQL 1.1] on v

  Vertex(v, _, Correlation(varRef)) :-
  where varRef : ty
    and ty == VertexTy() else error $[Duplicate variable (variable with same name is passed from an outer query)] on v

  Edge(_, e, _, _, _, Correlation(VarRef(outer-var, _))) :-
  where definition of outer-var : ty
    and not(ty == ty ) /* make it always throw an error */ else error $[Duplicate variable (variable with same name is passed from an outer query)] on e

  PropRef(_, _) + BindVariable(_) : UnknownTy()

  Not(exp) : BooleanTy()
  where exp : ty
    and not (ty == VertexTy() or ty == EdgeTy()) else error $[Boolean expected here] on exp

  And(exp1, exp2) + Or(exp1, exp2) : BooleanTy()
  where exp1 : ty1
    and exp2 : ty2
    and not (ty1 == VertexTy() or ty1 == EdgeTy()) else error $[Boolean expected here] on exp1
    and not (ty2 == VertexTy() or ty2 == EdgeTy()) else error $[Boolean expected here] on exp2

  UMin(exp) : NumericTy()
  where exp : ty
    and not (ty == VertexTy() or ty == EdgeTy()) else error $[Numeric expected here] on exp

  Mul(exp1, exp2) + Add(exp1, exp2) + Div(exp1, exp2) + Mod(exp1, exp2) + Sub(exp1, exp2) : NumericTy()
  where exp1 : ty1
    and exp2 : ty2
    and not (ty1 == VertexTy() or ty1 == EdgeTy()) else error $[Numeric expected here] on exp1
    and not (ty2 == VertexTy() or ty2 == EdgeTy()) else error $[Numeric expected here] on exp2

  t@Eq(exp1, exp2) + t@Neq(exp1, exp2) : BooleanTy()

  t@Gt(exp1, exp2) + t@Lt(exp1, exp2) + t@Gte(exp1, exp2) + t@Lte(exp1, exp2) : BooleanTy()
  where exp1 : ty1
    and exp2 : ty2
    and not (ty1 == VertexTy() or ty2 == VertexTy()) else error $[Comparison not allowed because no order is defined for vertices] on t
    and not (ty1 == EdgeTy() or ty2 == EdgeTy()) else error $[Comparison not allowed because no order is defined for edges] on t
    and not (ty1 == ArrayTy() or ty2 == ArrayTy()) else error $[Comparison not allowed because no order is defined for arrays] on t

  MIN(_, exp)  + MAX(_, exp)  + SUM(_, exp)  + AVG(_, exp): ty
  where exp : ty
    and not (ty == VertexTy() or ty == EdgeTy()) else error $[Aggregate does not allow vertex or edge input] on exp
    and not ty == ArrayTy() else error $[Aggregate does not allow array input] on exp

  COUNT(_, exp) : NumericTy()

  ARRAY-AGG(_, exp) : ArrayTy()
  where exp : ty
    and not (ty == VertexTy() or ty == EdgeTy()) else error $[Aggregate does not allow vertex or edge input] on exp
    and not ty == ArrayTy() else error $[Aggregate does not allow array input] on exp

  Cast(_, _) + FunctionCall(_, _, _) + Star(): UnknownTy()

  ExtractExp(_, _) : NumericTy()

  Exists(_) + InPredicate(_, _) : BooleanTy()

  Subquery(_) : UnknownTy()

  ExpAsVar(exp, var, _, _) : ty
  where exp : ty

  ScalarSubquery(Subquery(NormalizedQuery(_, SelectClause(_, ExpAsVars([expAsVar|_])), _, _, _, _, _, _, _, _))) : ty
  where expAsVar : ty
    and not ( ty == VertexTy() or ty == EdgeTy() ) else error $[Scalar subquery not allowed to return a vertex or an edge] on expAsVar

  True() + False()        : BooleanTy()
  Date(_)                 : DateTy()
  Integer(_) + Decimal(_) : NumericTy()
  String(_)               : StringTy()
  Time(_)                 : TimeTy()
  Timestamp(_)            : TimestampTy()

  OrderByElem(exp, _, "v1.1") :-
  where exp : ty
    and not ty == VertexTy() else error $[Cannot order by vertex] on exp
    and not ty == EdgeTy() else error $[Cannot order by edge] on exp
    and not ty == ArrayTy() else error $[Cannot order by array] on exp
