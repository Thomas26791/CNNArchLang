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
package de.monticore.lang.monticar.cnnarch._symboltable;

import java.util.*;

public class CompositeLayerSymbol extends LayerSymbol {

    private boolean parallel;
    private List<LayerSymbol> layers;

    protected CompositeLayerSymbol() {
        super("");
    }

    public boolean isParallel() {
        return parallel;
    }

    protected void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    public List<LayerSymbol> getLayers() {
        return layers;
    }

    protected void setLayers(List<LayerSymbol> layers) {
        this.layers = layers;
    }

    @Override
    public boolean isCompositeLayer(){
        return true;
    }

    @Override
    public Set<String> resolve() {
        Set<String> unresolvableSet = new HashSet<>();
        //todo
        return unresolvableSet;
    }

    @Override
    protected void checkIfResolved() {
        //todo
    }

    @Override
    protected List<ShapeSymbol> computeOutputShape() {
        if (isParallel()){
            List<ShapeSymbol> outputShapes = new ArrayList<>(getLayers().size());
            for (LayerSymbol layer : getLayers()){
                //todo: assure that last layer in each parallel group has only one outputShape
                outputShapes.add(layer.getOutputShapes().get(0));
            }
            return outputShapes;
        }
        else {
            return getLayers().get(getLayers().size() - 1).getOutputShapes();
        }
    }

    @Override
    public boolean isResolvable() {
        //todo
        return false;
    }


    public static class Builder{
        private boolean parallel = false;
        private List<LayerSymbol> layers = new ArrayList<>();
        private LayerSymbol inputLayer;

        public Builder parallel(boolean parallel){
            this.parallel = parallel;
            return this;
        }

        public Builder layers(List<LayerSymbol> layers){
            this.layers = layers;
            return this;
        }

        public Builder layers(LayerSymbol... layers){
            this.layers = Arrays.asList(layers);
            return this;
        }

        public Builder inputLayer(LayerSymbol inputLayer){
            this.inputLayer = inputLayer;
            return this;
        }

        public CompositeLayerSymbol build(){
            CompositeLayerSymbol sym = new CompositeLayerSymbol();
            sym.setParallel(parallel);
            sym.setLayers(layers);
            sym.setInputLayer(inputLayer);
            return sym;
        }
    }
}
