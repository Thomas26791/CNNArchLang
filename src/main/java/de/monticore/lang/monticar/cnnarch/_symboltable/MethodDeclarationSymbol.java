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

import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedVariables;
import de.monticore.symboltable.CommonScopeSpanningSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class MethodDeclarationSymbol extends CommonScopeSpanningSymbol {

    public static final MethodDeclarationKind KIND = new MethodDeclarationKind();

    private List<VariableSymbol> parameters;
    private CompositeLayerSymbol body;


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
        if (!getParameter(AllPredefinedVariables.IF_NAME).isPresent()){
            VariableSymbol ifParam = AllPredefinedVariables.createIfParameter();
            this.parameters.add(ifParam);
            ifParam.putInScope(getSpannedScope());
        }
        if (!getParameter(AllPredefinedVariables.FOR_NAME).isPresent()){
            VariableSymbol forParam = AllPredefinedVariables.createForParameter();
            this.parameters.add(forParam);
            forParam.putInScope(getSpannedScope());
        }
        if (!getParameter(AllPredefinedVariables.CARDINALITY_NAME).isPresent()){
            VariableSymbol forParam = AllPredefinedVariables.createCardinalityParameter();
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
        //Override by PredefinedMethodDeclaration
        return false;
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


    public LayerSymbol call(MethodLayerSymbol layer) throws ArchResolveException{
        checkForSequence(layer.getArguments());

        if (isPredefined()){
            return layer;
        }
        else {
            reset();
            set(layer.getArguments());

            CompositeLayerSymbol copy = getBody().copy();
            copy.putInScope(getSpannedScope());
            copy.resolveOrError();
            getSpannedScope().remove(copy);
            getSpannedScope().removeSubScope(copy.getSpannedScope());

            reset();
            return copy;
        }
    }

    private void reset(){
        for (VariableSymbol param : getParameters()){
            param.reset();
        }
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


    /*public static class Builder{
        private List<VariableSymbol> parameters = new ArrayList<>();
        private CompositeLayerSymbol body;
        private String name = "";

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

        public MethodDeclarationSymbol build(){
            if (name == null || name.equals("")){
                throw new IllegalStateException("Missing or empty name for MethodDeclarationSymbol");
            }
            MethodDeclarationSymbol sym = new MethodDeclarationSymbol(name);
            sym.setBody(body);
            if (body != null){
                body.putInScope(sym.getSpannedScope());
            }
            for (VariableSymbol param : parameters){
                param.putInScope(sym.getSpannedScope());
            }
            sym.setParameters(parameters);
            return sym;
        }
    }*/
}
