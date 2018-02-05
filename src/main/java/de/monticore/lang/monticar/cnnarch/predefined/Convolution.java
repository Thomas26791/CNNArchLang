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
import java.util.List;

public class Convolution extends PredefinedMethodDeclaration {

    private Convolution() {
        super(AllPredefinedMethods.CONVOLUTION_NAME);
    }

    @Override
    public List<ArchTypeSymbol> computeOutputTypes(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer) {
        return computeConvAndPoolOutputShape(inputTypes.get(0),
                layer,
                layer.getIntValue(AllPredefinedMethods.CHANNELS_NAME).get());
    }

    @Override
    public void checkInput(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer) {
        errorIfInputSizeIsNotOne(inputTypes, layer);
        errorIfInputSmallerThanKernel(inputTypes, layer);
    }

    public static Convolution create(){
        Convolution method = new Convolution();
        List<VariableSymbol> parameters = new ArrayList<>(Arrays.asList(
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.KERNEL_NAME)
                        .constraints(Constraints.INTEGER_TUPLE, Constraints.POSITIVE)
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.CHANNELS_NAME)
                        .constraints(Constraints.INTEGER, Constraints.POSITIVE)
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.STRIDE_NAME)
                        .constraints(Constraints.INTEGER_TUPLE, Constraints.POSITIVE)
                        .defaultValue(Arrays.asList(1, 1))
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.NOBIAS_NAME)
                        .constraints(Constraints.BOOLEAN)
                        .defaultValue(false)
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.PADDING_NAME)
                        .constraints(Constraints.PADDING_TYPE)
                        .defaultValue(AllPredefinedMethods.PADDING_SAME)
                        .build()));
        method.setParameters(parameters);
        return method;
    }
}
