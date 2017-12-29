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

import de.monticore.lang.monticar.cnnarch.Constraint;
import de.monticore.symboltable.CommonSymbol;
import de.se_rwth.commons.logging.Log;
import org.jscience.mathematics.number.Rational;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static de.monticore.lang.monticar.cnnarch.ErrorMessages.MISSING_VAR_VALUE_CODE;

public class VariableSymbol extends CommonSymbol {

    public static final VariableKind KIND = new VariableKind();

    private VariableType type;
    private ArchSimpleExpressionSymbol defaultExpression = null; //Optional
    private ArchSimpleExpressionSymbol currentExpression = null; //Optional
    private Set<Constraint> constraints = new HashSet<>();


    protected VariableSymbol(String name) {
        super(name, KIND);
    }

    public VariableType getType() {
        return type;
    }

    protected void setType(VariableType type) {
        this.type = type;
    }

    public Optional<ArchSimpleExpressionSymbol> getDefaultExpression() {
        return Optional.ofNullable(defaultExpression);
    }

    protected void setDefaultExpression(ArchSimpleExpressionSymbol defaultExpression) {
        this.defaultExpression = defaultExpression;
    }

    protected Optional<ArchSimpleExpressionSymbol> getCurrentExpression() {
        return Optional.ofNullable(currentExpression);
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    protected void setConstraints(Set<Constraint> constraints) {
        this.constraints = constraints;
    }

    public boolean isConstant(){
        return type == VariableType.CONSTANT;
    }

    public boolean isIOVariable(){
        return type == VariableType.IOVariable;
    }

    public boolean isParameter(){
        return type == VariableType.PARAMETER;
    }


    public boolean hasValue(){
        return getCurrentExpression().isPresent() || getDefaultExpression().isPresent();
    }

    public void setExpression(ArchSimpleExpressionSymbol value){
        currentExpression = value;
    }

    public ArchSimpleExpressionSymbol getExpression(){
        ArchSimpleExpressionSymbol value = null;
        if (hasValue()){
            if (getCurrentExpression().isPresent()){
                value = getCurrentExpression().get();
            }
            else {
                value = getDefaultExpression().get();
            }
        }
        else {
            String msg = "0" + MISSING_VAR_VALUE_CODE + " The variable " + getName() + " has no value.";
            if (getAstNode().isPresent()){
                Log.error(msg, getAstNode().get().get_SourcePositionStart());
            }
            else {
                Log.error(msg);
            }
        }
        return value;
    }

    public Optional<Object> getValue(){
        return getExpression().getValue();
    }

    public void reset(){
        currentExpression = null;
    }


    public static class Builder{
        private VariableType type = VariableType.PARAMETER;
        private ArchSimpleExpressionSymbol defaultValue = null;
        private String name = null;
        private Set<Constraint> constraints = new HashSet<>();

        public Builder type(VariableType type){
            this.type = type;
            return this;
        }

        public Builder name(String name){
            this.name = name;
            return this;
        }

        public Builder defaultValue(ArchSimpleExpressionSymbol defaultValue){
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder defaultValue(int defaultValue){
            this.defaultValue = ArchSimpleExpressionSymbol.of(defaultValue);
            return this;
        }

        public Builder defaultValue(Rational defaultValue){
            this.defaultValue = ArchSimpleExpressionSymbol.of(defaultValue);
            return this;
        }

        public Builder defaultValue(boolean defaultValue){
            this.defaultValue = ArchSimpleExpressionSymbol.of(defaultValue);
            return this;
        }

        public Builder defaultValue(int... tupleValues){
            this.defaultValue = ArchSimpleExpressionSymbol.of(tupleValues);
            return this;
        }

        public Builder constraints(Constraint... constraints){
            this.constraints = new HashSet<>(Arrays.asList(constraints));
            return this;
        }

        public VariableSymbol build(){
            if (name == null || name.equals("")){
                throw new IllegalStateException("Missing or empty name for VariableSymbol");
            }
            VariableSymbol sym = new VariableSymbol(name);
            sym.setType(type);
            sym.setDefaultExpression(defaultValue);
            sym.setConstraints(constraints);
            return sym;
        }
    }
}