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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Flatten extends PredefinedLayerDeclaration {

    private Flatten() {
        super(AllPredefinedLayers.FLATTEN_NAME);
    }

    @Override
    public List<ArchTypeSymbol> computeOutputTypes(List<ArchTypeSymbol> inputTypes, LayerSymbol layer) {
        return Collections.singletonList(new ArchTypeSymbol.Builder()
                .height(1)
                .width(1)
                .channels(inputTypes.get(0).getHeight()
                        * inputTypes.get(0).getWidth()
                        * inputTypes.get(0).getChannels())
                .elementType(inputTypes.get(0).getDomain())
                .build());
    }

    @Override
    public void checkInput(List<ArchTypeSymbol> inputTypes, LayerSymbol layer) {
        errorIfInputSizeIsNotOne(inputTypes, layer);
    }

    public static Flatten create(){
        Flatten declaration = new Flatten();
        declaration.setParameters(new ArrayList<>());
        return declaration;
    }
}
