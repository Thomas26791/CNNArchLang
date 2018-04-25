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

import de.monticore.lang.monticar.cnnarch.helper.Utils;
import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedMethods;
import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedVariables;
import de.monticore.symboltable.CommonScopeSpanningSymbol;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;

import javax.annotation.Nullable;
import java.util.*;

public class ArchitectureSymbol extends CommonScopeSpanningSymbol {

    public static final ArchitectureKind KIND = new ArchitectureKind();

    private LayerSymbol body;
    private List<IOLayerSymbol> inputs = new ArrayList<>();
    private List<IOLayerSymbol> outputs = new ArrayList<>();
    private Map<String, IODeclarationSymbol> ioDeclarationMap = new HashMap<>();
    private boolean isCopy = false;

    public ArchitectureSymbol() {
        super("", KIND);
    }

    public ArchitectureSymbol(String name) {
        super(name, KIND);
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

    public boolean isCopy() {
        return isCopy;
    }

    //called in IOLayer to get IODeclaration; only null if error; will be checked in coco CheckIOName
    @Nullable
    protected IODeclarationSymbol resolveIODeclaration(String name){
        IODeclarationSymbol ioDeclaration = ioDeclarationMap.get(name);
        if (ioDeclaration == null){
            Collection<IODeclarationSymbol> ioDefCollection = getEnclosingScope().resolveMany(name, IODeclarationSymbol.KIND);
            if (!ioDefCollection.isEmpty()){
                ioDeclaration = ioDefCollection.iterator().next();
                ioDeclarationMap.put(name, ioDeclaration);
                ioDeclaration.setArchitecture(this);
            }
        }
        return ioDeclaration;
    }

    public Collection<IODeclarationSymbol> getIODeclarations(){
        return ioDeclarationMap.values();
    }

    public Collection<MethodDeclarationSymbol> getMethodDeclarations(){
        return getSpannedScope().resolveLocally(MethodDeclarationSymbol.KIND);
    }

    //useful to resolve the architecture in a component instance
    public ArchitectureSymbol resolveInScope(Scope scope){
        if (isCopy()){
            getBody().checkIfResolvable();
            try{
                getBody().resolveOrError();
            }
            catch (ArchResolveException e){
                //do nothing; error is already logged
            }
            return this;
        }
        else {
            ArchitectureSymbol copy = preResolveDeepCopy();
            copy.putInScope(scope);
            copy.resolve();
            return copy;
        }
    }

    public ArchitectureSymbol resolve(){
        return resolveInScope(getEnclosingScope());
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

    public void putInScope(Scope scope){
        Collection<Symbol> symbolsInScope = scope.getLocalSymbols().get(getName());
        if (symbolsInScope == null || !symbolsInScope.contains(this)){
            scope.getAsMutableScope().add(this);
            getSpannedScope().getAsMutableScope().setResolvingFilters(scope.getResolvingFilters());
            Utils.recursiveSetResolvingFilters(getSpannedScope(), scope.getResolvingFilters());
        }
    }

    public ArchitectureSymbol preResolveDeepCopy(){
        ArchitectureSymbol copy = new ArchitectureSymbol("instance");
        copy.setBody(getBody().preResolveDeepCopy());
        if (getAstNode().isPresent()){
            copy.setAstNode(getAstNode().get());
        }
        copy.getSpannedScope().getAsMutableScope().add(AllPredefinedVariables.createTrueConstant());
        copy.getSpannedScope().getAsMutableScope().add(AllPredefinedVariables.createFalseConstant());
        for (MethodDeclarationSymbol methodDeclaration : AllPredefinedMethods.createList()){
            copy.getSpannedScope().getAsMutableScope().add(methodDeclaration);
        }
        for (MethodDeclarationSymbol methodDeclaration : getSpannedScope().<MethodDeclarationSymbol>resolveLocally(MethodDeclarationSymbol.KIND)){
            if (!methodDeclaration.isPredefined()) {
                copy.getSpannedScope().getAsMutableScope().add(methodDeclaration.deepCopy());
            }
        }

        copy.getBody().putInScope(copy.getSpannedScope());
        copy.isCopy = true;
        return copy;
    }
}
