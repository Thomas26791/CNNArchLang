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
        if (!isFullyResolved()) {
            for (LayerSymbol layer : layers) {
                unresolvableSet.addAll(layer.resolve());
            }
        }
        return unresolvableSet;
    }

    @Override
    protected void checkIfResolved() {
        boolean isResolved = true;
        for (LayerSymbol layer : layers){
            layer.checkIfResolved();
            if (!layer.isFullyResolved()){
                isResolved = false;
            }
        }
        setFullyResolved(isResolved);
    }


    public static class Builder{
        private boolean parallel = false;
        private List<LayerSymbol> layers = new ArrayList<>();

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

        public CompositeLayerSymbol build(){
            CompositeLayerSymbol sym = new CompositeLayerSymbol();
            sym.setParallel(parallel);
            sym.setLayers(layers);
            return sym;
        }
    }
}
