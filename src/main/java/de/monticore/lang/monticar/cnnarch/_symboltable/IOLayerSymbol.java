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
    public Set<String> resolve() {
        return null;
    }

    @Override
    public boolean isResolved() {
        boolean isResolved = true;
        if (getArrayAccess().isPresent()){
            if (!getArrayAccess().get().isFullyResolved()){
                isResolved = false;
            }
        }
        //todo getShape().isResolved
        if (!getDefinition().getShape().computeUnresolvableNames().isEmpty()){
            isResolved = false;
        }
        return isResolved;
    }

    @Override
    protected Set<String> computeUnresolvableNames() {
        HashSet<String> unresolvableNames = new HashSet<>();
        if (getArrayAccess().isPresent()){
            unresolvableNames.addAll(getArrayAccess().get().computeUnresolvableNames());
        }
        unresolvableNames.addAll(getDefinition().getShape().computeUnresolvableNames());
        return unresolvableNames;
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
    public int getParallelLength() {
        return getDefinition().getArrayLength();
    }

    @Override
    public int getSerialLength() {
        return 1;
    }

    @Override
    protected void putInScope(MutableScope scope) {
        if (!scope.getLocalSymbols().get(getName()).contains(this)) {
            scope.add(this);
        }
        //todo: probably not complete
    }

    @Override
    public LayerSymbol copy() {
        return new Builder()
                .name(getName())
                .arrayAccess(arrayAccess)
                .build();
    }

    @Override
    protected void resolveExpressions() {
        if (getArrayAccess().isPresent()){
            getArrayAccess().get().resolve();
        }
    }

    public static class Builder{
        private ArchSimpleExpressionSymbol arrayAccess = null;
        private String name;

        public Builder arrayAccess(ArchSimpleExpressionSymbol arrayAccess){
            this.arrayAccess = arrayAccess;
            return this;
        }

        public Builder arrayAccess(int arrayAccess){
            this.arrayAccess = ArchSimpleExpressionSymbol.of(arrayAccess);
            return this;
        }

        public Builder name(String name){
            this.name = name;
            return this;
        }

        public IOLayerSymbol build(){
            if (name == null || name.equals("")){
                throw new IllegalStateException("Missing or empty name for IOLayerSymbol");
            }
            IOLayerSymbol sym = new IOLayerSymbol(name);
            sym.setArrayAccess(arrayAccess);
            return sym;
        }
    }
}
