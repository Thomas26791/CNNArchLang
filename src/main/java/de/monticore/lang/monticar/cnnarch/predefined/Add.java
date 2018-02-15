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

import de.monticore.lang.monticar.cnnarch._symboltable.ArchTypeSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.MethodLayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.PredefinedMethodDeclaration;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.logging.Log;
import org.jscience.mathematics.number.Rational;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Add extends PredefinedMethodDeclaration {

    private Add() {
        super(AllPredefinedMethods.ADD_NAME);
    }

    @Override
    public List<ArchTypeSymbol> computeOutputTypes(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer) {
        List<String> range = computeStartAndEndValue(inputTypes, Rational::plus, Rational::plus);

        return Collections.singletonList(new ArchTypeSymbol.Builder()
                .channels(inputTypes.get(0).getChannels())
                .height(inputTypes.get(0).getHeight())
                .width(inputTypes.get(0).getWidth())
                .elementType(range.get(0), range.get(1))
                .build());
    }

    @Override
    public void checkInput(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer) {
        errorIfInputIsEmpty(inputTypes, layer);
        if (inputTypes.size() == 1){
            Log.warn("Add layer has only one input stream. Layer can be removed." , layer.getSourcePosition());
        }
        else if (inputTypes.size() > 1){
            List<Integer> heightList = new ArrayList<>();
            List<Integer> widthList = new ArrayList<>();
            List<Integer> channelsList = new ArrayList<>();
            for (ArchTypeSymbol shape : inputTypes){
                heightList.add(shape.getHeight());
                widthList.add(shape.getWidth());
                channelsList.add(shape.getChannels());
            }
            int countEqualHeights = (int)heightList.stream().distinct().count();
            int countEqualWidths = (int)widthList.stream().distinct().count();
            int countEqualNumberOfChannels = (int)channelsList.stream().distinct().count();
            if (countEqualHeights != 1 || countEqualWidths != 1 || countEqualNumberOfChannels != 1){
                Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT_SHAPE + " Invalid layer input. " +
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
