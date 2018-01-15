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
package de.monticore.lang.monticar.cnnarch.predefined;

import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Get extends PredefinedMethodDeclaration {

    private Get() {
        super(AllPredefinedMethods.GET_NAME);
    }

    @Override
    public List<ShapeSymbol> computeOutputShapes(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer) {
        return Collections.singletonList(
                inputShapes.get(layer.getIntValue(AllPredefinedMethods.INDEX_NAME).get())
        );
    }

    @Override
    public void checkInput(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer) {
        int index = layer.getIntValue(AllPredefinedMethods.INDEX_NAME).get();
        if (inputShapes.size() <= index){
            Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT + " Invalid layer input. " +
                            "The selected input stream has the index " + index +
                            " but there are only " + inputShapes.size() + " input streams."
                    , layer.getSourcePosition());
        }
    }

    public static Get create(){
        Get method = new Get();
        List<VariableSymbol> parameters = new ArrayList<>(Arrays.asList(
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.INDEX_NAME)
                        .constraints(Constraints.INTEGER, Constraints.NON_NEGATIVE)
                        .build()));
        for (VariableSymbol param : parameters){
            param.putInScope(method.getSpannedScope());
        }
        method.setParameters(parameters);
        return method;
    }
}
