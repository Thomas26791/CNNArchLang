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


import de.se_rwth.commons.logging.Log;

import java.util.*;
import java.util.function.BiFunction;

public class MethodLayerSymbol extends LayerSymbol {

    private MethodDeclarationSymbol method = null;
    private List<ArgumentSymbol> arguments;
    private ArchExpressionSymbol ifArgument = ArchSimpleExpressionSymbol.TRUE;
    private ArchExpressionSymbol forArgument = ArchSimpleExpressionSymbol.ONE;

    protected MethodLayerSymbol(String name) {
        super(name);
    }

    public MethodDeclarationSymbol getMethod() {
        if (method == null){
            Optional<MethodDeclarationSymbol> optMethod = getEnclosingScope().resolve(getName(), MethodDeclarationSymbol.KIND);
            if (optMethod.isPresent()){
                setMethod(optMethod.get());
            }
            else {
                Log.error("method with name " + getName() + " could not be resolved", getSourcePosition());
            }
        }
        return method;
    }

    protected void setMethod(MethodDeclarationSymbol method) {
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

    public ArchExpressionSymbol isIfArgument() {
        return ifArgument;
    }

    protected void setIfArgument(ArchExpressionSymbol ifArgument) {
        this.ifArgument = ifArgument;
    }

    public ArchExpressionSymbol getForArgument() {
        return forArgument;
    }

    protected void setForArgument(ArchExpressionSymbol forArgument) {
        this.forArgument = forArgument;
    }

    @Override
    public boolean isMethod(){
        return true;
    }

    @Override
    public Set<String> resolve() {
        //todo
        return null;
    }

    @Override
    protected void checkIfResolved() {
        //todo
    }

    @Override
    protected List<ShapeSymbol> computeOutputShape() {
        if (getMethod().isPredefined()){
            BiFunction<List<ShapeSymbol>, MethodLayerSymbol, List<ShapeSymbol>> shapeFunction = getMethod().getShapeFunction();
            return shapeFunction.apply(getInputLayer().getOutputShapes(), this);
        }
        else {
            if (isResolved()){
                return getResolvedThis().get().computeOutputShape();
            }
            else {
                throw new IllegalStateException("The output shape can only be computed if this and all previous layer are resolved");
            }

        }
    }

    @Override
    public boolean isResolvable() {
        //todo
        return false;
    }

    public Optional<LayerSymbol> call(){
        return getMethod().call(this);
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

    //todo outputShape Function partial apply and check argument correctness

    public boolean isCallable(){
        boolean callable = true;
        for (ArgumentSymbol argument : getArguments()) {
            if (argument.getRhs().isRange()){
                argument.getRhs().resolve();
                if (!argument.getRhs().isFullyResolved()){
                    callable = false;
                }
            }
        }
        return callable;
    }

    public Optional<Integer> getParallelLength(){
        if (isCallable()) {
            int parallelLength = -1;
            for (ArgumentSymbol argument : getArguments()) {
                if (argument.getRhs().isParallelSequence()) {
                    int argumentLength = ((ArchAbstractSequenceExpression) argument.getRhs()).getParallelLength().get();
                    if (parallelLength == -1) {
                        parallelLength = argumentLength;
                    } else if (parallelLength != argumentLength) {
                        Log.error("Illegal sequence length. " +
                                        "All serial sequences in the same method layer must be of the same size."
                                , argument.getSourcePosition());
                    }

                }
            }
            if (parallelLength == -1) parallelLength = 1;
            return Optional.of(parallelLength);
        }
        else {
            return Optional.empty();
        }
    }

    public Optional<List<MethodLayerSymbol>> parallelize(){
        Optional<Integer> optLength = getParallelLength();
        if (optLength.isPresent()){
            for (int i = 0; i < optLength.get(); i++){
                for (ArgumentSymbol arg : getArguments()){

                }
            }
            //todo
            return null;
        }
        else {
            return Optional.empty();
        }
    }

    private List<List<ArgumentSymbol>> expand(ArgumentSymbol argument, int parallelIndex, int serialIndex){
        if (argument.getRhs().isRange()){
            ArchRangeExpressionSymbol range = (ArchRangeExpressionSymbol) argument.getRhs();
            range.getValue();
        }
        //todo
        return null;
    }

    public Optional<Integer> getSerialLength(){
        if (isCallable()) {
            int serialLength = -1;
            for (ArgumentSymbol argument : getArguments()) {
                if (argument.getRhs().isSerialSequence()) {
                    int argumentLength = ((ArchAbstractSequenceExpression) argument.getRhs()).getSerialLength().get();
                    if (serialLength == -1) {
                        serialLength = argumentLength;
                    } else if (serialLength != argumentLength) {
                        Log.error("Illegal sequence length. " +
                                        "All serial sequences in the same method layer must be of the same size."
                                , argument.getSourcePosition());
                    }

                }
            }
            if (serialLength == -1) serialLength = 1;
            return Optional.of(serialLength);
        }
        else {
            return Optional.empty();
        }
    }


    public static class Builder{
        private String name = null;
        private List<ArgumentSymbol> arguments;
        private MethodDeclarationSymbol method;
        private ArchExpressionSymbol ifArgument = ArchSimpleExpressionSymbol.TRUE;
        private ArchExpressionSymbol forArgument = ArchSimpleExpressionSymbol.ONE;
        private LayerSymbol inputLayer;

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

        //will be assigned automatically by name if not set
        public Builder method(MethodDeclarationSymbol method){
            this.method = method;
            return this;
        }

        public Builder ifArgument(ArchExpressionSymbol ifArgument){
            this.ifArgument = ifArgument;
            return this;
        }

        public Builder forArgument(ArchExpressionSymbol forArgument){
            this.forArgument = forArgument;
            return this;
        }

        public Builder inputLayer(LayerSymbol inputLayer){
            this.inputLayer = inputLayer;
            return this;
        }

        public MethodLayerSymbol build(){
            if (name == null || name.equals("")){
                throw new IllegalStateException("Missing name for MethodLayerSymbol");
            }
            MethodLayerSymbol sym = new MethodLayerSymbol(name);
            sym.setArguments(arguments);
            if (method == null){
                sym.getMethod();
            }
            else{
                sym.setMethod(method);
            }
            sym.setIfArgument(ifArgument);
            sym.setForArgument(forArgument);
            sym.setInputLayer(inputLayer);
            return sym;
        }

    }

}
