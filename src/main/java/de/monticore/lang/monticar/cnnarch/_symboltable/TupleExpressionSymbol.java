/**
 *
 *  ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
package de.monticore.lang.monticar.cnnarch._symboltable;

import de.monticore.lang.math.math._symboltable.expression.MathExpressionSymbol;

import java.util.LinkedList;
import java.util.List;

public class TupleExpressionSymbol extends MathExpressionSymbol {

    List<MathExpressionSymbol> expressions = new LinkedList<>();

    public TupleExpressionSymbol() {
    }

    public TupleExpressionSymbol(List<MathExpressionSymbol> expressions) {
        this.expressions = expressions;
    }

    @Override
    public String getTextualRepresentation() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (int i = 0; i< expressions.size(); i++){
            builder.append(expressions.get(i).getTextualRepresentation());
            if (i != expressions.size()-1 || i==0){
                builder.append(",");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    public void add(MathExpressionSymbol symbol){
        expressions.add(symbol);
    }

    public List<MathExpressionSymbol> getExpressions() {
        return expressions;
    }
}
