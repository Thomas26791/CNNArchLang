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
import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedVariables;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

import java.util.*;
import java.util.function.Function;

public class LayerSymbol extends ArchitectureElementSymbol {

    private LayerDeclarationSymbol declaration = null;
    private List<ArgumentSymbol> arguments;

    protected LayerSymbol(String name) {
        super(name);
    }

    public LayerDeclarationSymbol getDeclaration() {
        if (declaration == null){
            Collection<LayerDeclarationSymbol> declarationCollection = getEnclosingScope().resolveMany(getName(), LayerDeclarationSymbol.KIND);
            if (!declarationCollection.isEmpty()){
                setDeclaration(declarationCollection.iterator().next());
            }
        }
        return declaration;
    }

    @Override
    public boolean isResolvable() {
        return super.isResolvable() && getDeclaration() != null;
    }

    private void setDeclaration(LayerDeclarationSymbol declaration) {
        this.declaration = declaration;
    }

    public List<ArgumentSymbol> getArguments() {
        return arguments;
    }

    protected void setArguments(List<ArgumentSymbol> arguments) {
        this.arguments = arguments;
    }

    public ArchExpressionSymbol getIfExpression(){
        Optional<ArgumentSymbol> argument = getArgument(AllPredefinedVariables.CONDITIONAL_ARG_NAME);
        if (argument.isPresent()){
            return argument.get().getRhs();
        }
        else {
            return ArchSimpleExpressionSymbol.of(true);
        }
    }

    @Override
    public void setInputElement(ArchitectureElementSymbol inputElement) {
        super.setInputElement(inputElement);
        if (getResolvedThis().isPresent() && getResolvedThis().get() != this){
            getResolvedThis().get().setInputElement(inputElement);
        }
    }

    @Override
    public void setOutputElement(ArchitectureElementSymbol outputElement) {
        super.setOutputElement(outputElement);
        if (getResolvedThis().isPresent() && getResolvedThis().get() != this){
            getResolvedThis().get().setOutputElement(outputElement);
        }
    }

    @Override
    protected void putInScope(Scope scope){
        Collection<Symbol> symbolsInScope = scope.getLocalSymbols().get(getName());
        if (symbolsInScope == null || !symbolsInScope.contains(this)){
            scope.getAsMutableScope().add(this);
            /*if (getResolvedThis().isPresent()){
                getResolvedThis().get().putInScope(getSpannedScope());
            }*/
            for (ArgumentSymbol argument : getArguments()){
                argument.putInScope(getSpannedScope());
            }
        }
    }

    @Override
    public boolean isAtomic(){
        return getResolvedThis().isPresent() && getResolvedThis().get() == this;
    }

    @Override
    public List<ArchitectureElementSymbol> getFirstAtomicElements() {
        if (isAtomic()){
            return Collections.singletonList(this);
        }
        else {
            return getResolvedThis().get().getFirstAtomicElements();
        }
    }

    @Override
    public List<ArchitectureElementSymbol> getLastAtomicElements() {
        if (isAtomic()){
            return Collections.singletonList(this);
        }
        else {
            return getResolvedThis().get().getLastAtomicElements();
        }
    }

    @Override
    public Set<VariableSymbol> resolve() throws ArchResolveException {
        if (!isResolved()) {
            if (isResolvable()) {
                getDeclaration();
                resolveExpressions();
                int parallelLength = getParallelLength().get();
                int maxSerialLength = getMaxSerialLength().get();

                if (!isActive() || maxSerialLength == 0) {
                    //set resolvedThis to empty composite to remove the layer.
                    setResolvedThis(new CompositeElementSymbol.Builder().build());
                }
                else if (parallelLength == 1 && maxSerialLength == 1) {
                    //resolve the layer call
                    ArchitectureElementSymbol resolvedLayer = getDeclaration().call(this);
                    setResolvedThis(resolvedLayer);
                }
                else {
                    //split the layer if it contains an argument sequence
                    ArchitectureElementSymbol splitComposite = resolveSequences(parallelLength, getSerialLengths().get());
                    setResolvedThis(splitComposite);
                    splitComposite.resolveOrError();
                }
            }
        }
        return getUnresolvableVariables();
    }

    private boolean isActive(){
        if (getIfExpression().isSimpleValue() && !getIfExpression().getBooleanValue().get()){
            return false;
        }
        else {
            return true;
        }
    }

    protected void resolveExpressions() throws ArchResolveException{
        for (ArgumentSymbol argument : getArguments()){
            argument.resolveExpression();
        }
    }

    private ArchitectureElementSymbol resolveSequences(int parallelLength, List<Integer> serialLengths){
        List<List<ArchitectureElementSymbol>> elements = computeExpandedSplit(parallelLength, serialLengths);
        List<ArchitectureElementSymbol> serialComposites = new ArrayList<>();

        if (elements.size() == 1){
            return createSerialSequencePart(elements.get(0));
        }
        else {
            for (List<ArchitectureElementSymbol> serialElements : elements) {
                serialComposites.add(createSerialSequencePart(serialElements));
            }
            CompositeElementSymbol parallelElement = new CompositeElementSymbol.Builder()
                    .parallel(true)
                    .elements(serialComposites)
                    .build();

            if (getAstNode().isPresent()) {
                parallelElement.setAstNode(getAstNode().get());
            }
            return parallelElement;
        }
    }

