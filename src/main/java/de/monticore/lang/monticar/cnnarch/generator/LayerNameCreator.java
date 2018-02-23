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
package de.monticore.lang.monticar.cnnarch.generator;

import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.lang.monticar.cnnarch.predefined.Convolution;
import de.monticore.lang.monticar.cnnarch.predefined.FullyConnected;
import de.monticore.lang.monticar.cnnarch.predefined.Pooling;

import java.util.*;

public class LayerNameCreator {

    private Map<LayerSymbol, String> layerToName = new HashMap<>();
    private Map<String, LayerSymbol> nameToLayer = new HashMap<>();

    public LayerNameCreator(ArchitectureSymbol architecture) {
        name(architecture.getBody(), 1, new ArrayList<>());
    }

    public LayerSymbol getLayer(String name){
        return nameToLayer.get(name);
    }

    public String getName(LayerSymbol layer){
        return layerToName.get(layer);
    }

    protected int name(CompositeLayerSymbol compositeLayer, int stage, List<Integer> streamIndices){
        if (compositeLayer.isParallel()){
            int startStage = stage + 1;
            streamIndices.add(1);
            int lastIndex = streamIndices.size() - 1;

            List<Integer> endStages = new ArrayList<>();
            for (LayerSymbol subLayer : compositeLayer.getLayers()){
                endStages.add(name(subLayer, startStage, streamIndices));
                streamIndices.set(lastIndex, streamIndices.get(lastIndex) + 1);
            }

            streamIndices.remove(lastIndex);
            return Collections.max(endStages) + 1;
        }
        else {
            int endStage = stage;
            for (LayerSymbol subLayer : compositeLayer.getLayers()){
                endStage = name(subLayer, endStage, streamIndices);
            }
            return endStage;
        }
    }

    protected int name(LayerSymbol layer, int stage, List<Integer> streamIndices){
        if (layer instanceof CompositeLayerSymbol){
            return name((CompositeLayerSymbol) layer, stage, streamIndices);
        }
        else if (layer instanceof MethodLayerSymbol){
            if (layer.isAtomic()){
                if (layer.getMaxSerialLength().get() > 0){
                    return add(layer, stage, streamIndices);
                }
                else {
                    return stage;
                }
            }
            else {
                LayerSymbol resolvedLayer = ((MethodLayerSymbol) layer).getResolvedThis().get();
                return (name(resolvedLayer, stage, streamIndices));
            }
        }
        else {
            return add(layer, stage, streamIndices);
        }
    }

    protected int add(LayerSymbol layer, int stage, List<Integer> streamIndices){
        int endStage = stage;
        if (!layerToName.containsKey(layer)) {
            String name = createName(layer, endStage, streamIndices);

            while (nameToLayer.containsKey(name)) {
                endStage++;
                name = createName(layer, endStage, streamIndices);
            }

            layerToName.put(layer, name);
            nameToLayer.put(name, layer);
        }
        return endStage;
    }

    protected String createName(LayerSymbol layer, int stage, List<Integer> streamIndices){
        if (layer instanceof IOLayerSymbol){
            String name = createBaseName(layer);
            IOLayerSymbol ioLayer = (IOLayerSymbol) layer;
            if (ioLayer.getArrayAccess().isPresent()){
                int arrayAccess = ioLayer.getArrayAccess().get().getIntValue().get();
                name = name + arrayAccess;
            }
            return name;
        }
        else {
            return createBaseName(layer) + stage + createStreamPostfix(streamIndices);
        }
    }


    protected String createBaseName(LayerSymbol layer){
        if (layer instanceof MethodLayerSymbol) {
            MethodDeclarationSymbol method = ((MethodLayerSymbol) layer).getMethod();
            if (method instanceof Convolution) {
                return "conv";
            } else if (method instanceof FullyConnected) {
                return "fc";
            } else if (method instanceof Pooling) {
                return "pool";
            } else {
                return method.getName().toLowerCase();
            }
        }
        else if (layer instanceof CompositeLayerSymbol){
            return "group";
        }
        else {
            return layer.getName();
        }
    }

    protected String createStreamPostfix(List<Integer> streamIndices){
        StringBuilder stringBuilder = new StringBuilder();
        for (int streamIndex : streamIndices){
            stringBuilder.append("_");
            stringBuilder.append(streamIndex);
        }
        return stringBuilder.toString();
    }
}

