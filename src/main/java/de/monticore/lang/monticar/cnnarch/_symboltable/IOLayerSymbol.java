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
            Collection<IODeclarationSymbol> ioDefCollection = getEnclosingScope().resolveMany(getName(), IODeclarationSymbol.KIND);
            if (!ioDefCollection.isEmpty()){
                setDefinition(ioDefCollection.iterator().next());
            }
        }
        return definition;
    }

    @Override
    public boolean isResolvable() {
        return super.isResolvable() && getDefinition() != null;
    }

    private void setDefinition(IODeclarationSymbol definition) {
        this.definition = definition;
        definition.getConnectedLayers().add(this);
    }

    @Override
    public boolean isInput(){
        return getDefinition().isInput();
    }

    @Override
    public boolean isOutput(){
        return getDefinition().isOutput();
    }

    @Override
    public boolean isAtomic(){
        return true;
    }

    @Override
    public List<LayerSymbol> getFirstAtomicLayers() {
        return Collections.singletonList(this);
    }

    @Override
    public List<LayerSymbol> getLastAtomicLayers() {
        return Collections.singletonList(this);
    }

    @Override
    public Set<VariableSymbol> resolve() throws ArchResolveException {
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
    public List<ShapeSymbol> computeOutputShapes() {
        List<ShapeSymbol> outputShapes;
        if (isInput()){
            outputShapes = Collections.singletonList(getDefinition().getShape());
        }
        else {
            outputShapes = Collections.emptyList();
        }
        return outputShapes;
    }

    @Override
    public void checkInputAndOutput() {
        if (isOutput()){
            List<ShapeSymbol> inputShapes = getInputShapes();
            if (inputShapes.isEmpty()){
                Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT + " Invalid number of input streams. " +
                                "The number of input streams to the output " + getName() + " is 0."
                        , getSourcePosition());
            }
            else{
                int size = getDefinition().getArrayLength();
                if (getArrayAccess().isPresent()){
                    size = 1;
                }

                if (inputShapes.size() != size){
                    Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT + " Invalid number of input streams. " +
                                    "The output " + getName() + " is an array of size " + size +
                                    " but the number of input streams is " + inputShapes.size() + "."
                            , getSourcePosition());
                }
            }
            //todo check type
        }
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
        if (getAstNode().isPresent()){
            copy.setAstNode(getAstNode().get());
        }
        return copy;
    }

    @Override
    protected void resolveExpressions() throws ArchResolveException {
        if (getArrayAccess().isPresent()){
            getArrayAccess().get().resolveOrError();
            boolean valid = true;
            valid = valid && Constraints.INTEGER.check(getArrayAccess().get(), getSourcePosition(), getName());
            valid = valid && Constraints.NON_NEGATIVE.check(getArrayAccess().get(), getSourcePosition(), getName());
            if (!valid){
                throw new ArchResolveException();
            }
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