    private ArchitectureElementSymbol createSerialSequencePart(List<ArchitectureElementSymbol> elements){
        if (elements.size() == 1){
            return elements.get(0);
        }
        else {
            CompositeElementSymbol serialComposite = new CompositeElementSymbol.Builder()
                    .parallel(false)
                    .elements(elements)
                    .build();

            if (getAstNode().isPresent()){
                serialComposite.setAstNode(getAstNode().get());
            }
            return serialComposite;
        }
    }

    private List<List<ArchitectureElementSymbol>> computeExpandedSplit(int parallelLength, List<Integer> serialLengths){
        List<List<ArchitectureElementSymbol>> elements = new ArrayList<>(parallelLength);

        List<List<List<ArgumentSymbol>>> allExpandedArguments = new ArrayList<>(getArguments().size());
        for (ArgumentSymbol argument : getArguments()){
            allExpandedArguments.add(argument.expandedSplit(parallelLength, serialLengths).get());
        }

        for (int i = 0; i < parallelLength; i++){
            List<ArchitectureElementSymbol> serialElementList = new ArrayList<>(serialLengths.get(i));
            for (int j = 0; j < serialLengths.get(i); j++){
                List<ArgumentSymbol> layerArguments = new ArrayList<>();
                for (List<List<ArgumentSymbol>> args : allExpandedArguments){
                    layerArguments.add(args.get(i).get(j));
                }

                LayerSymbol layer = new LayerSymbol.Builder()
                        .declaration(getDeclaration())
                        .arguments(layerArguments)
                        .build();
                if (getAstNode().isPresent()){
                    layer.setAstNode(getAstNode().get());
                }
                serialElementList.add(layer);
            }
            elements.add(serialElementList);
        }
        return elements;
    }

    @Override
    protected void computeUnresolvableVariables(Set<VariableSymbol> unresolvableVariables, Set<VariableSymbol> allVariables) {
        for (ArgumentSymbol argument : getArguments()){
            argument.getRhs().checkIfResolvable(allVariables);
            unresolvableVariables.addAll(argument.getRhs().getUnresolvableVariables());
        }
    }

    @Override
    public List<ArchTypeSymbol> computeOutputTypes() {
        if (getResolvedThis().isPresent()) {
            if (getResolvedThis().get() == this) {
                return ((PredefinedLayerDeclaration) getDeclaration()).computeOutputTypes(getInputTypes(), this);
            }
            else {
                return getResolvedThis().get().getOutputTypes();

            }
        }
        else {
            throw new IllegalStateException("Output type cannot be computed before the layer is resolved");
        }
    }

    @Override
    public void checkInput() {
        if (getResolvedThis().isPresent()){
            if (getResolvedThis().get() == this){
                ((PredefinedLayerDeclaration) getDeclaration()).checkInput(getInputTypes(), this);
            }
            else {
                getResolvedThis().get().checkInput();
            }
        }
    }

