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

import de.monticore.lang.math.math._symboltable.expression.MathArithmeticExpressionSymbol;
import de.monticore.lang.math.math._symboltable.expression.MathCompareExpressionSymbol;
import de.monticore.lang.math.math._symboltable.expression.MathExpressionSymbol;
import de.monticore.lang.monticar.interfaces.TextualExpression;

import java.util.Optional;

public class ArchSimpleValueSymbol extends ArchValueSymbol implements TextualExpression {

    private MathExpressionSymbol expression;

    public ArchSimpleValueSymbol() {
        super();
    }

    public MathExpressionSymbol getExpression() {
        return expression;
    }

    public void setExpression(MathExpressionSymbol expression) {
        this.expression = expression;
    }

    @Override
    public boolean isBoolean(){
        return (expression instanceof MathCompareExpressionSymbol);
    }

    @Override
    public boolean isNumber(){
        return (expression instanceof MathArithmeticExpressionSymbol);
    }

    @Override
    public boolean isTuple(){
        return (expression instanceof TupleExpressionSymbol);
    }

    @Override
    public Optional<Boolean> isInt(){
        Optional<Boolean> isInt = Optional.empty();
        if (isNumber()){
            Optional<Object> optValue = getValue();
            if (optValue.isPresent()){
                /*if (optValue.get() instanceof Double){
                    Double value = (Double) optValue.get();
                    isInt = Optional.of(Math.round(value) == value);
                }*/
                if (optValue.get() instanceof Integer){
                    isInt = Optional.of(true);
                }
                else {
                    isInt = Optional.of(false);
                }
            }
        }
        return isInt;
    }

    @Override
    public boolean isSimpleValue() {
        return true;
    }

    @Override
    public Optional<Object> getValue() {
        //todo
        return null;
    }

    @Override
    public ArchSimpleValueSymbol resolve() {
        //todo
        return null;
    }

    @Override
    public String getTextualRepresentation() {
        return expression.getTextualRepresentation();
    }
}
