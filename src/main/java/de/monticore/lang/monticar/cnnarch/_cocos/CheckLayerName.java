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

import de.monticore.lang.monticar.cnnarch._ast.ASTLayerDeclaration;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;

public class CheckLayerName implements CNNArchASTLayerDeclarationCoCo {

    Set<String> methodNames = new HashSet<>();

    @Override
    public void check(ASTLayerDeclaration node) {
        String name = node.getName();
        if (name.isEmpty() || !Character.isLowerCase(name.codePointAt(0))){
            Log.error("0" + ErrorCodes.ILLEGAL_NAME + " Illegal name: " + name +
                            ". All new variable and method names have to start with a lowercase letter. "
                    , node.get_SourcePositionStart());
        }

        if (methodNames.contains(name)){
            Log.error("0" + ErrorCodes.DUPLICATED_NAME + " Duplicated method name. " +
                            "The name '" + name + "' is already used."
                    , node.get_SourcePositionStart());
        }
        else {
            methodNames.add(name);
        }
    }

}
