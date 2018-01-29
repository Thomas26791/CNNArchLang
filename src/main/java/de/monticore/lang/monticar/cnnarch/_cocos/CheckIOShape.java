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

import de.monticore.lang.monticar.cnnarch._ast.ASTDimensionArgument;
import de.monticore.lang.monticar.cnnarch._ast.ASTShape;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchSimpleExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ShapeSymbol;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

public class CheckIOShape implements CNNArchASTShapeCoCo {

    @Override
    public void check(ASTShape node) {
        boolean hasHeight = false;
        boolean hasWidth = false;
        boolean hasChannels = false;
        for (ASTDimensionArgument dimensionArg : node.getDimensions()){
            if (dimensionArg.getWidth().isPresent()){
                if (hasWidth){
                    repetitionError(dimensionArg);
                }
                hasWidth = true;
            }
            else if (dimensionArg.getHeight().isPresent()){
                if (hasHeight){
                    repetitionError(dimensionArg);
                }
                hasHeight = true;
            }
            else {
                if (hasChannels){
                    repetitionError(dimensionArg);
                }
                hasChannels = true;
            }
        }


        ShapeSymbol shape = (ShapeSymbol) node.getSymbol().get();
        for (ArchSimpleExpressionSymbol dimension : shape.getDimensionSymbols()){
            Optional<Integer> value = dimension.getIntValue();
            if (!value.isPresent() || value.get() <= 0){
                Log.error("0" + ErrorCodes.INVALID_IO_SHAPE + " Invalid shape. " +
                                "The dimensions can only be defined by a positive integer."
                        , dimension.getSourcePosition());
            }
        }
    }

    private void repetitionError(ASTDimensionArgument node){
        Log.error("0" + ErrorCodes.INVALID_IO_SHAPE + " Invalid shape. " +
                        "The dimension '" + node.getName().get() + "' was defined multiple times. "
                , node.get_SourcePositionStart());
    }

}
