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

import de.monticore.lang.monticar.cnnarch._ast.ASTIODeclaration;
import de.monticore.lang.monticar.cnnarch._symboltable.IODeclarationSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.IOLayerSymbol;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CheckIOAccessAndIOMissing implements CNNArchASTIODeclarationCoCo {

    @Override
    public void check(ASTIODeclaration node) {
        IODeclarationSymbol ioDeclaration = (IODeclarationSymbol) node.getSymbol().get();

        if (ioDeclaration.getArrayLength() == 1){
            checkSingleIO(ioDeclaration);
        }
        else {
            checkIOArray(ioDeclaration);
        }
    }


    private void checkSingleIO(IODeclarationSymbol ioDeclaration){
        if (ioDeclaration.getConnectedLayers().isEmpty()){
            Log.error("0" + ErrorCodes.MISSING_IO + " Input or output '" + ioDeclaration.getName() + "' was declared but never used."
                    , ioDeclaration.getSourcePosition());
        }
        else {
            for (IOLayerSymbol layer : ioDeclaration.getConnectedLayers()){
                if (layer.getArrayAccess().isPresent()){
                    Log.error("0" + ErrorCodes.INVALID_ARRAY_ACCESS + " Invalid IO array access. " +
                                    "This input or output is not an array."
                            , layer.getSourcePosition());
                }
            }
        }
    }


    private void checkIOArray(IODeclarationSymbol ioDeclaration){
        List<Integer> unusedIndices = IntStream.range(0, ioDeclaration.getArrayLength()).boxed().collect(Collectors.toList());

        for (IOLayerSymbol layer : ioDeclaration.getConnectedLayers()){
            if (layer.getArrayAccess().isPresent()){
                Optional<Integer> arrayAccess = layer.getArrayAccess().get().getIntValue();
                if (arrayAccess.isPresent() && arrayAccess.get() >= 0 && arrayAccess.get() < ioDeclaration.getArrayLength()){
                    unusedIndices.remove(arrayAccess.get());
                }
                else {
                    Log.error("0" + ErrorCodes.INVALID_ARRAY_ACCESS + " The IO array access value of '" + layer.getName() +
                                    "' must be an integer between 0 and " + (ioDeclaration.getArrayLength()-1) + ". " +
                                    "The current value is: " + layer.getArrayAccess().get().getValue().get().toString()
                            , layer.getSourcePosition());
                }
            }
            else{
                unusedIndices = new ArrayList<>();
            }
        }

        if (!unusedIndices.isEmpty()){
            Log.error("0" + ErrorCodes.MISSING_IO + " Input or output array with name '" + ioDeclaration.getName() + "' was declared but not used. " +
                            "The following indices are unused: " + Joiners.COMMA.join(unusedIndices) + "."
                    , ioDeclaration.getSourcePosition());
        }
    }

}
