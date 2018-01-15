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

public class Lrn extends PredefinedMethodDeclaration {

    private Lrn() {
        super(AllPredefinedMethods.LRN_NAME);
    }

    @Override
    public List<ShapeSymbol> computeOutputShapes(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer) {
        return inputShapes;
        //todo: check
    }

    @Override
    public void checkInput(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer) {
        checkOneInput(inputShapes, layer);
        //todo: check

    }

    public static Lrn create(){
        Lrn method = new Lrn();
        List<VariableSymbol> parameters = new ArrayList<>(Arrays.asList(
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.NSIZE_NAME)
                        .constraints(Constraints.INTEGER, Constraints.NON_NEGATIVE)
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.KNORM_NAME)
                        .constraints(Constraints.NUMBER)
                        .defaultValue(2)
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.ALPHA_NAME)
                        .constraints(Constraints.NUMBER)
                        .defaultValue(0.0001)
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedMethods.BETA_NAME)
                        .constraints(Constraints.NUMBER)
                        .defaultValue(0.75)
                        .build()));
        for (VariableSymbol param : parameters){
            param.putInScope(method.getSpannedScope());
        }
        method.setParameters(parameters);
        return method;
    }
}
