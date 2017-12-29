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
import de.monticore.lang.monticar.cnnarch.PredefinedMethods;
import de.monticore.lang.monticar.cnnarch.PredefinedVariables;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

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
        MethodDeclarationSymbol method = PredefinedMethods.MAP.get(getName());
        if (method == null){
            Optional<MethodDeclarationSymbol> optMethod = getEnclosingScope().resolve(getName(), MethodDeclarationSymbol.KIND);
            if (optMethod.isPresent()){
                method = optMethod.get();
            }
        }

        if (method == null){
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
            resolvedThis.putInScope(getSpannedScope().getAsMutableScope());
        }
        this.resolvedThis = resolvedThis;
        checkIfResolvable();
    }

    protected void putInScope(MutableScope scope){
        Collection<Symbol> symbols = scope.getLocalSymbols().get(getName());
        if (symbols == null || !symbols.contains(this)){
            scope.add(this);
            for (ArgumentSymbol argument : getArguments()){
                getSpannedScope().getAsMutableScope().add(argument);
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
        if (!isResolved()){
            checkIfResolvable();
            if (isResolvable()){
                resolveExpressions();

                if (checkIfActive()){
                    int parallelLength = getParallelLength();
                    int serialLength = getSerialLength();

                    if (parallelLength == 1 && serialLength == 1){
                        //resolve the method call
                        getMethod().call(this);
                    }
                    else {
                        //split the method if it contains an argument sequence
                        resolveSequences(parallelLength, serialLength);
                    }
                }
            }
        }
        return getUnresolvableNames();
    }


    private boolean checkIfActive(){
        if (getIfExpression().isSimpleValue() && !getIfExpression().getBooleanValue().get()){
            //set resolved this to empty composite. This practically removes this method call.
            setResolvedThis(new CompositeLayerSymbol.Builder().build());
            return false;
        }
        else {
            return true;
        }
    }

    protected void resolveExpressions(){
        for (ArgumentSymbol argument : getArguments()){
            argument.getRhs().resolve(getSpannedScope());
        }
    }

    private void resolveSequences(int parallelLength, int serialLength){
        List<List<LayerSymbol>> layers = computeExpandedSplit(parallelLength, serialLength);
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

        setResolvedThis(parallelLayer);
        parallelLayer.resolve();
    }

    private List<List<LayerSymbol>> computeExpandedSplit(int parallelLength, int serialLength){
        List<List<LayerSymbol>> layers = new ArrayList<>(parallelLength);

        List<List<List<ArgumentSymbol>>> allExpandedArguments = new ArrayList<>(getArguments().size());
        for (ArgumentSymbol argument : getArguments()){
            allExpandedArguments.add(argument.expandedSplit(parallelLength, serialLength).get());
        }

        for (int i = 0; i < parallelLength; i++){
            List<LayerSymbol> serialLayerList = new ArrayList<>(serialLength);
            for (int j = 0; j < serialLength; j++){
                List<ArgumentSymbol> methodArguments = new ArrayList<>();
                for (List<List<ArgumentSymbol>> args : allExpandedArguments){
                    methodArguments.add(args.get(i).get(j));
                }

                MethodLayerSymbol method = new MethodLayerSymbol.Builder()
                        .name(getName())
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

    public int getParallelLength(){
        if (getResolvedThis().isPresent()){
            if (getResolvedThis().get() == this){
                return 1;
            }
            else{
                return getResolvedThis().get().getParallelLength();
            }
        }
        else {
            return computeLength(sequence -> sequence.getParallelLength().get());
        }
    }

    public int getSerialLength(){
        //todo check different serial lengths
        if (getResolvedThis().isPresent()){
            if (getResolvedThis().get() == this){
                return 1;
            }
            else {
                return getResolvedThis().get().getSerialLength();
            }
        }
        else {
            return computeLength(sequence -> sequence.getMaxSerialLength().get());
        }
    }

    private int computeLength(Function<ArchAbstractSequenceExpression, Integer> lengthFunction){
        int length = -1;

        for (ArgumentSymbol argument : getArguments()) {
            if (argument.getRhs().isParallelSequence()) {
                int argumentLength = lengthFunction.apply((ArchAbstractSequenceExpression) argument.getRhs());
                if (length == -1) {
                    length = argumentLength;
                }
                else if (length != argumentLength) {
                    Log.error(ErrorMessages.ILLEGAL_SEQUENCE_LENGTH_MSG +
                                    "Length is " + argumentLength + " but it should be " + length + " or not a sequence. " +
                                    "All parallel/serial sequences in the same method layer must be of the same size. "
                            , argument.getSourcePosition());
                }

            }
        }
        if (length == -1) length = 1;
        return length;
    }

    @Override
    public LayerSymbol copy() {
        List<ArgumentSymbol> args = new ArrayList<>(getArguments().size());
        for (ArgumentSymbol argument : getArguments()){
            args.add(argument.copy());
        }

        return new Builder()
                .name(getName())
                .arguments(args)
                .build();
    }

    public static class Builder{
        private String name = null;
        private List<ArgumentSymbol> arguments = new ArrayList<>();
        private boolean isResolved = false;

        public Builder name(String name){
            this.name = name;
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
            if (name == null || name.equals("")){
                throw new IllegalStateException("Missing or empty name for MethodLayerSymbol");
            }
            MethodLayerSymbol sym = new MethodLayerSymbol(name);
            sym.setArguments(arguments);
            if (isResolved){
                sym.setResolvedThis(sym);
            }
            return sym;
        }

    }

}
