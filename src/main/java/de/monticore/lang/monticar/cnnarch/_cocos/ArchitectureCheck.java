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
package de.monticore.lang.monticar.cnnarch._cocos;

import de.monticore.lang.monticar.cnnarch._ast.*;

import java.util.List;

public class ArchitectureCheck implements CNNArchASTArchitectureCoCo {

    //private static final ASTFlattenMethod.Builder flattenMethodBuilder = new ASTFlattenMethod.Builder().name("Flatten");

    @Override
    public void check(ASTArchitecture node) {
            completeArchitecture(node.getMainLayers());
    }

    //set position for each layer and insert flatten layer if necessary
    private void completeArchitecture(List<ASTMainLayer> layers) {
        /*int pos = 0;
        boolean isConvNet = false;
        int flattenPos = -1;

        for(ASTMainLayer layer : layers) {

            if (layer.getMethod() instanceof ASTConvolutionMethod) {
                isConvNet = true;
            }

            if (layer.getMethod() instanceof ASTFullyConnectedMethod && flattenPos == -1) {
                flattenPos = pos;
                pos++;
            }

            //layer.setPosition(pos);
            pos++;
        }

        if (isConvNet) {
            ASTMainLayer flattenLayer = (new ASTMainLayer.Builder()).method(flattenMethodBuilder.build()).build();
            layers.add(flattenPos, flattenLayer);
            //flattenLayer.setPosition(flattenPos);
        }*/
    }
}