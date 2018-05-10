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

import de.monticore.lang.monticar.cnnarch._ast.ASTArchArgument;
import de.monticore.lang.monticar.cnnarch._symboltable.ArgumentSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.LayerDeclarationSymbol;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.logging.Log;

public class CheckArgument implements CNNArchASTArchArgumentCoCo {

    @Override
    public void check(ASTArchArgument node) {
        ArgumentSymbol argument = (ArgumentSymbol) node.getSymbol().get();
        LayerDeclarationSymbol layerDeclaration = argument.getLayer().getDeclaration();
        if (layerDeclaration != null && argument.getParameter() ==  null){
            Log.error("0"+ ErrorCodes.UNKNOWN_ARGUMENT + " Unknown Argument. " +
                            "Parameter with name '" + node.getName() + "' does not exist. " +
                            "Possible arguments are: " + Joiners.COMMA.join(layerDeclaration.getParameters())
                    , node.get_SourcePositionStart());
        }
    }

}
