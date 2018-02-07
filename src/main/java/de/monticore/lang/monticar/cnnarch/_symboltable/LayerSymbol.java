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

import de.monticore.symboltable.CommonScopeSpanningSymbol;
import de.monticore.symboltable.MutableScope;
import de.se_rwth.commons.Joiners;

import java.util.*;

public abstract class LayerSymbol extends CommonScopeSpanningSymbol {

    public static final LayerKind KIND = new LayerKind();

    private LayerSymbol inputLayer;
    private LayerSymbol outputLayer;
    private List<ArchTypeSymbol> outputTypes = null;
    private Set<VariableSymbol> unresolvableVariables = null;

    protected LayerSymbol(String name) {
        super(name, KIND);
    }

    @Override
    protected LayerScope createSpannedScope() {
        return new LayerScope();
    }

    @Override
    public LayerScope getSpannedScope() {
        return (LayerScope) super.getSpannedScope();
    }

    public Optional<LayerSymbol> getInputLayer() {
        return Optional.ofNullable(inputLayer);
    }

    public void setInputLayer(LayerSymbol inputLayer) {
        this.inputLayer = inputLayer;
    }

    public Optional<LayerSymbol> getOutputLayer() {
        return Optional.ofNullable(outputLayer);
    }

    public void setOutputLayer(LayerSymbol outputLayer) {
        this.outputLayer = outputLayer;
    }

    //only call after resolve
    public List<ArchTypeSymbol> getOutputTypes() {
        if (outputTypes == null){
            outputTypes = computeOutputTypes();
        }
        return outputTypes;
    }

    protected void setOutputTypes(List<ArchTypeSymbol> outputTypes) {
        this.outputTypes = outputTypes;
    }

    public List<ArchTypeSymbol> getInputTypes() {
        if (getInputLayer().isPresent()){
            return getInputLayer().get().getOutputTypes();
        }
        else {
            return new ArrayList<>();
        }
    }

    public boolean isInput(){
        //override by IOLayerSymbol
        return false;
    }

    public boolean isOutput(){
        //override by IOLayerSymbol
        return false;
    }

    /**
     * only call after resolve():
     * @return returns the non-empty atomic layers which have the output of this layer as input.
     */
    public List<LayerSymbol> getNext(){
        if (getOutputLayer().isPresent()){
            List<LayerSymbol> outputLayers = new ArrayList<>();
            for (LayerSymbol layer : getOutputLayer().get().getFirstAtomicLayers()){
                if (layer.getMaxSerialLength().get() == 0){
                    outputLayers.addAll(layer.getNext());
                }
                else {
                    outputLayers.add(layer);
                }
            }
            return outputLayers;
        }
        else {
            return new ArrayList<>();
        }
    }

    /**
     * only call after resolve():
     * @return returns the non-empty atomic layers which are the input to this layer.
     */
    public List<LayerSymbol> getPrevious(){
        if (getInputLayer().isPresent()){
            List<LayerSymbol> inputLayers = new ArrayList<>();
            for (LayerSymbol layer : getInputLayer().get().getLastAtomicLayers()){
                if (layer.getMaxSerialLength().get() == 0){
                    inputLayers.addAll(layer.getPrevious());
                }
                else {
                    inputLayers.add(layer);
                }
            }
            return inputLayers;
        }
        else {
            return new ArrayList<>();
        }
    }

    public Set<VariableSymbol> getUnresolvableVariables() {
        if (unresolvableVariables == null){
            checkIfResolvable();
        }
        return unresolvableVariables;
    }

    protected void setUnresolvableVariables(Set<VariableSymbol> unresolvableVariables) {
        this.unresolvableVariables = unresolvableVariables;
    }

    public boolean isResolvable(){
        return getUnresolvableVariables().isEmpty();
    }

    public void checkIfResolvable(){
        checkIfResolvable(new HashSet<>());
    }

    protected void checkIfResolvable(Set<VariableSymbol> occurringVariables){
        Set<VariableSymbol> unresolvableVariables = new HashSet<>();
        computeUnresolvableVariables(unresolvableVariables, occurringVariables);
        setUnresolvableVariables(unresolvableVariables);
    }

    public void resolveOrError() throws ArchResolveException{
        Set<VariableSymbol> names = resolve();
        if (!isResolved()){
            throw new IllegalStateException("The following names could not be resolved: " + Joiners.COMMA.join(getUnresolvableVariables()));
        }
    }

    /**
     * resolves all expressions and underlying layers and handles method calls and sequences.
     * Architecture parameters have to be set before calling resolve.
     * Resolves prepares the layers such that the output type and shape of each layer can be calculated and checked.
     * CNNArchPreResolveCocos have to be checked before calling resolve and CNNArchPostResolvesCocos have to be checked after calling resolve.
     * @return returns the set of all variables which could not be resolved. Should be ignored.
     * @throws ArchResolveException thrown to interrupt the recursive resolve process to avoid follow-up Runtime Exceptions in tests after an error was logged.
     *                              Can be caught and ignored.
     */
    abstract public Set<VariableSymbol> resolve() throws ArchResolveException;

    //only call after resolve
    protected abstract List<ArchTypeSymbol> computeOutputTypes();

    abstract protected void computeUnresolvableVariables(Set<VariableSymbol> unresolvableVariables, Set<VariableSymbol> allVariables);

    abstract public boolean isResolved();

    abstract public Optional<Integer> getParallelLength();

    abstract public Optional<List<Integer>> getSerialLengths();

    public Optional<Integer> getMaxSerialLength() {
        Optional<List<Integer>> optLengths = getSerialLengths();
        if (optLengths.isPresent()){
            int max = 0;
            for (int length : optLengths.get()){
                if (length > max){
                    max = length;
                }
            }
            return Optional.of(max);
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * deepCopy for LayerSymbols, ArgumentSymbol and ArchExpressionSymbols but does not copy math expressions or scope and ast information.
     * @return returns a copy of this object
     */
    abstract public LayerSymbol copy();

    abstract protected void putInScope(MutableScope scope);

    abstract protected void resolveExpressions() throws ArchResolveException;

    /**
     * only call after resolve.
     * @return returns the first atomic layers which are contained in this layer or itself if it is atomic.
     */
    abstract public List<LayerSymbol> getFirstAtomicLayers();

    /**
     * only call after resolve.
     * @return returns the last atomic layers which are contained in this layer or itself if it is atomic.
     */
    abstract public List<LayerSymbol> getLastAtomicLayers();

    /**
     * A layer is called atomic if it is either a active predefined method, an input, an output or an empty composite.
     * This method only works correctly after a successful resolve().
     * @return returns true iff this layer is atomic and resolved.
     */
    abstract public boolean isAtomic();

    //only call after resolve; used in coco CheckLayerInputs to check the input type and shape of each layer.
    abstract public void checkInput();

}
