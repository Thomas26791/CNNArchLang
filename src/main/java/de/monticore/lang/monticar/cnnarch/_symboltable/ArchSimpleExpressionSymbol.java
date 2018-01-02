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
import de.monticore.lang.monticar.interfaces.TextualExpression;

import java.util.*;

public class ArchSimpleExpressionSymbol extends ArchExpressionSymbol implements TextualExpression {

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
    public boolean isSimpleValue() {
        return true;
    }

    @Override
    public boolean isBoolean() {
        if (getMathExpression().isPresent()){
            return getMathExpression().get() instanceof MathCompareExpressionSymbol;
        }
        else {
            return getBooleanValue().isPresent();
        }
    }

    @Override
    public boolean isNumber() {
        if (getMathExpression().isPresent()){
            return getMathExpression().get() instanceof MathArithmeticExpressionSymbol;
        }
        else {
            return getDoubleValue().isPresent();
        }
    }

    @Override
    public boolean isTuple() {
        if (getMathExpression().isPresent()){
            return getMathExpression().get() instanceof TupleExpressionSymbol;
        }
        else {
            return getValue().get() instanceof List;
        }
    }

    @Override
    protected Set<String> computeUnresolvableNames() {
        Set<String> unresolvableNames = new HashSet<>();
        Set<String> allNames = new HashSet<>();
        computeUnresolvableNames(unresolvableNames, allNames);
        return unresolvableNames;
    }

    protected void computeUnresolvableNames(Set<String> unresolvableNames, Set<String> allNames) {
        if (getMathExpression().isPresent()) {
            for (MathExpressionSymbol exp : ExpressionHelper.createSubExpressionList(getMathExpression().get())) {
                if (exp instanceof MathNameExpressionSymbol) {
                    String name = ((MathNameExpressionSymbol) exp).getNameToAccess();
                    if (!allNames.contains(name)) {
                        allNames.add(name);
                        Optional<VariableSymbol> variable = getEnclosingScope().resolve(name, VariableSymbol.KIND);
                        if (variable.isPresent() && !variable.get().getExpression().isResolved()) {
                            if (variable.get().hasValue()) {
                                variable.get().getExpression().computeUnresolvableNames(unresolvableNames, allNames);
                            } else {
                                unresolvableNames.add(name);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Set<String> resolve() {
        checkIfResolvable();
        if (getMathExpression().isPresent() && isResolvable()) {
            Object value;
            if (isTuple()){
                TupleExpressionSymbol tuple = (TupleExpressionSymbol) getMathExpression().get();
                List<Object> tupleValues = new ArrayList<>(tuple.getExpressions().size());
                for (MathExpressionSymbol exp : tuple.getExpressions()){
                    tupleValues.add(computeValue());
                }
                value = tupleValues;
            }
            else {
                value = computeValue();
            }
            setValue(value);
        }
        return getUnresolvableNames();
    }

    private Object computeValue(){
        Map<String, String> replacementMap = new HashMap<>();
        for (MathExpressionSymbol exp : ExpressionHelper.createSubExpressionList(getMathExpression().get())) {
            if (exp instanceof MathNameExpressionSymbol) {
                String name = ((MathNameExpressionSymbol) exp).getNameToAccess();
                VariableSymbol variable = (VariableSymbol) getEnclosingScope().resolve(name, VariableSymbol.KIND).get();
                if (!variable.getExpression().isResolved()) {
                    variable.getExpression().resolveOrError();
                }

                replacementMap.put(name, variable.getExpression().getTextualRepresentation());
            }
        }

        String resolvedString = ExpressionHelper.replace(getTextualRepresentation(), replacementMap);
        return Calculator.getInstance().calculate(resolvedString);
    }

    @Override
    public String getTextualRepresentation() {
        if (isResolved()){
            if (isTuple()){
                return ExpressionHelper.createTupleTextualRepresentation(getTupleValue().get(), Object::toString);
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
        //copy.setMathExpression(mathExpression);
        copy.setValue(value);
        copy.setUnresolvableNames(getUnresolvableNames());
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
