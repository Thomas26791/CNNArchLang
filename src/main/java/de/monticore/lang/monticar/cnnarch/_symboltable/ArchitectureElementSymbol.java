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
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.Joiners;

import java.util.*;

public abstract class ArchitectureElementSymbol extends CommonScopeSpanningSymbol {

    public static final ArchitectureElementKind KIND = new ArchitectureElementKind();

    private ArchitectureElementSymbol inputElement;
    private ArchitectureElementSymbol outputElement;
    private List<ArchTypeSymbol> outputTypes = null;
    private Set<VariableSymbol> unresolvableVariables = null;
    private ArchitectureElementSymbol resolvedThis = null;

    protected ArchitectureElementSymbol(String name) {
        super(name, KIND);
    }

    @Override
    protected ArchitectureElementScope createSpannedScope() {
        return new ArchitectureElementScope();
    }

    @Override
    public ArchitectureElementScope getSpannedScope() {
        return (ArchitectureElementScope) super.getSpannedScope();
    }

    public Optional<ArchitectureElementSymbol> getInputElement() {
        return Optional.ofNullable(inputElement);
    }

    public void setInputElement(ArchitectureElementSymbol inputElement) {
        this.inputElement = inputElement;
    }

    public Optional<ArchitectureElementSymbol> getOutputElement() {
        return Optional.ofNullable(outputElement);
    }

    public void setOutputElement(ArchitectureElementSymbol outputElement) {
        this.outputElement = outputElement;
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
        if (getInputElement().isPresent()){
            return getInputElement().get().getOutputTypes();
        }
        else {
            return new ArrayList<>();
        }
    }

    public boolean isInput(){
        //override by IOSymbol
        return false;
    }

    public boolean isOutput(){
        //override by IOSymbol
        return false;
    }

    public ArchitectureSymbol getArchitecture(){
        Symbol sym = getEnclosingScope().getSpanningSymbol().get();
        if (sym instanceof ArchitectureSymbol){
            return (ArchitectureSymbol) sym;
        }
        else {
            return ((ArchitectureElementSymbol) sym).getArchitecture();
        }
    }

    /**
     * only call after resolve():
     * @return returns the non-empty atomic element which have the output of this element as input.
     */
    public List<ArchitectureElementSymbol> getNext(){
        if (getOutputElement().isPresent()){
            List<ArchitectureElementSymbol> outputElements = new ArrayList<>();
            for (ArchitectureElementSymbol element : getOutputElement().get().getFirstAtomicElements()){
                if (element.getMaxSerialLength().get() == 0){
                    outputElements.addAll(element.getNext());
                }
                else {
                    outputElements.add(element);
                }
            }
            return outputElements;
        }
        else {
            return new ArrayList<>();
        }
    }

    /**
     * only call after resolve():
     * @return returns the non-empty atomic elements which are the input to this element.
     */
    public List<ArchitectureElementSymbol> getPrevious(){
        if (getInputElement().isPresent()){
            List<ArchitectureElementSymbol> inputElements = new ArrayList<>();
            for (ArchitectureElementSymbol element : getInputElement().get().getLastAtomicElements()){
                if (element.getMaxSerialLength().get() == 0){
                    inputElements.addAll(element.getPrevious());
                }
                else {
                    inputElements.add(element);
                }
            }
            return inputElements;
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

    public Optional<ArchitectureElementSymbol> getResolvedThis() {
        return Optional.ofNullable(resolvedThis);
    }

    protected void setResolvedThis(ArchitectureElementSymbol resolvedThis) {
        if (resolvedThis != null && resolvedThis != this){
            resolvedThis.putInScope(getSpannedScope());
            if (getInputElement().isPresent()){
                resolvedThis.setInputElement(getInputElement().get());
            }
            if (getOutputElement().isPresent()){
                resolvedThis.setOutputElement(getOutputElement().get());
            }
        }
        this.resolvedThis = resolvedThis;
    }

    public void resolveOrError() throws ArchResolveException{
        Set<VariableSymbol> names = resolve();
        if (!isResolved()){
            throw new IllegalStateException("The following names could not be resolved: " + Joiners.COMMA.join(getUnresolvableVariables()));
        }
    }

    public boolean isResolved(){
        if (getResolvedThis().isPresent() && getResolvedThis().get() != this){
            return getResolvedThis().get().isResolved();
        }
        else {
            return getResolvedThis().isPresent();
        }
    }

    /**
     * resolves all expressions and underlying architecture elements and handles layer method calls and sequences.
     * Architecture parameters have to be set before calling resolve.
     * Resolves prepares the architecture elements such that the output type and shape of each element can be calculated and checked.
     * @return returns the set of all variables which could not be resolved. Should be ignored.
     * @throws ArchResolveException thrown to interrupt the recursive resolve process to avoid follow-up Runtime Exceptions in tests after an error was logged.
     *                              Can be caught and ignored.
     */
    abstract public Set<VariableSymbol> resolve() throws ArchResolveException;

    //only call after resolve
    protected abstract List<ArchTypeSymbol> computeOutputTypes();

    abstract protected void computeUnresolvableVariables(Set<VariableSymbol> unresolvableVariables, Set<VariableSymbol> allVariables);

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

    abstract protected void putInScope(Scope scope);

    abstract protected void resolveExpressions() throws ArchResolveException;

    /**
     * only call after resolve.
     * @return returns the first atomic elements which are contained in this element or itself if it is atomic.
     */
    abstract public List<ArchitectureElementSymbol> getFirstAtomicElements();

    /**
     * only call after resolve.
     * @return returns the last atomic elements which are contained in this element or itself if it is atomic.
     */
    abstract public List<ArchitectureElementSymbol> getLastAtomicElements();

    /**
     * A element is called atomic if it is either a active predefined layer, an single input, an single output or an empty composite.
     * This method only works correctly after a successful resolve().
     * @return returns true iff this element is atomic and resolved.
     */
    abstract public boolean isAtomic();

    //only call after resolve; used in coco CheckElementInputs to check the input type and shape of each element.
    abstract public void checkInput();

    /**
     * Creates a deep copy in the state before the architecture resolution.
     * @return returns a deep copy of this object in the pre-resolve version.
     */
    protected abstract ArchitectureElementSymbol preResolveDeepCopy();
}
