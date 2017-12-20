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
import de.monticore.symboltable.CommonSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class LayerSymbol extends CommonScopeSpanningSymbol {

    public static final LayerKind KIND = new LayerKind();

    private List<LayerSymbol> inputLayers = new ArrayList<>(4);
    private List<LayerSymbol> outputLayers = new ArrayList<>(4);
    private ShapeSymbol outputShape;
    private boolean fullyResolved;

    public LayerSymbol(String name) {
        super(name, KIND);
    }

    public List<LayerSymbol> getInputLayers() {
        return inputLayers;
    }

    public void setInputLayers(List<LayerSymbol> inputLayers) {
        this.inputLayers = inputLayers;
    }

    public List<LayerSymbol> getOutputLayers() {
        return outputLayers;
    }

    public void setOutputLayers(List<LayerSymbol> outputLayers) {
        this.outputLayers = outputLayers;
    }

    public ShapeSymbol getOutputShape() {
        return outputShape;
    }

    public void setOutputShape(ShapeSymbol outputShape) {
        this.outputShape = outputShape;
    }

    public boolean isFullyResolved() {
        return fullyResolved;
    }

    public void setFullyResolved(boolean fullyResolved) {
        this.fullyResolved = fullyResolved;
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

    abstract public Set<String> resolve();

    abstract protected void checkIfResolved();
}
