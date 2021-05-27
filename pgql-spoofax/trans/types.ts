module types

imports

  signatures/-

type rules

  VarRef(Identifier(v, _), _) : ty
  where definition of v : ty
    and not ( ty == PathTy() ) else error $[Path variables not supported] on v

//  ExtractExp(_, _) : NumericTy()
//
//  IfElse(exp1, exp2, exp3): ty2
//  where exp1 : ty1
//    and exp2 : ty2
//    and exp3 : ty3
//    and (ty1 == BooleanTy() or ty1 == UnknownTy()) else error $[Boolean expected here] on exp1
//    and not (ty2 == VertexTy() or ty2 == EdgeTy()) else error $[CASE does not allow vertex or edge output] on exp2
//    and not (ty3 == VertexTy() or ty3 == EdgeTy()) else error $[CASE does not allow vertex or edge output] on exp3
//
//  // type determination of SimpleCase if based on IfElse representation
//  SimpleCase(_, _, _, ifElseRepresentation) : ty
//  where ifElseRepresentation : ty
//
//  SetProperty(_, exp) : ty
//  where exp : ty
//    and not ( ty == VertexTy() or ty == EdgeTy() ) else error $[Cannot set the value of a property to a vertex or an edge] on exp
