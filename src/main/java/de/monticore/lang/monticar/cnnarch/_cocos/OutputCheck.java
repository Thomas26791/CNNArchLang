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

import de.monticore.lang.monticar.cnnarch._ast.*;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

public class OutputCheck implements CNNArchASTOutputStructureCoCo {

    @Override
    public void check(ASTOutputStructure node) {
        checkFullyConnectedLayer(node);
    }

    public void checkFullyConnectedLayer(ASTOutputStructure node){
        ASTFullyConnectedMethod lastFullyConnectedLayer = null;

        for (ASTArchitectureElement element: node.getElements()){
            if (element instanceof ASTFullyConnectedMethod){
                if (lastFullyConnectedLayer != null){
                    Log.error("0x06021 The argument 'units' is required if the fullyConnected()-layer is not the last fc-layer"
                            , lastFullyConnectedLayer.get_SourcePositionStart());
                    lastFullyConnectedLayer = null;
                }

                ASTArgumentListing args = ((ASTFullyConnectedMethod) element).getArgumentListing();

                Optional<ASTArgumentRhs> arg = args.getArgument(ASTFullyConnectedArgument.UNITS);
                if (!arg.isPresent()){
                    // element has to be the last layer because it does not set the argument 'units'
                    lastFullyConnectedLayer = (ASTFullyConnectedMethod) element;
                }

            }
        }

        if (lastFullyConnectedLayer == null){
            Log.error("0x06028 The output block has to contain a fullyConnectedLayer without the argument 'units'. " +
                            "This is because the number of outputs is variable for all architectures. " +
                            "Example: output{ fullyConnected() activation.softmax() } -> out"
                    , node.get_SourcePositionEnd());
        }
    }

}
