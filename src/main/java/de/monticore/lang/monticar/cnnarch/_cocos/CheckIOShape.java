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

import de.monticore.lang.monticar.cnnarch._ast.ASTDimension;
import de.monticore.lang.monticar.cnnarch._ast.ASTIODeclaration;
import de.monticore.lang.monticar.cnnarch._symboltable.ShapeSymbol;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.monticore.lang.monticar.types2._ast.ASTUnitNumberResolution;
import de.se_rwth.commons.logging.Log;
import org.jscience.mathematics.number.Rational;

import java.util.Optional;

public class CheckIOShape implements CNNArchASTIODeclarationCoCo {

    @Override
    public void check(ASTIODeclaration node) {
        int shapeSize = node.getType().getShape().getDimensions().size();
        if (shapeSize != 1 && shapeSize != 3){
            Log.error("0" + ErrorCodes.INVALID_IO_SHAPE + " Invalid shape. " +
                            "IO Shape has to be either {height, width, channels} or {channels}."
                    , node.getType().getShape().get_SourcePositionStart());
        }
        else {
            for (ASTDimension dimension : node.getType().getShape().getDimensions()){
                Rational rational;
                if (dimension.getIntLiteral().isPresent()){
                    rational = dimension.getIntLiteral().get().getNumber().get();
                }
                else {
                    rational = dimension.getIOVariable().get().getIntRhs().get().getNumber().get();
                }
                if (rational.getDivisor().intValue() != 1 || !rational.isPositive()){
                    Log.error("0" + ErrorCodes.INVALID_IO_SHAPE + " Invalid shape. " +
                                    "The dimension can only be defined by a positive integer."
                            , dimension.get_SourcePositionStart());
                }
            }
        }

    }

}
