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

public class MethodLayerSymbol extends LayerSymbol {

    private MethodDeclarationSymbol method = null;
    private List<ArgumentSymbol> arguments;

    protected MethodLayerSymbol(String name) {
        super(name);
    }

    public MethodDeclarationSymbol getMethod() {
        if (method == null){
            Collection<MethodDeclarationSymbol> methodCollection = getEnclosingScope().resolveMany(getName(), MethodDeclarationSymbol.KIND);
            if (!methodCollection.isEmpty()){
                setMethod(methodCollection.iterator().next());
            }
        }
        return method;
    }

    @Override
    public boolean isResolvable() {
        return super.isResolvable() && getMethod() != null;
    }

    private void setMethod(MethodDeclarationSymbol method) {
        this.method = method;
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
    public void setInputLayer(LayerSymbol inputLayer) {
        super.setInputLayer(inputLayer);
        if (getResolvedThis().isPresent() && getResolvedThis().get() != this){
            getResolvedThis().get().setInputLayer(inputLayer);
        }
    }

    @Override
    public void setOutputLayer(LayerSymbol outputLayer) {
        super.setOutputLayer(outputLayer);
        if (getResolvedThis().isPresent() && getResolvedThis().get() != this){
            getResolvedThis().get().setOutputLayer(outputLayer);
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
    public List<LayerSymbol> getFirstAtomicLayers() {
        if (isAtomic()){
            return Collections.singletonList(this);
        }
        else {
            return getResolvedThis().get().getFirstAtomicLayers();
        }
    }

    @Override
    public List<LayerSymbol> getLastAtomicLayers() {
        if (isAtomic()){
            return Collections.singletonList(this);
        }
        else {
            return getResolvedThis().get().getLastAtomicLayers();
        }
    }

    @Override
    public Set<VariableSymbol> resolve() throws ArchResolveException {
        if (!isResolved()) {
            if (isResolvable()) {
                getMethod();
                resolveExpressions();
                int parallelLength = getParallelLength().get();
                int maxSerialLength = getMaxSerialLength().get();

                if (!isActive() || maxSerialLength == 0) {
                    //set resolvedThis to empty composite. This practically removes this method call.
                    setResolvedThis(new CompositeLayerSymbol.Builder().build());
                }
                else if (parallelLength == 1 && maxSerialLength == 1) {
                    //resolve the method call
                    LayerSymbol resolvedMethod = getMethod().call(this);
                    setResolvedThis(resolvedMethod);
                }
                else {
                    //split the method if it contains an argument sequence
                    LayerSymbol splitComposite = resolveSequences(parallelLength, getSerialLengths().get());
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

    private LayerSymbol resolveSequences(int parallelLength, List<Integer> serialLengths){
        List<List<LayerSymbol>> layers = computeExpandedSplit(parallelLength, serialLengths);
        List<LayerSymbol> serialComposites = new ArrayList<>();

        if (layers.size() == 1){
            return createSerialSequencePart(layers.get(0));
        }
        else {
            for (List<LayerSymbol> serialLayers : layers) {
                serialComposites.add(createSerialSequencePart(serialLayers));
            }
            CompositeLayerSymbol parallelLayer = new CompositeLayerSymbol.Builder()
                    .parallel(true)
                    .layers(serialComposites)
                    .build();

            if (getAstNode().isPresent()) {
                parallelLayer.setAstNode(getAstNode().get());
            }
            return parallelLayer;
        }
    }

    private LayerSymbol createSerialSequencePart(List<LayerSymbol> layers){
        if (layers.size() == 1){
            return layers.get(0);
        }
        else {
            CompositeLayerSymbol serialComposite = new CompositeLayerSymbol.Builder()
                    .parallel(false)
                    .layers(layers)
                    .build();

            if (getAstNode().isPresent()){
                serialComposite.setAstNode(getAstNode().get());
            }
            return serialComposite;
        }
    }

    private List<List<LayerSymbol>> computeExpandedSplit(int parallelLength, List<Integer> serialLengths){
        List<List<LayerSymbol>> layers = new ArrayList<>(parallelLength);

        List<List<List<ArgumentSymbol>>> allExpandedArguments = new ArrayList<>(getArguments().size());
        for (ArgumentSymbol argument : getArguments()){
            allExpandedArguments.add(argument.expandedSplit(parallelLength, serialLengths).get());
        }

        for (int i = 0; i < parallelLength; i++){
            List<LayerSymbol> serialLayerList = new ArrayList<>(serialLengths.get(i));
            for (int j = 0; j < serialLengths.get(i); j++){
                List<ArgumentSymbol> methodArguments = new ArrayList<>();
                for (List<List<ArgumentSymbol>> args : allExpandedArguments){
                    methodArguments.add(args.get(i).get(j));
                }

                MethodLayerSymbol method = new MethodLayerSymbol.Builder()
                        .method(getMethod())
                        .arguments(methodArguments)
                        .build();
                if (getAstNode().isPresent()){
                    method.setAstNode(getAstNode().get());
                }
                serialLayerList.add(method);
            }
            layers.add(serialLayerList);
        }
        return layers;
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
                return ((PredefinedMethodDeclaration) getMethod()).computeOutputTypes(getInputTypes(), this);
            }
            else {
                return getResolvedThis().get().getOutputTypes();

            }
        }
        else {
            throw new IllegalStateException("Output type cannot be computed before the method is resolved");
        }
    }

    @Override
    public void checkInput() {
        if (getResolvedThis().isPresent()){
            if (getResolvedThis().get() == this){
                ((PredefinedMethodDeclaration) getMethod()).checkInput(getInputTypes(), this);
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
        Optional<VariableSymbol> param = getMethod().getParameter(parameterName);
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
                                        "All parallel sequences in the same method layer must be of the same size. "
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
                                "All serial sequences of the same paralle dimension in the same method layer must be of the same size. "
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
    protected LayerSymbol preResolveDeepCopy() {
        MethodLayerSymbol copy = new MethodLayerSymbol(getName());
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
        private MethodDeclarationSymbol method;
        private List<ArgumentSymbol> arguments = new ArrayList<>();
        private boolean isResolved = false;

        public Builder method(MethodDeclarationSymbol method){
            this.method = method;
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

        public MethodLayerSymbol build(){
            if (method == null){
                throw new IllegalStateException("Missing method for MethodLayerSymbol");
            }
            MethodLayerSymbol sym = new MethodLayerSymbol(method.getName());
            sym.setMethod(method);
            sym.setArguments(arguments);
            if (isResolved){
                sym.setResolvedThis(sym);
            }
            return sym;
        }

    }

}
