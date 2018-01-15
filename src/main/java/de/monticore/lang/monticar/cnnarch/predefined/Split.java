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
    public List<ShapeSymbol> computeOutputShapes(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer) {
        ShapeSymbol inputShape = inputShapes.get(0);
        int numberOfSplits = layer.getIntValue(AllPredefinedMethods.NUM_SPLITS_NAME).get();
        int inputHeight = inputShape.getHeight().get();
        int inputWidth = inputShape.getWidth().get();
        int inputChannels = inputShape.getChannels().get();

        int outputChannels = inputChannels / numberOfSplits;
        int outputChannelsLast = inputChannels - (numberOfSplits-1) * outputChannels;

        List<ShapeSymbol> outputShapes  = new ArrayList<>(numberOfSplits);
        for (int i = 0; i < numberOfSplits; i++){
            if (i == numberOfSplits - 1) {
                outputShapes.add(new ShapeSymbol.Builder()
                        .height(inputHeight)
                        .width(inputWidth)
                        .channels(outputChannelsLast)
                        .build());
            } else {
                outputShapes.add(new ShapeSymbol.Builder()
                        .height(inputHeight)
                        .width(inputWidth)
                        .channels(outputChannels)
                        .build());
            }
        }
        return outputShapes;
    }

    @Override
    public void checkInput(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer) {
        checkOneInput(inputShapes, layer);
        if (inputShapes.size() == 1) {
            int inputChannels = inputShapes.get(0).getChannels().get();
            int numberOfSplits = layer.getIntValue(AllPredefinedMethods.NUM_SPLITS_NAME).get();

            if (inputChannels < numberOfSplits){
                Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT + " Invalid layer input. " +
                                "The number of input channels " +  inputChannels +
                                " is smaller than the number of splits " + numberOfSplits + "."
                        , layer.getSourcePosition());
            }
        }
    }

    public static Split create(){
        Split method = new Split();
        List<VariableSymbol> parameters = new ArrayList<>(Arrays.asList(
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.NUM_SPLITS_NAME)
                        .constraints(Constraints.INTEGER, Constraints.POSITIVE)
                        .build()));
        for (VariableSymbol param : parameters){
            param.putInScope(method.getSpannedScope());
        }
        method.setParameters(parameters);
        return method;
    }
}
