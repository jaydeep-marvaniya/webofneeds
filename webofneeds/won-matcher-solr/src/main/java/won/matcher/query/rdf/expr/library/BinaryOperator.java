/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package won.matcher.query.rdf.expr.library;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import org.sindice.siren.search.SirenBooleanClause;
import org.sindice.siren.search.SirenBooleanQuery;
import org.sindice.siren.search.SirenPrimitiveQuery;
import won.matcher.query.rdf.RdfToSirenQuery;
import won.matcher.query.rdf.expr.ExpressionToQueryMapper;

import java.util.Set;

/**
 * User: fkleedorfer
 * Date: 06.09.13
 */
public abstract class BinaryOperator implements ExpressionToQueryMapper
{


  @Override
  public final SirenBooleanQuery mapExpression(final Expr expression, String field, SirenBooleanClause.Occur occur, final RdfToSirenQuery rdfToSirenQuery)
  {
    ExprFunction2 func2 = (ExprFunction2) expression;
    Expr arg1 = func2.getArg1();
    Expr arg2 = func2.getArg2();
    Set<Var> vars = arg1.getVarsMentioned();
    if (vars.size() > 1) throw new IllegalArgumentException("Exactly one variable expected in expression but found more in arg1");
    if (vars.size() == 1){
      SirenPrimitiveQuery rangeQuery = mapExpressionWithValueOnRightSide(arg2, field, rdfToSirenQuery);
      return wrap(rangeQuery, occur);      
    }
    //2nd arg is the variable
    vars = arg2.getVarsMentioned();
    if (vars.size() > 1) throw new IllegalArgumentException("Exactly one variable expected in expression but found more in arg2");
    if (vars.size() == 1){
      SirenPrimitiveQuery rangeQuery = mapExpressionWithValueOnLeftSide(arg1, field, rdfToSirenQuery);
      return wrap(rangeQuery, occur);
    }
    throw new IllegalArgumentException("Exactly one variable expected in expression but none was provided.");
  }

  private SirenBooleanQuery wrap(final SirenPrimitiveQuery rangeQuery, final SirenBooleanClause.Occur occur)
  {
    if (rangeQuery == null) return null;
    SirenBooleanQuery query = new SirenBooleanQuery();
    query.add(rangeQuery,occur);
    return query;
  }

  protected abstract SirenPrimitiveQuery mapExpressionWithValueOnRightSide(Expr value, String field, final RdfToSirenQuery rdfToSirenQuery);

  protected abstract SirenPrimitiveQuery mapExpressionWithValueOnLeftSide(Expr value, String field, final RdfToSirenQuery rdfToSirenQuery);

}
