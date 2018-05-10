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
import de.monticore.lang.monticar.cnnarch.helper.Utils;
import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedLayers;
import de.monticore.lang.monticar.types2._ast.ASTElementType;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

import java.util.*;

public class IOSymbol extends ArchitectureElementSymbol {

    private ArchSimpleExpressionSymbol arrayAccess = null;
    private IODeclarationSymbol definition;

    protected IOSymbol(String name) {
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
        this.arrayAccess.putInScope(getSpannedScope());
    }

    //returns null if IODeclaration does not exist. This is checked in coco CheckIOName.
    public IODeclarationSymbol getDefinition() {
        if (definition == null){
            this.definition = getArchitecture().resolveIODeclaration(getName());
        }
        return definition;
    }

    @Override
    public boolean isResolvable() {
        return super.isResolvable() && getDefinition() != null;
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
        return getResolvedThis().isPresent() && getResolvedThis().get() == this;
    }

    @Override
    public List<ArchitectureElementSymbol> getFirstAtomicElements() {
        if (getResolvedThis().isPresent() && getResolvedThis().get() != this){
            return getResolvedThis().get().getFirstAtomicElements();
        }
        else {
            return Collections.singletonList(this);
        }
    }

    @Override
    public List<ArchitectureElementSymbol> getLastAtomicElements() {
        if (getResolvedThis().isPresent() && getResolvedThis().get() != this){
            return getResolvedThis().get().getLastAtomicElements();
        }
        else {
            return Collections.singletonList(this);
        }
    }

    @Override
    public Set<VariableSymbol> resolve() throws ArchResolveException {
        if (!isResolved()) {
            if (isResolvable()) {
                resolveExpressions();
                getDefinition().getType().resolve();

                if (!getArrayAccess().isPresent() && getDefinition().getArrayLength() > 1){
                    //transform io array into parallel composite
                    List<ArchitectureElementSymbol> parallelElements = createExpandedParallelElements();
                    CompositeElementSymbol composite = new CompositeElementSymbol.Builder()
                            .parallel(true)
                            .elements(parallelElements)
                            .build();

                    getSpannedScope().getAsMutableScope().add(composite);
                    composite.setAstNode(getAstNode().get());
                    for (ArchitectureElementSymbol element : parallelElements){
                        element.putInScope(composite.getSpannedScope());
                        element.setAstNode(getAstNode().get());
                    }
                    if (getInputElement().isPresent()){
                        composite.setInputElement(getInputElement().get());
                    }
                    if (getOutputElement().isPresent()){
                        composite.setOutputElement(getOutputElement().get());
                    }
                    composite.resolveOrError();
                    setResolvedThis(composite);
                }
                else {
                    //Add port to the ports stored in ArchitectureSymbol
                    if (getDefinition().isInput()){
                        getArchitecture().getInputs().add(this);
                    }
                    else {
                        getArchitecture().getOutputs().add(this);
                    }
                    setResolvedThis(this);
                }

            }
        }
        return getUnresolvableVariables();
    }

    private List<ArchitectureElementSymbol> createExpandedParallelElements() throws ArchResolveException{
        List<ArchitectureElementSymbol> parallelElements = new ArrayList<>(getDefinition().getArrayLength());
        if (getDefinition().isInput()){
            for (int i = 0; i < getDefinition().getArrayLength(); i++){
                IOSymbol ioElement = new IOSymbol(getName());
                ioElement.setArrayAccess(i);
                parallelElements.add(ioElement);
            }
        }
        else {
            for (int i = 0; i < getDefinition().getArrayLength(); i++){
                CompositeElementSymbol serialComposite = new CompositeElementSymbol();
                serialComposite.setParallel(false);

                IOSymbol ioElement = new IOSymbol(getName());
                ioElement.setArrayAccess(i);
                ioElement.setAstNode(getAstNode().get());

                LayerSymbol getLayer = new LayerSymbol(AllPredefinedLayers.GET_NAME);
                getLayer.setArguments(Collections.singletonList(
                        new ArgumentSymbol.Builder()
                                .parameter(AllPredefinedLayers.INDEX_NAME)
                                .value(ArchSimpleExpressionSymbol.of(i))
                                .build()));
                getLayer.setAstNode(getAstNode().get());

                serialComposite.setElement(Arrays.asList(getLayer, ioElement));

                parallelElements.add(serialComposite);
            }
        }
        return parallelElements;
    }

