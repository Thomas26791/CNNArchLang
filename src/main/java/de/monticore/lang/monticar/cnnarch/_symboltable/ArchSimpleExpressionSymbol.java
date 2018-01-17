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

import de.monticore.lang.math.math._symboltable.expression.*;
import de.monticore.lang.monticar.cnnarch.helper.Calculator;
import de.monticore.lang.monticar.cnnarch.helper.ExpressionHelper;

import java.util.*;

public class ArchSimpleExpressionSymbol extends ArchExpressionSymbol {

    private MathExpressionSymbol mathExpression = null;
    private Object value = null;

    protected ArchSimpleExpressionSymbol() {
        super();
    }

    public Optional<MathExpressionSymbol> getMathExpression() {
        return Optional.ofNullable(mathExpression);
    }

    public void setMathExpression(MathExpressionSymbol mathExpression) {
        this.mathExpression = mathExpression;
    }

    @Override
    public Optional<Object> getValue() {
        return Optional.ofNullable(value);
    }

    protected void setValue(Object value){
        this.value = value;
    }

    @Override
    public void reset(){
        if (getMathExpression().isPresent()){
            if (getMathExpression().isPresent()){
                setValue(null);
                setUnresolvableVariables(null);
            }
        }
    }

    @Override
    public boolean isSimpleValue() {
        return true;
    }

    @Override
    public boolean isBoolean() {
        if (getMathExpression().isPresent() && !(getMathExpression().get() instanceof MathNameExpressionSymbol)){
            if (getMathExpression().get().getRealMathExpressionSymbol() instanceof MathCompareExpressionSymbol){
                return true;
            }
        }
        return getBooleanValue().isPresent();
    }

    @Override
    public boolean isNumber() {
        if (getMathExpression().isPresent()){
            MathExpressionSymbol mathExp = getMathExpression().get().getRealMathExpressionSymbol();
            if (mathExp instanceof MathArithmeticExpressionSymbol || mathExp instanceof MathNumberExpressionSymbol){
                return true;
            }
        }
        return getDoubleValue().isPresent();
    }

    @Override
    public boolean isTuple() {
        if (getMathExpression().isPresent() && !(getMathExpression().get() instanceof MathNameExpressionSymbol)){
            if (getMathExpression().get().getRealMathExpressionSymbol() instanceof TupleExpressionSymbol){
                return true;
            }
        }
        return getTupleValues().isPresent();
    }

    protected void computeUnresolvableVariables(Set<VariableSymbol> unresolvableVariables, Set<VariableSymbol> allVariables) {
        if (getMathExpression().isPresent()) {
            for (MathExpressionSymbol exp : ExpressionHelper.createSubExpressionList(getMathExpression().get())) {
                if (exp instanceof MathNameExpressionSymbol) {
                    String name = ((MathNameExpressionSymbol) exp).getNameToAccess();
                    Optional<VariableSymbol> variable = getEnclosingScope().resolve(name, VariableSymbol.KIND);
                    if (variable.isPresent()) {
                        if (!allVariables.contains(variable.get())) {
                            allVariables.add(variable.get());
                            if (variable.get().hasExpression()) {
                                if (!variable.get().getExpression().isResolved()) {
                                    variable.get().getExpression().computeUnresolvableVariables(unresolvableVariables, allVariables);
                                }
                            } else {
                                unresolvableVariables.add(variable.get());
                            }
                        }
                    }
                    else {
                        unresolvableVariables.add(new VariableSymbol.Builder()
                                .name(name)
                                .type(VariableType.UNKNOWN)
                                .build());
                    }
                }
            }
        }
    }

    @Override
    public Set<VariableSymbol> resolve() {
        if (!isResolved()) {
            if (isResolvable()) {
                if (getMathExpression().isPresent() && isResolvable()) {
                    Object value = computeValue();
                    setValue(value);
                }
            }
        }
        return getUnresolvableVariables();
    }

