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
import de.se_rwth.commons.logging.Log;
import jline.internal.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class LayerSymbol extends CommonScopeSpanningSymbol {

    public static final LayerKind KIND = new LayerKind();

    private LayerSymbol inputLayer;
    private List<ShapeSymbol> outputShapes = null;
    private Set<String> unresolvableNames = null;

    protected LayerSymbol(String name) {
        super(name, KIND);
    }

    public Optional<LayerSymbol> getInputLayer() {
        return Optional.ofNullable(inputLayer);
    }

    public void setInputLayer(LayerSymbol inputLayer) {
        this.inputLayer = inputLayer;
    }

    public List<ShapeSymbol> getOutputShapes() {
        if (outputShapes == null){
            outputShapes = computeOutputShapes();
        }
        return outputShapes;
    }

    protected void setOutputShapes(List<ShapeSymbol> outputShapes) {
        this.outputShapes = outputShapes;
    }

    public boolean isInput(){
        return false;
    }

    public boolean isOutput(){
        return false;
    }

    public boolean isCompositeLayer(){
        return false;
    }

    public boolean isMethod(){
        return false;
    }

    public Set<String> getUnresolvableNames() {
        if (unresolvableNames == null){
            checkIfResolvable();
        }
        return unresolvableNames;
    }

    public boolean isResolvable(){
        return getUnresolvableNames().isEmpty();
    }

    public void checkIfResolvable(){
        unresolvableNames = computeUnresolvableNames();
    }

    public void resolveOrError(){
        resolve();
        if (isResolved()){
            throw new IllegalStateException("The following names could not be resolved: " + getUnresolvableNames());
        }
    }

    abstract public Set<String> resolve();

    abstract protected List<ShapeSymbol> computeOutputShapes();

    abstract protected Set<String> computeUnresolvableNames();

    abstract public boolean isResolved();

    abstract public int getParallelLength();

    abstract public int getSerialLength();

    abstract protected void putInScope(MutableScope scope);

    //deepCopy for LayerSymbols and ArgumentSymbol but does not copy expressions
    abstract public LayerSymbol copy();

    abstract protected void resolveExpressions();
}