    public Optional<ArgumentSymbol> getArgument(String name){
        for (ArgumentSymbol argument : getArguments()){
            if (argument.getName().equals(name)) {
                return Optional.of(argument);
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> getIntValue(String parameterName){
        return getTValue(parameterName, ArchExpressionSymbol::getIntValue);
    }

    public Optional<List<Integer>> getIntTupleValue(String parameterName){
        return getTValue(parameterName, ArchExpressionSymbol::getIntTupleValues);
    }

    public Optional<Boolean> getBooleanValue(String parameterName){
        return getTValue(parameterName, ArchExpressionSymbol::getBooleanValue);
    }

    public Optional<String> getStringValue(String parameterName){
        return getTValue(parameterName, ArchExpressionSymbol::getStringValue);
    }

    public Optional<Double> getDoubleValue(String parameterName){
        return getTValue(parameterName, ArchExpressionSymbol::getDoubleValue);
    }

    public Optional<Object> getValue(String parameterName){
        return getTValue(parameterName, ArchExpressionSymbol::getValue);
    }

    public <T> Optional<T> getTValue(String parameterName, Function<ArchExpressionSymbol, Optional<T>> getValue){
        Optional<ArgumentSymbol> arg = getArgument(parameterName);
        Optional<VariableSymbol> param = getDeclaration().getParameter(parameterName);
        if (arg.isPresent()){
            return getValue.apply(arg.get().getRhs());
        }
        else if (param.isPresent() && param.get().getDefaultExpression().isPresent()){
            return getValue.apply(param.get().getDefaultExpression().get());
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Integer> getParallelLength(){
        int length = -1;
        for (ArgumentSymbol argument : getArguments()) {
            if (argument.getRhs() instanceof ArchAbstractSequenceExpression) {
                Optional<Integer> optParallelLength = argument.getRhs().getParallelLength();
                if (optParallelLength.isPresent()) {
                    int argLength = optParallelLength.get();
                    if (length == -1) {
                        length = argLength;
                    }
                    else if (length != argLength) {
                        Log.error("0" + ErrorCodes.ILLEGAL_SEQUENCE_LENGTH + " Illegal sequence length. " +
                                        "Length is " + argLength + " but it should be " + length + " or not a sequence. " +
                                        "All parallel sequences in the same layer must be of the same size. "
                                , argument.getSourcePosition());
                    }
                }
                else {
                    return Optional.empty();
                }
            }
        }
        if (length == -1) length = 1;
        return Optional.of(length);
    }

    @Override
    public Optional<Integer> getMaxSerialLength(){
        int max = 0;
        for (ArgumentSymbol arg : getArguments()){
            Optional<Integer> argLen = arg.getRhs().getMaxSerialLength();
            if (argLen.isPresent()){
                if (argLen.get() > max){
                    max = argLen.get();
                }
            }
            else {
                return Optional.empty();
            }
        }
        if (getArguments().isEmpty()){
            max = 1;
        }
        return Optional.of(max);
    }

    @Override
    public Optional<List<Integer>> getSerialLengths(){
        Optional<Integer> optParallelLength = getParallelLength();
        if (optParallelLength.isPresent()){
            Optional<List<List<Integer>>> allArgLengths = expandArgumentSerialLengths(getArguments(), optParallelLength.get());
            if (allArgLengths.isPresent()){
                List<Integer> serialLengths = new ArrayList<>(optParallelLength.get());
                for (int i = 0; i < optParallelLength.get(); i++){
                    int serialLength = checkSerialLength(allArgLengths.get(), i);
                    serialLengths.add(serialLength);
                }
                return Optional.of(serialLengths);
            }
        }
        return Optional.empty();
    }

    private int checkSerialLength(List<List<Integer>> allArgumentLengths, int serialIndex){
        int serialLength = -1;
        for (List<Integer> argLengths : allArgumentLengths){
            int argLength = argLengths.get(serialIndex);
            if (serialLength == -1){
                serialLength = argLength;
            }
            else if (serialLength == 1) {
                serialLength = argLength;
            }
            else if (argLength != 1 && argLength != serialLength){
                Log.error("0" + ErrorCodes.ILLEGAL_SEQUENCE_LENGTH + " Illegal sequence length. " +
                                "Length of sequence dimension "+ serialIndex +" is " + argLength + " but it should be " + serialLength + " or not a sequence. " +
                                "All serial sequences of the same paralle dimension in the same layer must be of the same size. "
                        , getSourcePosition());
            }
        }
        if (serialLength == -1){
            serialLength = 1;
        }
        return serialLength;
    }

    private Optional<List<List<Integer>>> expandArgumentSerialLengths(List<ArgumentSymbol> arguments, int parallelLength){
        List<List<Integer>> argumentLengths = new ArrayList<>();
        for (ArgumentSymbol arg : arguments){
            Optional<List<Integer>> argLen = arg.getRhs().getSerialLengths();
            if (argLen.isPresent()){
                if (argLen.get().size() == 1){
                    argumentLengths.add(Collections.nCopies(parallelLength, argLen.get().get(0)));
                }
                else {
                    //assuming argLen.get().size() == parallelLength.
                    argumentLengths.add(argLen.get());
                }
            }
            else {
                return Optional.empty();
            }
        }
        if (getArguments().isEmpty()){
            argumentLengths.add(Collections.singletonList(1));
        }
        return Optional.of(argumentLengths);
    }

    @Override
    protected ArchitectureElementSymbol preResolveDeepCopy() {
        LayerSymbol copy = new LayerSymbol(getName());
        if (getAstNode().isPresent()){
            copy.setAstNode(getAstNode().get());
        }

        List<ArgumentSymbol> args = new ArrayList<>(getArguments().size());
        for (ArgumentSymbol argument : getArguments()){
            args.add(argument.preResolveDeepCopy());
        }
        copy.setArguments(args);

        return copy;
    }

    public static class Builder{
        private LayerDeclarationSymbol declaration;
        private List<ArgumentSymbol> arguments = new ArrayList<>();
        private boolean isResolved = false;

        public Builder declaration(LayerDeclarationSymbol declaration){
            this.declaration = declaration;
            return this;
        }

        public Builder arguments(List<ArgumentSymbol> arguments){
            this.arguments = arguments;
            return this;
        }

        public Builder arguments(ArgumentSymbol... arguments){
            this.arguments = Arrays.asList(arguments);
            return this;
        }

        public Builder isResolved(boolean isResolved){
            this.isResolved = isResolved;
            return this;
        }

        public LayerSymbol build(){
            if (declaration == null){
                throw new IllegalStateException("Missing declaration for LayerSymbol");
            }
            LayerSymbol sym = new LayerSymbol(declaration.getName());
            sym.setDeclaration(declaration);
            sym.setArguments(arguments);
            if (isResolved){
                sym.setResolvedThis(sym);
            }
            return sym;
        }

    }

}
