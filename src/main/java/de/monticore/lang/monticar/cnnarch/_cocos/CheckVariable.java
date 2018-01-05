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
package de.monticore.lang.monticar.cnnarch._cocos;

import de.monticore.lang.monticar.cnnarch._ast.ASTParameter;
import de.monticore.lang.monticar.cnnarch._ast.ASTVariable;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.monticore.lang.monticar.cnnarch.helper.PredefinedVariables;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CheckVariable implements CNNArchASTVariableCoCo {

    Set<String> variableNames = new HashSet<>();


    @Override
    public void check(ASTVariable node) {
        String name = node.getName();
        if (name.isEmpty() || !Character.isLowerCase(name.codePointAt(0))){
            Log.error("0" + ErrorCodes.ILLEGAL_NAME_CODE + " Illegal name: " + name +
                            ". All new variable and method names have to start with a lowercase letter. "
                    , node.get_SourcePositionStart());
        }
        if (name.equals(PredefinedVariables.TRUE_NAME) || name.equals(PredefinedVariables.FALSE_NAME)){
            Log.error("0" + ErrorCodes.ILLEGAL_NAME_CODE + " Illegal name: " + name +
                            ". No variable can be named 'true' or 'false'"
                    , node.get_SourcePositionStart());
        }

        boolean duplicated = false;
        if (variableNames.contains(name)){
            if (node instanceof ASTParameter){
                Collection<Symbol> collection = ((ASTParameter) node).getEnclosingScope().get().getLocalSymbols().get(name);
                if (collection.size() > 1){
                    duplicated = true;
                }
            }
            else {
                duplicated = true;
            }
        }
        else{
            variableNames.add(name);
        }

        if (duplicated){
            Log.error("0" + ErrorCodes.DUPLICATED_NAME_CODE + " Duplicated name: " + name +
                            ". This name is already defined."
                    , node.get_SourcePositionStart());
        }
    }

}
