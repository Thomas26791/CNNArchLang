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

import de.monticore.symboltable.CommonSymbol;

import java.util.Optional;

public class DimensionSymbol extends CommonSymbol {

    public static final DimensionKind KIND = new DimensionKind();

    private ArchSimpleExpressionSymbol valueExpression;
    private VariableSymbol ioVariable;

    public DimensionSymbol() {
        super("", KIND);
    }

    public ArchSimpleExpressionSymbol getValueExpression() {
        return valueExpression;
    }

    public void setValueExpression(ArchSimpleExpressionSymbol valueExpression) {
        this.valueExpression = valueExpression;
    }

    public Optional<VariableSymbol> getIoVariable() {
        return Optional.ofNullable(ioVariable);
    }

    public void setIoVariable(VariableSymbol ioVariable) {
        this.ioVariable = ioVariable;
    }

    public Optional<Integer> getValue(){
        Optional<Object> optObj = getValueExpression().getValue();
        return optObj.map(o -> (Integer) o);
    }

    public static DimensionSymbol of(int value){
        DimensionSymbol sym = new DimensionSymbol();
        sym.setValueExpression(ArchSimpleExpressionSymbol.of(value));
        return sym;
    }
}
