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
import de.se_rwth.commons.logging.Log;

import java.util.*;
import java.util.function.BiFunction;

public class MethodLayerSymbol extends LayerSymbol {

    private MethodDeclarationSymbol method = null;
    private List<ArgumentSymbol> arguments;
    private ArchExpressionSymbol ifArgument = ArchSimpleExpressionSymbol.TRUE;
    private ArchExpressionSymbol forArgument = ArchSimpleExpressionSymbol.ONE;
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

    public Optional<LayerSymbol> getResolvedThis() {
        return Optional.ofNullable(resolvedThis);
    }

    protected void setResolvedThis(LayerSymbol resolvedThis) {
        this.resolvedThis = resolvedThis;
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
            //todo
        }
        return null;
    }

    @Override
    protected Set<String> computeUnresolvableNames() {
        //todo
        return null;
    }

    @Override
    protected List<ShapeSymbol> computeOutputShapes() {
        if (getMethod().isPredefined()){
            BiFunction<List<ShapeSymbol>, MethodLayerSymbol, List<ShapeSymbol>> shapeFunction = getMethod().getShapeFunction();
            return shapeFunction.apply(getInputLayer().get().getOutputShapes(), this);
        }
        else {
            if (isResolvable()){
                resolve();
                return getResolvedThis().get().computeOutputShapes();
            }
            else {
                throw new IllegalStateException("The output shape can only be computed if this and all previous layer are resolved");
            }

        }
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
        private ArchExpressionSymbol ifArgument = ArchSimpleExpressionSymbol.TRUE;
        private ArchExpressionSymbol forArgument = ArchSimpleExpressionSymbol.ONE;

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

        public Builder ifArgument(ArchExpressionSymbol ifArgument){
            this.ifArgument = ifArgument;
            return this;
        }

        public Builder forArgument(ArchExpressionSymbol forArgument){
            this.forArgument = forArgument;
            return this;
        }

        public MethodLayerSymbol build(){
            if (name == null || name.equals("")){
                throw new IllegalStateException("Missing or empty name for MethodLayerSymbol");
            }
            MethodLayerSymbol sym = new MethodLayerSymbol(name);
            sym.setArguments(arguments);
            sym.setIfArgument(ifArgument);
            sym.setForArgument(forArgument);
            return sym;
        }

    }

}
