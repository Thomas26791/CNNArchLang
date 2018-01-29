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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GlobalPooling extends PredefinedMethodDeclaration {

    protected GlobalPooling() {
        super(AllPredefinedMethods.GLOBAL_POOLING_NAME);
    }

    @Override
    public List<ShapeSymbol> computeOutputShapes(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer) {
        return Collections.singletonList(new ShapeSymbol.Builder()
                .height(1)
                .width(1)
                .channels(inputShapes.get(0).getChannels().get())
                .build());
    }

    @Override
    public void checkInput(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer) {
        errorIfInputSizeIsNotOne(inputShapes, layer);
    }

    protected void setParameters(){
        List<VariableSymbol> parameters = new ArrayList<>(Arrays.asList(
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.POOL_TYPE_NAME)
                        .constraints(Constraints.POOL_TYPE)
                        .build()));
        for (VariableSymbol param : parameters){
            param.putInScope(getSpannedScope());
        }
        setParameters(parameters);
    }

    public static GlobalPooling create(){
        GlobalPooling method = new GlobalPooling();
        method.setParameters();
        return method;
    }
}