    @Override
    protected void computeUnresolvableVariables(Set<VariableSymbol> unresolvableVariables, Set<VariableSymbol> allVariables) {
        if (getArrayAccess().isPresent()){
            getArrayAccess().get().checkIfResolvable(allVariables);
            unresolvableVariables.addAll(getArrayAccess().get().getUnresolvableVariables());
        }
        getDefinition().getType().checkIfResolvable(allVariables);
        unresolvableVariables.addAll(getDefinition().getType().getUnresolvableVariables());
    }

    @Override
    public List<ArchTypeSymbol> computeOutputTypes() {
        List<ArchTypeSymbol> outputShapes;
        if (isAtomic()){
            if (isInput()){
                outputShapes = Collections.singletonList(getDefinition().getType());
            }
            else {
                outputShapes = Collections.emptyList();
            }
        }
        else {
            if (!getResolvedThis().isPresent()){
                throw new IllegalStateException("The architecture resolve() method was never called");
            }
            outputShapes = getResolvedThis().get().computeOutputTypes();
        }
        return outputShapes;
    }

    @Override
    public void checkInput() {
        if (isAtomic()) {
            if (isOutput()) {
                String name = getName();
                if (getArrayAccess().isPresent()) {
                    name = name + "[" + getArrayAccess().get().getIntValue().get() + "]";
                }

                if (getInputTypes().size() != 1) {
                    Log.error("0" + ErrorCodes.INVALID_ELEMENT_INPUT_SHAPE + " Invalid number of input streams. " +
                                    "The number of input streams for the output '" + name + "' is " + getInputTypes().size() + "."
                            , getSourcePosition());
                } else {
                    ASTElementType inputType = getInputTypes().get(0).getDomain();
                    if (!Utils.equals(inputType, getDefinition().getType().getDomain())) {
                        Log.error("0" + ErrorCodes.INVALID_ELEMENT_INPUT_DOMAIN + " " +
                                "The declared output type of '" + name + "' does not match with the actual type. " +
                                "Declared type: " + getDefinition().getType().getDomain().getTElementType().get() + ". " +
                                "Actual type: " + inputType.getTElementType().get() + ".");
                    }
                }

            }
        }
        else {
            if (!getResolvedThis().isPresent()){
                throw new IllegalStateException("The architecture resolve() method was never called");
            }
            getResolvedThis().get().checkInput();
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
    protected void putInScope(Scope scope) {
        Collection<Symbol> symbolsInScope = scope.getLocalSymbols().get(getName());
        if (symbolsInScope == null || !symbolsInScope.contains(this)) {
            scope.getAsMutableScope().add(this);
            if (getArrayAccess().isPresent()){
                getArrayAccess().get().putInScope(getSpannedScope());
            }
        }
    }

    @Override
    protected void resolveExpressions() throws ArchResolveException {
        if (getArrayAccess().isPresent()){
            getArrayAccess().get().resolveOrError();
            boolean valid;
            valid = Constraints.INTEGER.check(getArrayAccess().get(), getSourcePosition(), getName());
            valid = valid && Constraints.NON_NEGATIVE.check(getArrayAccess().get(), getSourcePosition(), getName());
            if (!valid){
                throw new ArchResolveException();
            }
        }
    }

    @Override
    protected ArchitectureElementSymbol preResolveDeepCopy() {
        IOSymbol copy = new IOSymbol(getName());
        if (getAstNode().isPresent()){
            copy.setAstNode(getAstNode().get());
        }

        if (getArrayAccess().isPresent()) {
            copy.setArrayAccess(getArrayAccess().get().preResolveDeepCopy());
        }

        return copy;
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

        public IOSymbol build(){
            if (definition == null){
                throw new IllegalStateException("Missing declaration for IOSymbol");
            }
            IOSymbol sym = new IOSymbol(definition.getName());
            sym.setArrayAccess(arrayAccess);
            return sym;
        }
    }
}
