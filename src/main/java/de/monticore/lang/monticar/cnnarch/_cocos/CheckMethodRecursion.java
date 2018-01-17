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

import de.monticore.lang.monticar.cnnarch._ast.ASTMethodDeclaration;
import de.monticore.lang.monticar.cnnarch._symboltable.CompositeLayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.LayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.MethodDeclarationSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.MethodLayerSymbol;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;

public class CheckMethodRecursion implements CNNArchASTMethodDeclarationCoCo {

    Set<MethodDeclarationSymbol> seenMethods = new HashSet<>();
    boolean done;

    @Override
    public void check(ASTMethodDeclaration node) {
        done = false;
        MethodDeclarationSymbol method = (MethodDeclarationSymbol) node.getSymbol().get();
        checkForRecursion(method, method.getBody());
    }

    private void checkForRecursion(MethodDeclarationSymbol startingMethod, LayerSymbol current){
        if (!done) {
            if (current instanceof CompositeLayerSymbol) {
                for (LayerSymbol layer : ((CompositeLayerSymbol) current).getLayers()) {
                    checkForRecursion(startingMethod, layer);
                }
            }
            else if (current instanceof MethodLayerSymbol) {
                MethodDeclarationSymbol method = ((MethodLayerSymbol) current).getMethod();
                if (method != null && !method.isPredefined() && !seenMethods.contains(method)) {
                    seenMethods.add(method);
                    if (startingMethod == method) {
                        Log.error("0" + ErrorCodes.RECURSION_ERROR_CODE + " Recursion is not allowed. " +
                                        "The method '" + startingMethod.getName() + "' creates a recursive cycle."
                                , startingMethod.getSourcePosition());
                        done = true;
                    }
                    else {
                        checkForRecursion(startingMethod, method.getBody());
                    }
                }
            }
        }
    }

}
