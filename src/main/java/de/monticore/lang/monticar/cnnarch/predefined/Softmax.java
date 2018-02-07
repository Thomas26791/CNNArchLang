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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Softmax extends PredefinedMethodDeclaration {

    private Softmax() {
        super(AllPredefinedMethods.SOFTMAX_NAME);
    }

    @Override
    public List<ArchTypeSymbol> computeOutputTypes(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer) {
        return Collections.singletonList(
                new ArchTypeSymbol.Builder()
                        .channels(inputTypes.get(0).getChannels())
                        .height(inputTypes.get(0).getHeight())
                        .width(inputTypes.get(0).getWidth())
                        .elementType("0", "1")
                        .build());
    }

    @Override
    public void checkInput(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer) {
        errorIfInputSizeIsNotOne(inputTypes, layer);
    }

    public static Softmax create(){
        Softmax method = new Softmax();
        method.setParameters(new ArrayList<>());
        return method;
    }
}