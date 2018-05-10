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
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.CompositeElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.LayerDeclarationSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.LayerSymbol;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;

public class CheckLayerRecursion implements CNNArchASTLayerDeclarationCoCo {

    Set<LayerDeclarationSymbol> seenLayers = new HashSet<>();
    boolean done;

    @Override
    public void check(ASTLayerDeclaration node) {
        done = false;
        LayerDeclarationSymbol layerDeclaration = (LayerDeclarationSymbol) node.getSymbol().get();
        checkForRecursion(layerDeclaration, layerDeclaration.getBody());
    }

    private void checkForRecursion(LayerDeclarationSymbol startingLayer, ArchitectureElementSymbol current){
        if (!done) {
            if (current instanceof CompositeElementSymbol) {
                for (ArchitectureElementSymbol architectureElement : ((CompositeElementSymbol) current).getElement()) {
                    checkForRecursion(startingLayer, architectureElement);
                }
            }
            else if (current instanceof LayerSymbol) {
                LayerDeclarationSymbol layerDeclaration = ((LayerSymbol) current).getDeclaration();
                if (layerDeclaration != null && !layerDeclaration.isPredefined() && !seenLayers.contains(layerDeclaration)) {
                    seenLayers.add(layerDeclaration);
                    if (startingLayer == layerDeclaration) {
                        Log.error("0" + ErrorCodes.RECURSION_ERROR + " Recursion is not allowed. " +
                                        "The layer '" + startingLayer.getName() + "' creates a recursive cycle."
                                , startingLayer.getSourcePosition());
                        done = true;
                    }
                    else {
                        checkForRecursion(startingLayer, layerDeclaration.getBody());
                    }
                }
            }
        }
    }

}
