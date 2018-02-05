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

import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

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
            if (previous != null && !isParallel()){
                current.setInputLayer(previous);
                previous.setOutputLayer(current);
            }
            else {
                if (getInputLayer().isPresent()){
                    current.setInputLayer(getInputLayer().get());
                }
                if (getOutputLayer().isPresent()){
                    current.setOutputLayer(getOutputLayer().get());
                }
            }
            previous = current;
        }
        this.layers = layers;
    }

    @Override
    public boolean isAtomic() {
        return getLayers().isEmpty();
    }

    @Override
    public void setInputLayer(LayerSymbol inputLayer) {
        super.setInputLayer(inputLayer);
        if (isParallel()){
            for (LayerSymbol current : getLayers()){
                current.setInputLayer(inputLayer);
            }
        }
        else {
            if (!getLayers().isEmpty()){
                getLayers().get(0).setInputLayer(inputLayer);
            }
        }
    }

    @Override
    public void setOutputLayer(LayerSymbol outputLayer) {
        super.setOutputLayer(outputLayer);
        if (isParallel()){
            for (LayerSymbol current : getLayers()){
                current.setOutputLayer(outputLayer);
            }
        }
        else {
            if (!getLayers().isEmpty()){
                getLayers().get(getLayers().size()-1).setOutputLayer(outputLayer);
            }
        }
    }

    @Override
    public List<LayerSymbol> getFirstAtomicLayers() {
        if (getLayers().isEmpty()){
            return Collections.singletonList(this);
        }
        else if (isParallel()){
            List<LayerSymbol> firstLayers = new ArrayList<>();
            for (LayerSymbol layer : getLayers()){
                firstLayers.addAll(layer.getFirstAtomicLayers());
            }
            return firstLayers;
        }
        else {
            return getLayers().get(0).getFirstAtomicLayers();
        }
    }

    @Override
    public List<LayerSymbol> getLastAtomicLayers() {
        if (getLayers().isEmpty()){
            return Collections.singletonList(this);
        }
        else if (isParallel()){
            List<LayerSymbol> lastLayers = new ArrayList<>();
            for (LayerSymbol layer : getLayers()){
                lastLayers.addAll(layer.getLastAtomicLayers());
            }
            return lastLayers;
        }
        else {
            return getLayers().get(getLayers().size()-1).getLastAtomicLayers();
        }
    }

    @Override
    public Set<VariableSymbol> resolve() throws ArchResolveException {
        if (!isResolved()) {
            if (isResolvable()) {
                List<LayerSymbol> resolvedLayers = new ArrayList<>();
                for (LayerSymbol layer : getLayers()) {
                    layer.resolve();
                }
            }
        }
        return getUnresolvableVariables();
    }

    @Override
    protected void resolveExpressions() throws ArchResolveException {
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
    protected void computeUnresolvableVariables(Set<VariableSymbol> unresolvableVariables, Set<VariableSymbol> allVariables) {
        for (LayerSymbol layer : getLayers()){
            layer.checkIfResolvable(allVariables);
            unresolvableVariables.addAll(layer.getUnresolvableVariables());
        }
    }

    @Override
    public List<ArchTypeSymbol> computeOutputTypes() {
        if (getLayers().isEmpty()){
            if (getInputLayer().isPresent()){
                return getInputLayer().get().getOutputTypes();
            }
            else {
                return Collections.emptyList();
            }
        }
        else {
            if (isParallel()){
                List<ArchTypeSymbol> outputShapes = new ArrayList<>(getLayers().size());
                for (LayerSymbol layer : getLayers()){
                    if (layer.getOutputTypes().size() != 0){
                        outputShapes.add(layer.getOutputTypes().get(0));
                    }
                }
                return outputShapes;
            }
            else {
                for (LayerSymbol layer : getLayers()){
                    layer.getOutputTypes();
                }
                return getLayers().get(getLayers().size() - 1).getOutputTypes();
            }
        }
    }

    @Override
    public void checkInput() {
        if (!getLayers().isEmpty()){
            for (LayerSymbol layer : getLayers()){
                layer.checkInput();
            }
            if (isParallel()){
                for (LayerSymbol layer : getLayers()){
                    if (layer.getOutputTypes().size() > 1){
                        Log.error("0" + ErrorCodes.MISSING_MERGE + " Missing merge layer (Add(), Concatenate() or [i]). " +
                                        "Each stream at the end of a parallel layer can only have one output stream. "
                                , getSourcePosition());
                    }
                }
            }
        }
    }

    @Override
    public Optional<Integer> getParallelLength() {
        if (isParallel()){
            return Optional.of(getLayers().size());
        }
        else {
            return Optional.of(1);
        }
    }

    @Override
    public Optional<List<Integer>> getSerialLengths() {
        if (isParallel()){
            return Optional.of(Collections.nCopies(getLayers().size(), 1));
        }
        else {
            return Optional.of(Collections.singletonList(getLayers().size()));
        }
    }

    @Override
    protected void putInScope(MutableScope scope) {
        Collection<Symbol> symbolsInScope = scope.getLocalSymbols().get(getName());
        if (symbolsInScope == null || !symbolsInScope.contains(this)) {
            scope.add(this);
            for (LayerSymbol layer : getLayers()) {
                layer.putInScope(getSpannedScope());
            }
        }
    }

    @Override
    public CompositeLayerSymbol copy() {
        CompositeLayerSymbol copy = new CompositeLayerSymbol();
        copy.setParallel(isParallel());
        List<LayerSymbol> layers = new ArrayList<>(getLayers().size());
        for (LayerSymbol layer : getLayers()){
            layers.add(layer.copy());
        }
        if (getAstNode().isPresent()){
            copy.setAstNode(getAstNode().get());
        }
        copy.setLayers(layers);
        return copy;
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
