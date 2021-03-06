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
import de.monticore.lang.monticar.cnnarch._symboltable.LayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.PredefinedLayerDeclaration;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Concatenate extends PredefinedLayerDeclaration {

    private Concatenate() {
        super(AllPredefinedLayers.CONCATENATE_NAME);
    }

    @Override
    public List<ArchTypeSymbol> computeOutputTypes(List<ArchTypeSymbol> inputTypes, LayerSymbol layer) {
        int height = inputTypes.get(0).getHeight();
        int width = inputTypes.get(0).getWidth();
        int channels = 0;
        for (ArchTypeSymbol inputShape : inputTypes) {
            channels += inputShape.getChannels();
        }

        List<String> range = computeStartAndEndValue(inputTypes, (x,y) -> x.isLessThan(y) ? x : y, (x,y) -> x.isLessThan(y) ? y : x);

        return Collections.singletonList(new ArchTypeSymbol.Builder()
                .channels(channels)
                .height(height)
                .width(width)
                .elementType(range.get(0), range.get(1))
                .build());
    }

    @Override
    public void checkInput(List<ArchTypeSymbol> inputTypes, LayerSymbol layer) {
        if (!inputTypes.isEmpty()) {
            List<Integer> heightList = new ArrayList<>();
            List<Integer> widthList = new ArrayList<>();
            for (ArchTypeSymbol shape : inputTypes){
                heightList.add(shape.getHeight());
                widthList.add(shape.getWidth());
            }
            int countEqualHeights = (int)heightList.stream().distinct().count();
            int countEqualWidths = (int)widthList.stream().distinct().count();
            if (countEqualHeights != 1 || countEqualWidths != 1){
                Log.error("0" + ErrorCodes.INVALID_ELEMENT_INPUT_SHAPE + " Invalid layer input. " +
                                "Concatenation of inputs with different resolutions is not possible. " +
                                "Input heights: " + Joiners.COMMA.join(heightList) + ". " +
                                "Input widths: " + Joiners.COMMA.join(widthList) + ". "
                        , layer.getSourcePosition());
            }
        }
        else {
            errorIfInputIsEmpty(inputTypes, layer);
        }
    }

    public static Concatenate create(){
        Concatenate declaration = new Concatenate();
        declaration.setParameters(new ArrayList<>());
        return declaration;
    }
}