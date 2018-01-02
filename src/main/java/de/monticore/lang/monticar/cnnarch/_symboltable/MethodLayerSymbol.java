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
import de.monticore.lang.monticar.cnnarch.PredefinedVariables;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

import java.util.*;
import java.util.function.BiFunction;

public class MethodLayerSymbol extends LayerSymbol {

    private MethodDeclarationSymbol method = null;
    private List<ArgumentSymbol> arguments;
    private LayerSymbol resolvedThis = null;

    protected MethodLayerSymbol(String name) {
        super(name);
    }

    public MethodDeclarationSymbol getMethod() {
        if (method == null){
            setMethod(resolveMethodSymbolByName());
        }
        return method;
    }

    private MethodDeclarationSymbol resolveMethodSymbolByName(){
        MethodDeclarationSymbol method = null;
        Optional<MethodDeclarationSymbol> optMethod = getEnclosingScope().resolve(getName(), MethodDeclarationSymbol.KIND);
        if (optMethod.isPresent()){
            method = optMethod.get();
        }
        else {
            Log.error(ErrorMessages.UNKNOWN_NAME_MSG + "Method with name " + getName() + " could not be resolved", getSourcePosition());
        }

        return method;
    }

    private void setMethod(MethodDeclarationSymbol method) {
        if (method.isPredefined()){
            setResolvedThis(this);
        }
        this.method = method;
    }

    public List<ArgumentSymbol> getArguments() {
        return arguments;
    }

    protected void setArguments(List<ArgumentSymbol> arguments) {
        this.arguments = arguments;
    }

    public ArchExpressionSymbol getIfExpression(){
        return getMethod().getParameter(PredefinedVariables.IF_NAME).get().getExpression();
    }

    public ArchExpressionSymbol getForExpression(){
        return getMethod().getParameter(PredefinedVariables.FOR_NAME).get().getExpression();
    }

    public Optional<LayerSymbol> getResolvedThis() {
        return Optional.ofNullable(resolvedThis);
    }

    protected void setResolvedThis(LayerSymbol resolvedThis) {
        if (resolvedThis != null && resolvedThis != this){
            resolvedThis.putInScope(getSpannedScope());
        }
        this.resolvedThis = resolvedThis;
    }

    @Override
    protected void putInScope(LayerScope scope){
        Collection<Symbol> symbolsInScope = scope.getLocalSymbols().get(getName());
        if (symbolsInScope == null || !symbolsInScope.contains(this)){
            scope.add(this);
            for (ArgumentSymbol argument : getArguments()){
                argument.putInScope(getSpannedScope());
            }
        }
    }

    @Override
    public boolean isMethod(){
        return true;
    }

    public boolean isResolved(){
        return getResolvedThis().isPresent();
    }

    @Override
    public Set<String> resolve() {
        //todo checkForRecursion()
        checkIfResolvable();
        if (isResolvable()){
            resolveExpressions();
            int parallelLength = getParallelLength().get();
            int maxSerialLength = getMaxSerialLength().get();

            if (!isActive() || maxSerialLength == 0){
                //set resolvedThis to empty composite. This practically removes this method call.
                setResolvedThis(new CompositeLayerSymbol.Builder().build());
            }
            else if (parallelLength == 1 && maxSerialLength == 1){
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
        return getUnresolvableNames();
    }

    /*protected void computeResolvedThis(){
        int parallelLength = getParallelLength().get();
        int maxSerialLength = getMaxSerialLength().get();

        if (!isActive() || maxSerialLength == 0){
            //set resolvedThis to empty composite. This practically removes this method call.
            setResolvedThis(new CompositeLayerSymbol.Builder().build());
        }
        else if (parallelLength == 1 && maxSerialLength == 1){
            //resolve the method call
            LayerSymbol resolvedMethod = getMethod().call(this);
            setResolvedThis(resolvedMethod);
            resolvedMethod.computeResolvedThis();
        }
        else {
            //split the method if it contains an argument sequence
            LayerSymbol splitComposite = resolveSequences(parallelLength, getSerialLengths().get());
            setResolvedThis(splitComposite);
            splitComposite.resolveOrError();
        }
    }*/

    private boolean isActive(){
        if (getIfExpression().isSimpleValue() && !getIfExpression().getBooleanValue().get()){
            return false;
        }
        else {
            return true;
        }
    }

    protected void resolveExpressions(){
        for (ArgumentSymbol argument : getArguments()){
            argument.getRhs().resolveOrError();
        }
    }

    private LayerSymbol resolveSequences(int parallelLength, List<Integer> serialLengths){
        List<List<LayerSymbol>> layers = computeExpandedSplit(parallelLength, serialLengths);
        List<LayerSymbol> serialComposites = new ArrayList<>();

        for (List<LayerSymbol> serialLayers : layers){
            CompositeLayerSymbol serialComposite = new CompositeLayerSymbol.Builder()
                    .parallel(false)
                    .layers(serialLayers)
                    .build();
            serialComposites.add(serialComposite);
        }
        CompositeLayerSymbol parallelLayer = new CompositeLayerSymbol.Builder()
                .parallel(true)
                .layers(serialComposites)
                .build();
        return parallelLayer;
    }

    private List<List<LayerSymbol>> computeExpandedSplit(int parallelLength, List<Integer> serialLengths){
        //todo change serialLength to List
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
                serialLayerList.add(method);
            }
            layers.add(serialLayerList);
        }
        return layers;
    }

