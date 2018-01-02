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
/* generated by template symboltable.ScopeSpanningSymbol*/


package de.monticore.lang.monticar.cnnarch._symboltable;

import de.monticore.lang.monticar.cnnarch.PredefinedVariables;
import de.monticore.symboltable.CommonScopeSpanningSymbol;

import java.util.*;
import java.util.function.BiFunction;

public class MethodDeclarationSymbol extends CommonScopeSpanningSymbol {

    public static final MethodDeclarationKind KIND = new MethodDeclarationKind();

    private List<VariableSymbol> parameters;
    private CompositeLayerSymbol body;
    private BiFunction<List<ShapeSymbol>, MethodLayerSymbol, List<ShapeSymbol>> shapeFunction = null;

    protected MethodDeclarationSymbol(String name) {
        super(name, KIND);
    }

    @Override
    protected MethodDeclarationScope createSpannedScope() {
        return new MethodDeclarationScope();
    }

    @Override
    public MethodDeclarationScope getSpannedScope() {
        return (MethodDeclarationScope) super.getSpannedScope();
    }

    public List<VariableSymbol> getParameters() {
        return parameters;
    }

    protected void setParameters(List<VariableSymbol> parameters) {
        this.parameters = parameters;
        if (!getParameter(PredefinedVariables.IF_NAME).isPresent()){
            VariableSymbol ifParam = PredefinedVariables.createIfParameter();
            this.parameters.add(ifParam);
            ifParam.putInScope(getSpannedScope());
        }
        if (!getParameter(PredefinedVariables.FOR_NAME).isPresent()){
            VariableSymbol forParam = PredefinedVariables.createForParameter();
            this.parameters.add(forParam);
            forParam.putInScope(getSpannedScope());
        }
        if (!getParameter(PredefinedVariables.CARDINALITY_NAME).isPresent()){
            VariableSymbol forParam = PredefinedVariables.createCardinalityParameter();
            this.parameters.add(forParam);
            forParam.putInScope(getSpannedScope());
        }
    }

    public CompositeLayerSymbol getBody() {
        return body;
    }

    protected void setBody(CompositeLayerSymbol body) {
        this.body = body;
    }

    public boolean isPredefined() {
        return shapeFunction != null;
    }

    public BiFunction<List<ShapeSymbol>, MethodLayerSymbol, List<ShapeSymbol>> getShapeFunction() {
        return shapeFunction;
    }

    protected void setShapeFunction(BiFunction<List<ShapeSymbol>, MethodLayerSymbol, List<ShapeSymbol>> shapeFunction) {
        this.shapeFunction = shapeFunction;
    }

    public Optional<VariableSymbol> getParameter(String name) {
        Optional<VariableSymbol> res = Optional.empty();
        for (VariableSymbol parameter : getParameters()){
            if (parameter.getName().equals(name)){
                res = Optional.of(parameter);
            }
        }
        return res;
    }


    public LayerSymbol call(MethodLayerSymbol layer) {
        checkForSequence(layer.getArguments());

        if (isPredefined()){
            return layer;
        }
        else {
            set(layer.getArguments());
            getBody().resolveOrError();
            CompositeLayerSymbol copy = getBody().copy();
            reset();
            return copy;
        }
    }

    private void reset(){
        for (VariableSymbol param : getParameters()){
            param.reset();
        }
        getBody().reset();
    }

    private void set(List<ArgumentSymbol> arguments){
        for (ArgumentSymbol arg : arguments){
            arg.set();
        }
    }

    private void checkForSequence(List<ArgumentSymbol> arguments){
        boolean valid = true;
        for (ArgumentSymbol arg : arguments){
            if (arg.getRhs() instanceof  ArchAbstractSequenceExpression){
                valid = false;
            }
        }
        if (!valid){
            throw new IllegalArgumentException("Arguments with sequence expressions have to be resolved first before calling the method.");
        }
    }


    public static class Builder{
        private List<VariableSymbol> parameters = new ArrayList<>();
        private CompositeLayerSymbol body;
        private String name = "";
        private BiFunction<List<ShapeSymbol>, MethodLayerSymbol, List<ShapeSymbol>> shapeFunction = (inputShape, method) -> inputShape;

        public Builder parameters(List<VariableSymbol> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder parameters(VariableSymbol... parameters) {
            this.parameters = new ArrayList<>(Arrays.asList(parameters));
            return this;
        }

        public Builder body(CompositeLayerSymbol body) {
            this.body = body;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder shapeFunction(BiFunction<List<ShapeSymbol>, MethodLayerSymbol, List<ShapeSymbol>> shapeFunction){
            this.shapeFunction = shapeFunction;
            return this;
        }

        public MethodDeclarationSymbol build(){
            if (name == null || name.equals("")){
                throw new IllegalStateException("Missing or empty name for MethodDeclarationSymbol");
            }
            MethodDeclarationSymbol sym = new MethodDeclarationSymbol(name);
            sym.setBody(body);
            for (VariableSymbol param : parameters){
                param.putInScope(sym.getSpannedScope());
            }
            sym.setParameters(parameters);
            sym.setShapeFunction(shapeFunction);
            return sym;
        }
    }
}
