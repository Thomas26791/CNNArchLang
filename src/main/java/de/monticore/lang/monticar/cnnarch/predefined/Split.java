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
import java.util.List;

public class Split extends PredefinedMethodDeclaration {

    private Split() {
        super(AllPredefinedMethods.SPLIT_NAME);
    }

    @Override
    public List<ArchTypeSymbol> computeOutputTypes(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer) {
        ArchTypeSymbol inputShape = inputTypes.get(0);
        int numberOfSplits = layer.getIntValue(AllPredefinedMethods.NUM_SPLITS_NAME).get();
        int inputHeight = inputShape.getHeight();
        int inputWidth = inputShape.getWidth();
        int inputChannels = inputShape.getChannels();

        int outputChannels = inputChannels / numberOfSplits;
        int outputChannelsLast = inputChannels - (numberOfSplits-1) * outputChannels;

        List<ArchTypeSymbol> outputShapes  = new ArrayList<>(numberOfSplits);
        for (int i = 0; i < numberOfSplits; i++){
            if (i == numberOfSplits - 1) {
                outputShapes.add(new ArchTypeSymbol.Builder()
                        .height(inputHeight)
                        .width(inputWidth)
                        .channels(outputChannelsLast)
                        .elementType(inputTypes.get(0).getElementType())
                        .build());
            } else {
                outputShapes.add(new ArchTypeSymbol.Builder()
                        .height(inputHeight)
                        .width(inputWidth)
                        .channels(outputChannels)
                        .elementType(inputTypes.get(0).getElementType())
                        .build());
            }
        }
        return outputShapes;
    }

    @Override
    public void checkInput(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer) {
        if (inputTypes.size() == 1) {
            int inputChannels = inputTypes.get(0).getChannels();
            int numberOfSplits = layer.getIntValue(AllPredefinedMethods.NUM_SPLITS_NAME).get();

            if (inputChannels < numberOfSplits){
                Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT_SHAPE + " Invalid layer input. " +
                                "The number of input channels " +  inputChannels +
                                " is smaller than the number of splits " + numberOfSplits + "."
                        , layer.getSourcePosition());
            }
        }
        else {
            errorIfInputSizeIsNotOne(inputTypes, layer);
        }
    }

    public static Split create(){
        Split method = new Split();
        List<VariableSymbol> parameters = new ArrayList<>(Arrays.asList(
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.NUM_SPLITS_NAME)
                        .constraints(Constraints.INTEGER, Constraints.POSITIVE)
                        .build()));
        method.setParameters(parameters);
        return method;
    }
}
