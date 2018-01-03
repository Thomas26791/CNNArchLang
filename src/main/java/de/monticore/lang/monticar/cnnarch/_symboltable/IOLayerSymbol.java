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

import de.monticore.lang.monticar.cnnarch.ErrorMessages;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

import java.util.*;

public class IOLayerSymbol extends LayerSymbol {

    private ArchSimpleExpressionSymbol arrayAccess = null;
    private IODeclarationSymbol definition;

    protected IOLayerSymbol(String name) {
        super(name);
    }

    public Optional<ArchSimpleExpressionSymbol> getArrayAccess() {
        return Optional.ofNullable(arrayAccess);
    }

    protected void setArrayAccess(ArchSimpleExpressionSymbol arrayAccess) {
        this.arrayAccess = arrayAccess;
    }

    protected void setArrayAccess(int arrayAccess) {
        this.arrayAccess = ArchSimpleExpressionSymbol.of(arrayAccess);
    }

    public IODeclarationSymbol getDefinition() {
        if (definition == null){
            Optional<IODeclarationSymbol> optDef = getEnclosingScope().resolve(getName(), IODeclarationSymbol.KIND);
            if (optDef.isPresent()){
                setDefinition(optDef.get());
            }
            else {
                Log.error(ErrorMessages.UNKNOWN_NAME_MSG + "IOVariable with name " + getName() + " could not be resolved", getSourcePosition());
            }
        }
        return definition;
    }

    private void setDefinition(IODeclarationSymbol definition) {
        this.definition = definition;
    }

    @Override
    public boolean isInput(){
        return getDefinition().isInput();
    }

    @Override
    public boolean isOutput(){
        return getDefinition().isOutput();
    }

    /*@Override
    public void reset() {
        setUnresolvableVariables(null);
        if (getArrayAccess().isPresent()){
            getArrayAccess().get().reset();
        }
    }*/

    @Override
    public Set<VariableSymbol> resolve() {
        if (!isResolved()) {
            if (isResolvable()) {
                resolveExpressions();
                getDefinition().getShape().resolve();
            }
        }
        return getUnresolvableVariables();
    }

    @Override
    public boolean isResolved() {
        boolean isResolved = true;
        if (getArrayAccess().isPresent()){
            if (!getArrayAccess().get().isResolved()){
                isResolved = false;
            }
        }
        //todo getShape().isResolved
        if (!getDefinition().getShape().isResolved()){
            isResolved = false;
        }
        return isResolved;
    }

    @Override
    protected void computeUnresolvableVariables(Set<VariableSymbol> unresolvableVariables, Set<VariableSymbol> allVariables) {
        if (getArrayAccess().isPresent()){
            getArrayAccess().get().checkIfResolvable(allVariables);
            unresolvableVariables.addAll(getArrayAccess().get().getUnresolvableVariables());
        }
        getDefinition().getShape().checkIfResolvable(allVariables);
        unresolvableVariables.addAll(getDefinition().getShape().getUnresolvableVariables());
    }

    @Override
    protected List<ShapeSymbol> computeOutputShapes() {
        List<ShapeSymbol> outputShapes;
        if (isInput()){
            outputShapes = new ArrayList<>(getDefinition().getArrayLength());
            for (int i = 0; i < getDefinition().getArrayLength(); i++){
                outputShapes.add(getDefinition().getShape());
            }
        }
        else {
            outputShapes = Collections.emptyList();
        }
        return outputShapes;
    }

    @Override
    public Optional<Integer> getParallelLength() {
        return Optional.of(getDefinition().getArrayLength());
    }

    @Override
    public Optional<List<Integer>> getSerialLengths() {
        return Optional.of(Collections.nCopies(getParallelLength().get(), 1));
    }



    @Override
    protected void putInScope(MutableScope scope) {
        Collection<Symbol> symbolsInScope = scope.getLocalSymbols().get(getName());
        if (symbolsInScope == null || !symbolsInScope.contains(this)) {
            scope.add(this);
            if (getArrayAccess().isPresent()){
                getArrayAccess().get().putInScope(getSpannedScope());
            }
        }
    }

    @Override
    public LayerSymbol copy() {
        ArchSimpleExpressionSymbol arrayAccessCopy = null;
        if (getArrayAccess().isPresent()){
            arrayAccessCopy = getArrayAccess().get().copy();
        }
        IOLayerSymbol copy = new Builder()
                .definition(getDefinition())
                .arrayAccess(arrayAccessCopy)
                .build();
        return copy;
    }

    @Override
    protected void resolveExpressions() {
        if (getArrayAccess().isPresent()){
            getArrayAccess().get().resolveOrError();
        }
    }

    public static class Builder{
        private ArchSimpleExpressionSymbol arrayAccess = null;
        private IODeclarationSymbol definition;

        public Builder arrayAccess(ArchSimpleExpressionSymbol arrayAccess){
            this.arrayAccess = arrayAccess;
            return this;
        }

        public Builder arrayAccess(int arrayAccess){
            this.arrayAccess = ArchSimpleExpressionSymbol.of(arrayAccess);
            return this;
        }

        public Builder definition(IODeclarationSymbol definition){
            this.definition = definition;
            return this;
        }

        public IOLayerSymbol build(){
            if (definition == null){
                throw new IllegalStateException("Missing or definition for IOLayerSymbol");
            }
            IOLayerSymbol sym = new IOLayerSymbol(definition.getName());
            sym.setArrayAccess(arrayAccess);
            return sym;
        }
    }
}
