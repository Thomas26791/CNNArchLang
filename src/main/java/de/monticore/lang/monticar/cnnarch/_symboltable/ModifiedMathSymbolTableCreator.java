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

import de.monticore.lang.math.math._ast.ASTMathFalseExpression;
import de.monticore.lang.math.math._ast.ASTMathTrueExpression;
import de.monticore.lang.math.math._symboltable.MathSymbolTableCreator;
import de.monticore.lang.math.math._symboltable.expression.MathNameExpressionSymbol;
import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedVariables;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolvingConfiguration;

import java.util.Deque;

public class ModifiedMathSymbolTableCreator extends MathSymbolTableCreator {

    public ModifiedMathSymbolTableCreator(ResolvingConfiguration resolverConfiguration, MutableScope enclosingScope) {
        super(resolverConfiguration, enclosingScope);
    }

    public ModifiedMathSymbolTableCreator(ResolvingConfiguration resolvingConfig, Deque<MutableScope> scopeStack) {
        super(resolvingConfig, scopeStack);
    }


    @Override
    public void endVisit(ASTMathTrueExpression node){
        MathNameExpressionSymbol symbol = new MathNameExpressionSymbol(AllPredefinedVariables.TRUE_NAME);
        addToScopeAndLinkWithNode(symbol, node);
    }

    @Override
    public void endVisit(ASTMathFalseExpression node){
        MathNameExpressionSymbol symbol = new MathNameExpressionSymbol(AllPredefinedVariables.FALSE_NAME);
        addToScopeAndLinkWithNode(symbol, node);
    }
}
