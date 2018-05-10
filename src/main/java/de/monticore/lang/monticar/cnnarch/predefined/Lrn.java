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

public class Lrn extends PredefinedLayerDeclaration {

    private Lrn() {
        super(AllPredefinedLayers.LRN_NAME);
    }

    @Override
    public List<ArchTypeSymbol> computeOutputTypes(List<ArchTypeSymbol> inputTypes, LayerSymbol layer) {
        return inputTypes;
    }

    @Override
    public void checkInput(List<ArchTypeSymbol> inputTypes, LayerSymbol layer) {
        errorIfInputSizeIsNotOne(inputTypes, layer);
    }

    public static Lrn create(){
        Lrn declaration = new Lrn();
        List<VariableSymbol> parameters = new ArrayList<>(Arrays.asList(
                new VariableSymbol.Builder()
                        .name(AllPredefinedLayers.NSIZE_NAME)
                        .constraints(Constraints.INTEGER, Constraints.NON_NEGATIVE)
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedLayers.KNORM_NAME)
                        .constraints(Constraints.NUMBER)
                        .defaultValue(2)
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedLayers.ALPHA_NAME)
                        .constraints(Constraints.NUMBER)
                        .defaultValue(0.0001)
                        .build(),
                new VariableSymbol.Builder()
                        .name(AllPredefinedLayers.BETA_NAME)
                        .constraints(Constraints.NUMBER)
                        .defaultValue(0.75)
                        .build()));
        declaration.setParameters(parameters);
        return declaration;
    }
}
