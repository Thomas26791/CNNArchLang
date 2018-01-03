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

import de.monticore.lang.monticar.cnnarch.ErrorMessages;
import de.monticore.lang.monticar.cnnarch.PredefinedVariables;
import de.monticore.lang.monticar.cnnarch._ast.ASTMethodDeclaration;
import de.monticore.lang.monticar.cnnarch._ast.ASTNameable;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;

public class CheckNames implements CNNArchASTNameableCoCo {

    Set<String> methodNames = new HashSet<>();

    @Override
    public void check(ASTNameable node) {
        String name = node.getName();
        if (name.isEmpty() || !Character.isLowerCase(name.codePointAt(0))){
            Log.error("0" + ErrorMessages.ILLEGAL_NAME_CODE + " Illegal name: " + name +
                    ". All new variable and method names have to start with a lowercase letter. "
                    , node.get_SourcePositionStart());
        }
        if (name.equals(PredefinedVariables.TRUE_NAME) || name.equals(PredefinedVariables.FALSE_NAME)){
            Log.error("0" + ErrorMessages.ILLEGAL_NAME_CODE + " Illegal name: " + name +
                            ". No method or variable can be named 'true' or 'false'"
                    , node.get_SourcePositionStart());
        }

        if (node instanceof ASTMethodDeclaration){
            if (methodNames.contains(name)){
                Log.error("0" + ErrorMessages.ILLEGAL_NAME_CODE + " Duplicated name: " + name +
                        ". This name was defined multiple times."
                        , node.get_SourcePositionStart());
            }
            else {
                methodNames.add(name);
            }
        }
    }

}
