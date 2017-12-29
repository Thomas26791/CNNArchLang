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

import de.monticore.symboltable.MutableScope;

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
        LayerSymbol previous = null;
        for (LayerSymbol current : layers){
            if (previous != null){
                current.setInputLayer(previous);
            }
            else {
                if (getInputLayer().isPresent()){
                    current.setInputLayer(getInputLayer().get());
                }
                else {
                    current.setInputLayer(null);
                }
            }
            previous = current;
        }
        this.layers = layers;
    }

    @Override
    public boolean isCompositeLayer(){
        return true;
    }

    @Override
    public Set<String> resolve() {
        if (isResolved()){
            checkIfResolvable();
            if (isResolvable()){
                List<LayerSymbol> resolvedLayers = new ArrayList<>();
                for (LayerSymbol layer : getLayers()){
                    layer.resolve();
                }
            }
        }
        return getUnresolvableNames();
    }

    @Override
    protected void resolveExpressions() {
        for (LayerSymbol layer : getLayers()){
            layer.resolveExpressions();
        }
    }

    @Override
    public boolean isResolved() {
        boolean isResolved = true;
        for (LayerSymbol layer : getLayers()){
            if (!layer.isResolved()){
                isResolved = false;
            }
        }
        return isResolved;
    }

    @Override
    protected Set<String> computeUnresolvableNames() {
        Set<String> unresolvableSet = new HashSet<>();
        for (LayerSymbol layer : getLayers()){
            layer.checkIfResolvable();
            unresolvableSet.addAll(layer.getUnresolvableNames());
        }
        return unresolvableSet;
    }

    @Override
    protected List<ShapeSymbol> computeOutputShapes() {
        if (layers.size() == 0){
            return getInputLayer().get().getOutputShapes();
        }
        else {
            if (isParallel()){
                List<ShapeSymbol> outputShapes = new ArrayList<>(getLayers().size());
                for (LayerSymbol layer : getLayers()){
                    //todo: assure with coco that last layer in each parallel group has only one outputShape
                    if (layer.getOutputShapes().size() != 0){
                        outputShapes.add(layer.getOutputShapes().get(0));
                    }
                }
                return outputShapes;
            }
            else {
                return getLayers().get(getLayers().size() - 1).getOutputShapes();
            }
        }
    }

    @Override
    public int getParallelLength() {
        if (isParallel()){
            return getLayers().size();
        }
        else {
            return 1;
        }
    }

    @Override
    public int getSerialLength() {
        if (isParallel()){
            return 1;
        }
        else {
            return getLayers().size();
        }
    }

    @Override
    protected void putInScope(MutableScope scope) {
        if (!scope.getLocalSymbols().get(getName()).contains(this)) {
            scope.add(this);
            for (LayerSymbol layer : getLayers()) {
                layer.putInScope(getSpannedScope().getAsMutableScope());
            }
        }
    }

    @Override
    public CompositeLayerSymbol copy() {
        List<LayerSymbol> layers = new ArrayList<>(getLayers().size());
        for (LayerSymbol layer : getLayers()){
            layers.add(layer.copy());
        }

        return new Builder()
                .parallel(isParallel())
                .layers(layers)
                .build();
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
