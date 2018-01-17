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

import java.util.*;

abstract public class Pooling extends PredefinedMethodDeclaration {

    protected Pooling(String name) {
        super(name);
    }

    @Override
    public List<ShapeSymbol> computeOutputShapes(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer) {
        return computeConvAndPoolOutputShape(inputShapes.get(0),
                layer,
                inputShapes.get(0).getChannels().get());
    }

    @Override
    public void checkInput(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer) {
        errorIfInputSizeIsNotOne(inputShapes, layer);
        errorIfInputSmallerThanKernel(inputShapes, layer);
    }

    protected void setParameters(){
        List<VariableSymbol> parameters = new ArrayList<>(Arrays.asList(
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.KERNEL_NAME)
                        .constraints(Constraints.INTEGER_TUPLE, Constraints.POSITIVE)
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.STRIDE_NAME)
                        .constraints(Constraints.INTEGER_TUPLE, Constraints.POSITIVE)
                        .defaultValue(Arrays.asList(1, 1))
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.GLOBAL_NAME)
                        .constraints(Constraints.BOOLEAN)
                        .defaultValue(false)
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.PADDING_NAME)
                        .constraints(Constraints.PADDING_TYPE)
                        .defaultValue(AllPredefinedMethods.PADDING_SAME)
                        .build()));
        for (VariableSymbol param : parameters){
            param.putInScope(getSpannedScope());
        }
        setParameters(parameters);
    }
}