    @Override
    protected Set<String> computeUnresolvableNames() {
        Set<String> unresolvableNames = new HashSet<>();
        for (ArgumentSymbol argument : getArguments()){
            unresolvableNames.addAll(argument.getRhs().computeUnresolvableNames());
        }
        return unresolvableNames;
    }

    @Override
    protected List<ShapeSymbol> computeOutputShapes() {
        if (getMethod().isPredefined()){
            BiFunction<List<ShapeSymbol>, MethodLayerSymbol, List<ShapeSymbol>> shapeFunction = getMethod().getShapeFunction();
            return shapeFunction.apply(getInputLayer().get().getOutputShapes(), this);
        }
        else {
            Set<String> unresolvableNames = resolve();
            if (unresolvableNames.isEmpty()){
                return getResolvedThis().get().computeOutputShapes();
            }
            else {
                throw new IllegalStateException("The output shape can only be computed if this and all previous layer are resolvable. " +
                        "The following names cannot be resolved: " + String.join(", ", unresolvableNames));
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

    public Optional<Integer> getIntValue(String argumentName){
        Optional<ArgumentSymbol> arg = getArgument(argumentName);
        if (arg.isPresent()){
            Optional<Object> val = arg.get().getValue();
            if (val.isPresent() && val.get() instanceof Integer){
                return val.map(o -> (Integer) o);
            }
        }
        return Optional.empty();
    }

    public Optional<List<Integer>> getIntTupleValue(String argumentName){
        Optional<ArgumentSymbol> arg = getArgument(argumentName);
        if (arg.isPresent()){
            Optional<Object> val = arg.get().getValue();
            if (val.isPresent() && val.get() instanceof List){
                List<Integer> list = new ArrayList<>();
                for (Object obj : (List) val.get()){
                    if (obj instanceof Integer){
                        list.add((Integer) obj);
                    }
                    else{
                        return Optional.empty();
                    }
                }
                return Optional.of(list);
            }
        }
        return Optional.empty();
    }

    //todo outputShape Function partial check and check argument correctness

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
                        Log.error(ErrorMessages.ILLEGAL_SEQUENCE_LENGTH_MSG +
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
        int serialLength = 0;
        for (List<Integer> argLengths : allArgumentLengths){
            int argLength = argLengths.get(serialIndex);
            if (serialLength == 0){
                serialLength = argLength;
            }
            else if (serialLength == 1) {
                if (argLength > 1){
                    serialLength = argLength;
                }
            }
            else if (argLength != 1 && argLength != serialLength){
                Log.error(ErrorMessages.ILLEGAL_SEQUENCE_LENGTH_MSG +
                                "Length of sequence dimension "+ serialIndex +" is " + argLength + " but it should be " + serialLength + " or not a sequence. " +
                                "All serial sequences of the same paralle dimension in the same method layer must be of the same size. "
                        , getSourcePosition());
            }
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
        return Optional.of(argumentLengths);
    }

    @Override
    public LayerSymbol copy() {
        MethodLayerSymbol copy = new MethodLayerSymbol(getName());
        List<ArgumentSymbol> args = new ArrayList<>(getArguments().size());
        for (ArgumentSymbol argument : getArguments()){
            args.add(argument.copy());
        }
        copy.setArguments(args);
        copy.setMethod(getMethod());
        if (getResolvedThis().isPresent() && getResolvedThis().get() != this) {
            copy.setResolvedThis(getResolvedThis().get().copy());
        }
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
