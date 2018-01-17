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

import de.monticore.lang.monticar.cnnarch._ast.ASTIOLayer;
import de.monticore.lang.monticar.cnnarch._symboltable.CompositeLayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.IODeclarationSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.IOLayerSymbol;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

public class CheckUnknownIO implements CNNArchASTIOLayerCoCo {

    @Override
    public void check(ASTIOLayer node) {
        Symbol symbol = node.getSymbol().get();
        IODeclarationSymbol ioDeclaration = null;
        if (symbol instanceof IOLayerSymbol){
            ioDeclaration = ((IOLayerSymbol) symbol).getDefinition();
        }
        else if (symbol instanceof CompositeLayerSymbol){
            IOLayerSymbol layer = (IOLayerSymbol) ((CompositeLayerSymbol) symbol).getLayers().get(0);
            ioDeclaration = layer.getDefinition();
        }

        if (ioDeclaration == null){
            Log.error("0" + ErrorCodes.UNKNOWN_IO_CODE + " Unknown input or output name. " +
                            "The input or output '" + node.getName() + "' does not exist"
                    , node.get_SourcePositionStart());
        }
    }

}
