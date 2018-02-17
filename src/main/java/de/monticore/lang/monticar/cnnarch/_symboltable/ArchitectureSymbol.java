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
/* generated by template symboltable.ScopeSpanningSymbol*/


package de.monticore.lang.monticar.cnnarch._symboltable;

import de.monticore.symboltable.CommonScopeSpanningSymbol;
import de.monticore.symboltable.Symbol;

import java.util.*;

public class ArchitectureSymbol extends CommonScopeSpanningSymbol {

    public static final ArchitectureKind KIND = new ArchitectureKind();

    private LayerSymbol body;
    private List<IOLayerSymbol> inputs = new ArrayList<>();
    private List<IOLayerSymbol> outputs = new ArrayList<>();

    public ArchitectureSymbol() {
        super("", KIND);
    }

    public LayerSymbol getBody() {
        return body;
    }

    protected void setBody(LayerSymbol body) {
        this.body = body;
    }

    public List<IOLayerSymbol> getInputs() {
        return inputs;
    }

    public List<IOLayerSymbol> getOutputs() {
        return outputs;
    }

    public Set<IODeclarationSymbol> getIODeclarations(){
        Set<IODeclarationSymbol> ioDeclarations = new HashSet<>();
        for (IOLayerSymbol input : getInputs()){
            ioDeclarations.add(input.getDefinition());
        }
        for (IOLayerSymbol output : getOutputs()){
            ioDeclarations.add(output.getDefinition());
        }
        return ioDeclarations;
    }

    public Collection<MethodDeclarationSymbol> getMethodDeclarations(){
        return getSpannedScope().resolveLocally(MethodDeclarationSymbol.KIND);
    }

    public void resolve(){
        getBody().checkIfResolvable();
        try{
            getBody().resolveOrError();
        }
        catch (ArchResolveException e){
            //do nothing; error is already logged
        }
    }

    public List<LayerSymbol> getFirstLayers(){
        if (!getBody().isResolved()){
            resolve();
        }
        return getBody().getFirstAtomicLayers();
    }

    public boolean isResolved(){
        return getBody().isResolved();
    }

    public boolean isResolvable(){
        return getBody().isResolvable();
    }
}
