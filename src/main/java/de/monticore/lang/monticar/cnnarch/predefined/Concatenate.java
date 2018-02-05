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
import de.monticore.lang.monticar.cnnarch._symboltable.ArchTypeSymbol;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.monticore.lang.monticar.ranges._ast.ASTRange;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.logging.Log;
import org.jscience.mathematics.number.Rational;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Concatenate extends PredefinedMethodDeclaration {

    private Concatenate() {
        super(AllPredefinedMethods.CONCATENATE_NAME);
    }

    @Override
    public List<ArchTypeSymbol> computeOutputTypes(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer) {
        int height = inputTypes.get(0).getHeight().get();
        int width = inputTypes.get(0).getWidth().get();
        int channels = 0;
        for (ArchTypeSymbol inputShape : inputTypes) {
            channels += inputShape.getChannels().get();
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
    public void checkInput(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer) {
        if (!inputTypes.isEmpty()) {
            List<Integer> heightList = new ArrayList<>();
            List<Integer> widthList = new ArrayList<>();
            for (ArchTypeSymbol shape : inputTypes){
                heightList.add(shape.getHeight().get());
                widthList.add(shape.getWidth().get());
            }
            int countEqualHeights = (int)heightList.stream().distinct().count();
            int countEqualWidths = (int)widthList.stream().distinct().count();
            if (countEqualHeights != 1 || countEqualWidths != 1){
                Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT_SHAPE + " Invalid layer input. " +
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
        Concatenate method = new Concatenate();
        method.setParameters(new ArrayList<>());
        return method;
    }
}