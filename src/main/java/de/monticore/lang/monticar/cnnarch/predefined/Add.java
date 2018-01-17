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

import de.monticore.lang.monticar.cnnarch._symboltable.MethodLayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.PredefinedMethodDeclaration;
import de.monticore.lang.monticar.cnnarch._symboltable.ShapeSymbol;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Add extends PredefinedMethodDeclaration {

    private Add() {
        super(AllPredefinedMethods.ADD_NAME);
    }

    @Override
    public List<ShapeSymbol> computeOutputShapes(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer) {
        return Collections.singletonList(inputShapes.get(0));
    }

    @Override
    public void checkInput(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer) {
        errorIfInputIsEmpty(inputShapes, layer);
        if (inputShapes.size() == 1){
            Log.warn("Add layer has only one input stream." , layer.getSourcePosition());
        }
        else if (inputShapes.size() > 1){
            List<Integer> heightList = new ArrayList<>();
            List<Integer> widthList = new ArrayList<>();
            List<Integer> channelsList = new ArrayList<>();
            for (ShapeSymbol shape : inputShapes){
                heightList.add(shape.getHeight().get());
                widthList.add(shape.getWidth().get());
                channelsList.add(shape.getChannels().get());
            }
            int countEqualHeights = (int)heightList.stream().distinct().count();
            int countEqualWidths = (int)widthList.stream().distinct().count();
            int countEqualNumberOfChannels = (int)channelsList.stream().distinct().count();
            if (countEqualHeights != 1 || countEqualWidths != 1 || countEqualNumberOfChannels != 1){
                Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT + " Invalid layer input. " +
                                "Shapes of all input streams must be equal. " +
                                "Input heights: " + Joiners.COMMA.join(heightList) + ". " +
                                "Input widths: " + Joiners.COMMA.join(widthList) + ". " +
                                "Number of input channels: " + Joiners.COMMA.join(channelsList) + ". "
                        , layer.getSourcePosition());
            }
        }
    }

    public static Add create(){
        Add method = new Add();
        method.setParameters(new ArrayList<>());
        return method;
    }
}