    private Object computeValue(){
        if (getMathExpression().get() instanceof MathNameExpressionSymbol){
            return computeValue((MathNameExpressionSymbol) getMathExpression().get());
        }
        else if (getMathExpression().get() instanceof TupleExpressionSymbol){
            Map<String, String> replacementMap = new HashMap<>();
            List<Object> valueList = new ArrayList<>();
            TupleExpressionSymbol tuple = (TupleExpressionSymbol) getMathExpression().get();
            for (MathExpressionSymbol mathExp : tuple.getExpressions()){
                if (mathExp instanceof MathNameExpressionSymbol){
                    valueList.add(computeValue((MathNameExpressionSymbol) mathExp));
                }
                else {
                    ArchSimpleExpressionSymbol temp = ArchSimpleExpressionSymbol.of(mathExp);
                    temp.setEnclosingScope(getEnclosingScope().getAsMutableScope());
                    temp.resolveOrError();
                    valueList.add(temp.getValue().get());
                    getEnclosingScope().getAsMutableScope().remove(temp);
                }
            }
            return valueList;
        }
        else {
            Map<String, String> replacementMap = new HashMap<>();
            for (MathExpressionSymbol exp : ExpressionHelper.createSubExpressionList(getMathExpression().get())) {
                if (exp instanceof MathNameExpressionSymbol) {
                    String name = ((MathNameExpressionSymbol) exp).getNameToAccess();
                    VariableSymbol variable = (VariableSymbol) getEnclosingScope().resolve(name, VariableSymbol.KIND).get();
                    variable.getExpression().resolveOrError();

                    replacementMap.put(name, variable.getExpression().getTextualRepresentation());
                }
            }

            String resolvedString = ExpressionHelper.replace(getTextualRepresentation(), replacementMap);
            return Calculator.getInstance().calculate(resolvedString);
        }
    }

    private Object computeValue(MathNameExpressionSymbol mathExpression){
        String name = ((MathNameExpressionSymbol) mathExpression).getNameToAccess();
        VariableSymbol variable = (VariableSymbol) getEnclosingScope().resolve(name, VariableSymbol.KIND).get();
        variable.getExpression().resolveOrError();

        return variable.getExpression().getValue().get();
    }

    @Override
    public String getTextualRepresentation() {
        if (isResolved()){
            if (isTuple()){
                return ExpressionHelper.createTupleTextualRepresentation(getTupleValues().get(), Object::toString);
            }
            else {
                return getValue().get().toString();
            }
        }
        else {
            return getMathExpression().get().getTextualRepresentation();
        }
    }

    @Override
    public Optional<List<List<ArchSimpleExpressionSymbol>>> getElements(){
        return Optional.of(Collections.singletonList(Collections.singletonList(this)));
    }

    @Override
    public boolean isResolved() {
        return getValue().isPresent() || !getMathExpression().isPresent();
    }

    public ArchSimpleExpressionSymbol copy(){
        ArchSimpleExpressionSymbol copy = new ArchSimpleExpressionSymbol();
        copy.setMathExpression(mathExpression);
        copy.setValue(value);
        copy.setUnresolvableVariables(getUnresolvableVariables());
        return copy;
    }


    public static ArchSimpleExpressionSymbol of(int value){
        ArchSimpleExpressionSymbol res = new ArchSimpleExpressionSymbol();
        res.setValue(value);
        return res;
    }

    public static ArchSimpleExpressionSymbol of(double value){
        ArchSimpleExpressionSymbol res = new ArchSimpleExpressionSymbol();
        res.setValue(value);
        return res;
    }

    public static ArchSimpleExpressionSymbol of(boolean value){
        ArchSimpleExpressionSymbol res = new ArchSimpleExpressionSymbol();
        res.setValue(value);
        return res;
    }

    public static ArchSimpleExpressionSymbol of(String value){
        ArchSimpleExpressionSymbol res = new ArchSimpleExpressionSymbol();
        res.setValue(value);
        return res;
    }

    public static ArchSimpleExpressionSymbol of(MathExpressionSymbol expressions){
        ArchSimpleExpressionSymbol res = new ArchSimpleExpressionSymbol();
        res.setMathExpression(expressions);
        return res;
    }

    public static ArchSimpleExpressionSymbol of(List<Object> tupleValues){
        ArchSimpleExpressionSymbol res = new ArchSimpleExpressionSymbol();
        res.setValue(tupleValues);
        return res;
    }

    public static ArchSimpleExpressionSymbol of(VariableSymbol variable){
        MathExpressionSymbol exp = new MathNameExpressionSymbol(variable.getName());
        ArchSimpleExpressionSymbol res = new ArchSimpleExpressionSymbol();
        res.setMathExpression(exp);
        return res;
    }
}
