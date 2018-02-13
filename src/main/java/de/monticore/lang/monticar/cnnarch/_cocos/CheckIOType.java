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

import de.monticore.lang.monticar.cnnarch._ast.ASTArchitecture;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchSimpleExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchTypeSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.IODeclarationSymbol;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

public class CheckIOType implements CNNArchASTArchitectureCoCo {

    @Override
    public void check(ASTArchitecture node) {
        ArchitectureSymbol architecture = (ArchitectureSymbol) node.getSymbol().get();

        for (IODeclarationSymbol ioDeclaration : architecture.getIODeclarations()){
            check(ioDeclaration);
        }
    }

    public void check(IODeclarationSymbol ioDeclaration) {
        ArchTypeSymbol type = ioDeclaration.getType();

        if (type.getElementType().isIsComplex() || type.getElementType().isIsBoolean()){
            Log.error("0" + ErrorCodes.INVALID_IO_TYPE + " Invalid IO element type. " +
                    "Type has to be rational or whole number.");
        }

        if (type.getDimensionSymbols().size() == 2 || type.getDimensionSymbols().size() > 3){
            Log.error("0" + ErrorCodes.INVALID_IO_TYPE + " Invalid dimension shape. Shape has to be either of size 1 or 3 (e.g. {number_of_channels, height, width})."
                    , ioDeclaration.getSourcePosition());
        }
        else {
            for (ArchSimpleExpressionSymbol dimension : type.getDimensionSymbols()){
                Optional<Integer> value = dimension.getIntValue();
                if (!value.isPresent() || value.get() <= 0){
                    Log.error("0" + ErrorCodes.INVALID_IO_TYPE + " Invalid shape. " +
                                    "The dimension sizes can only be positive integers."
                            , dimension.getSourcePosition());
                }
            }
        }

        if (Log.getFindings().isEmpty()){
            if (ioDeclaration.isInput() && type.getChannels() != 3 && type.getChannels() != 1){
                if (type.getHeight() > 1 || type.getWidth() > 1){
                    Log.warn("The number of channels of input '" +
                                    ioDeclaration.getName() + "' is: " + type.getChannels() +
                                    ". Heigth: " + type.getHeight() + ". Width: " + type.getWidth() + ". " +
                                    "This is unusual and a sign of an error. " +
                                    "The standard data format of this language is CHW and not HWC. "
                            , ioDeclaration.getSourcePosition());
                }
            }
        }
    }

}
