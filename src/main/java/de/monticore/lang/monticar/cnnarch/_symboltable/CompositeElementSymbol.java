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
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

import java.util.*;

public class CompositeElementSymbol extends ArchitectureElementSymbol {

    private boolean parallel;
    private List<ArchitectureElementSymbol> element;

    protected CompositeElementSymbol() {
        super("");
        setResolvedThis(this);
    }

    public boolean isParallel() {
        return parallel;
    }

    protected void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    public List<ArchitectureElementSymbol> getElement() {
        return element;
    }

    protected void setElement(List<ArchitectureElementSymbol> element) {
        ArchitectureElementSymbol previous = null;
        for (ArchitectureElementSymbol current : element){
            if (previous != null && !isParallel()){
                current.setInputElement(previous);
                previous.setOutputElement(current);
            }
            else {
                if (getInputElement().isPresent()){
                    current.setInputElement(getInputElement().get());
                }
                if (getOutputElement().isPresent()){
                    current.setOutputElement(getOutputElement().get());
                }
            }
            previous = current;
        }
        this.element = element;
    }

    @Override
    public boolean isAtomic() {
        return getElement().isEmpty();
    }

    @Override
    public void setInputElement(ArchitectureElementSymbol inputElement) {
        super.setInputElement(inputElement);
        if (isParallel()){
            for (ArchitectureElementSymbol current : getElement()){
                current.setInputElement(inputElement);
            }
        }
        else {
            if (!getElement().isEmpty()){
                getElement().get(0).setInputElement(inputElement);
            }
        }
    }

    @Override
    public void setOutputElement(ArchitectureElementSymbol outputElement) {
        super.setOutputElement(outputElement);
        if (isParallel()){
            for (ArchitectureElementSymbol current : getElement()){
                current.setOutputElement(outputElement);
            }
        }
        else {
            if (!getElement().isEmpty()){
                getElement().get(getElement().size()-1).setOutputElement(outputElement);
            }
        }
    }

    @Override
    public List<ArchitectureElementSymbol> getFirstAtomicElements() {
        if (getElement().isEmpty()){
            return Collections.singletonList(this);
        }
        else if (isParallel()){
            List<ArchitectureElementSymbol> firstElements = new ArrayList<>();
            for (ArchitectureElementSymbol element : getElement()){
                firstElements.addAll(element.getFirstAtomicElements());
            }
            return firstElements;
        }
        else {
            return getElement().get(0).getFirstAtomicElements();
        }
    }

    @Override
    public List<ArchitectureElementSymbol> getLastAtomicElements() {
        if (getElement().isEmpty()){
            return Collections.singletonList(this);
        }
        else if (isParallel()){
            List<ArchitectureElementSymbol> lastElements = new ArrayList<>();
            for (ArchitectureElementSymbol element : getElement()){
                lastElements.addAll(element.getLastAtomicElements());
            }
            return lastElements;
        }
        else {
            return getElement().get(getElement().size()-1).getLastAtomicElements();
        }
    }

    @Override
    public Set<VariableSymbol> resolve() throws ArchResolveException {
        if (!isResolved()) {
            if (isResolvable()) {
                List<ArchitectureElementSymbol> resolvedElements = new ArrayList<>();
                for (ArchitectureElementSymbol element : getElement()) {
                    element.resolve();
                }
            }
        }
        return getUnresolvableVariables();
    }

    @Override
    protected void resolveExpressions() throws ArchResolveException {
        for (ArchitectureElementSymbol element : getElement()){
            element.resolveExpressions();
        }
    }

    @Override
    public boolean isResolved() {
        boolean isResolved = true;
        for (ArchitectureElementSymbol element : getElement()){
            if (!element.isResolved()){
                isResolved = false;
            }
        }
        return isResolved;
    }

    @Override
    protected void computeUnresolvableVariables(Set<VariableSymbol> unresolvableVariables, Set<VariableSymbol> allVariables) {
        for (ArchitectureElementSymbol element : getElement()){
            element.checkIfResolvable(allVariables);
            unresolvableVariables.addAll(element.getUnresolvableVariables());
        }
    }

    @Override
    public List<ArchTypeSymbol> computeOutputTypes() {
        if (getElement().isEmpty()){
            if (getInputElement().isPresent()){
                return getInputElement().get().getOutputTypes();
            }
            else {
                return Collections.emptyList();
            }
        }
        else {
            if (isParallel()){
                List<ArchTypeSymbol> outputShapes = new ArrayList<>(getElement().size());
                for (ArchitectureElementSymbol element : getElement()){
                    if (element.getOutputTypes().size() != 0){
                        outputShapes.add(element.getOutputTypes().get(0));
                    }
                }
                return outputShapes;
            }
            else {
                for (ArchitectureElementSymbol element : getElement()){
                    element.getOutputTypes();
                }
                return getElement().get(getElement().size() - 1).getOutputTypes();
            }
        }
    }

    @Override
    public void checkInput() {
        if (!getElement().isEmpty()){
            for (ArchitectureElementSymbol element : getElement()){
                element.checkInput();
            }
            if (isParallel()){
                for (ArchitectureElementSymbol element : getElement()){
                    if (element.getOutputTypes().size() > 1){
                        Log.error("0" + ErrorCodes.MISSING_MERGE + " Missing merge layer (Add(), Concatenate() or [i]). " +
                                        "Each stream at the end of a parallelization block can only have one output stream. "
                                , getSourcePosition());
                    }
                }
            }
        }
    }

    @Override
    public Optional<Integer> getParallelLength() {
        if (isParallel()){
            return Optional.of(getElement().size());
        }
        else {
            return Optional.of(1);
        }
    }

    @Override
    public Optional<List<Integer>> getSerialLengths() {
        if (isParallel()){
            return Optional.of(Collections.nCopies(getElement().size(), 1));
        }
        else {
            return Optional.of(Collections.singletonList(getElement().size()));
        }
    }

    @Override
    protected void putInScope(Scope scope) {
        Collection<Symbol> symbolsInScope = scope.getLocalSymbols().get(getName());
        if (symbolsInScope == null || !symbolsInScope.contains(this)) {
            scope.getAsMutableScope().add(this);
            for (ArchitectureElementSymbol element : getElement()) {
                element.putInScope(getSpannedScope());
            }
        }
    }

    @Override
    protected CompositeElementSymbol preResolveDeepCopy() {
        CompositeElementSymbol copy = new CompositeElementSymbol();
        copy.setParallel(isParallel());
        if (getAstNode().isPresent()){
            copy.setAstNode(getAstNode().get());
        }

        List<ArchitectureElementSymbol> elements = new ArrayList<>(getElement().size());
        for (ArchitectureElementSymbol element : getElement()){
            ArchitectureElementSymbol elementCopy = element.preResolveDeepCopy();
            elements.add(elementCopy);
        }
        copy.setElement(elements);
        return copy;
    }

    public static class Builder{
        private boolean parallel = false;
        private List<ArchitectureElementSymbol> elements = new ArrayList<>();

        public Builder parallel(boolean parallel){
            this.parallel = parallel;
            return this;
        }

        public Builder elements(List<ArchitectureElementSymbol> elements){
            this.elements = elements;
            return this;
        }

        public Builder elements(ArchitectureElementSymbol... elements){
            this.elements = Arrays.asList(elements);
            return this;
        }

        public CompositeElementSymbol build(){
            CompositeElementSymbol sym = new CompositeElementSymbol();
            sym.setParallel(parallel);
            sym.setElement(elements);
            return sym;
        }
    }
}
