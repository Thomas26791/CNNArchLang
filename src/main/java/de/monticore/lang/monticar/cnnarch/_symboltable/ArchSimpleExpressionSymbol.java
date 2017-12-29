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
import de.monticore.symboltable.Scope;
import org.jscience.mathematics.number.Rational;

import java.util.*;

public class ArchSimpleExpressionSymbol extends ArchExpressionSymbol implements TextualExpression {

    private MathExpressionSymbol expression;
    private Object value = null;

    public ArchSimpleExpressionSymbol() {
        super();
    }

    public ArchSimpleExpressionSymbol(MathExpressionSymbol expression) {
        this.expression = expression;
    }

    public MathExpressionSymbol getExpression() {
        return expression;
    }

    public void setExpression(MathExpressionSymbol expression) {
        this.expression = expression;
    }

    @Override
    public boolean isSimpleValue() {
        return true;
    }

    @Override
    protected Set<String> computeUnresolvableNames() {
        Set<String> unresolvableNames = new HashSet<>();
        for (MathExpressionSymbol exp : ExpressionHelper.createSubExpressionList(getExpression())){
            if (exp instanceof MathNameExpressionSymbol){
                String name = ((MathNameExpressionSymbol) exp).getNameToAccess();
                Optional<VariableSymbol> variable = getEnclosingScope().resolve(name, VariableSymbol.KIND);
                if (variable.isPresent() && variable.get().hasValue()){
                    unresolvableNames.addAll(variable.get().getExpression().computeUnresolvableNames());
                }
                else {
                    unresolvableNames.add(name);
                }
            }
        }
        return unresolvableNames;
    }

    @Override
    public Optional<Object> getValue() {
        return Optional.ofNullable(value);
    }

    protected void setValue(Object value){
        this.value = value;
        checkIfResolvable();
    }

    @Override
    public Set<String> resolve(Scope resolvingScope) {
        if (!isResolved()){
            checkIfResolvable();
            if (isResolvable()){
                Map<String, String> replacementMap = new HashMap<>();

                for (MathExpressionSymbol exp : ExpressionHelper.createSubExpressionList(getExpression())){
                    if (exp instanceof MathNameExpressionSymbol){
                        String name = ((MathNameExpressionSymbol) exp).getNameToAccess();
                        VariableSymbol variable = (VariableSymbol) resolvingScope.resolve(name, VariableSymbol.KIND).get();
                        variable.getExpression().resolveOrError(variable.getEnclosingScope());

                        replacementMap.put(name, variable.getExpression().getTextualRepresentation());
                    }
                }
                String resolvedString = ExpressionHelper.replace(getExpression().getTextualRepresentation(), replacementMap);
                Object value = Calculator.getInstance().calculate(resolvedString);
                setValue(value);
            }
        }
        return getUnresolvableNames();
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
            return getExpression().getTextualRepresentation();
        }
    }

    @Override
    public Optional<List<List<ArchSimpleExpressionSymbol>>> getElements(){
        return Optional.of(Collections.singletonList(Collections.singletonList(this)));
    }

    @Override
    public boolean isResolved() {
        return value != null;
    }

    public static ArchSimpleExpressionSymbol of(int value){
        MathNumberExpressionSymbol exp = new MathNumberExpressionSymbol(Rational.valueOf(value, 1));
        ArchSimpleExpressionSymbol res = new ArchSimpleExpressionSymbol(exp);
        res.setValue(value);
        return res;
    }

    public static ArchSimpleExpressionSymbol of(Rational value){
        MathNumberExpressionSymbol exp = new MathNumberExpressionSymbol(value);
        ArchSimpleExpressionSymbol res = new ArchSimpleExpressionSymbol(exp);
        res.setValue(value.doubleValue());
        return res;
    }

    public static ArchSimpleExpressionSymbol of(boolean value){
        ArchSimpleExpressionSymbol res;
        MathExpressionSymbol mathOne = new MathNumberExpressionSymbol(Rational.ONE);
        MathExpressionSymbol mathZero = new MathNumberExpressionSymbol(Rational.ZERO);
        if (value){
            res = new ArchSimpleExpressionSymbol(createEqualsExpression(mathZero, mathZero));
        }
        else {
            res = new ArchSimpleExpressionSymbol(createEqualsExpression(mathZero, mathOne));
        }
        res.setValue(value);
        return res;
    }

    /*
        only used to create 'true' and 'false' expression.
        This is necessary because true and false are at the moment just names in the MontiMath SMI.
    */
    private static MathExpressionSymbol createEqualsExpression(MathExpressionSymbol leftExpression, MathExpressionSymbol rightExpression){
        MathCompareExpressionSymbol exp = new MathCompareExpressionSymbol();
        exp.setLeftExpression(leftExpression);
        exp.setRightExpression(rightExpression);
        exp.setCompareOperator("==");
        return exp;
    }

    public static ArchSimpleExpressionSymbol of(int... tupleValues){
        List<MathExpressionSymbol> expList = new ArrayList<>(tupleValues.length);
        for (int tupleValue : tupleValues) {
            MathNumberExpressionSymbol exp = new MathNumberExpressionSymbol(Rational.valueOf(tupleValue, 1));
            expList.add(exp);
        }
        TupleExpressionSymbol tupleExpression = new TupleExpressionSymbol(expList);

        ArchSimpleExpressionSymbol res = new ArchSimpleExpressionSymbol(tupleExpression);
        res.setValue(Arrays.asList(tupleValues));
        return res;
    }

    public static ArchSimpleExpressionSymbol of(VariableSymbol variable){
        MathExpressionSymbol exp = new MathNameExpressionSymbol(variable.getName());
        return new ArchSimpleExpressionSymbol(exp);
    }
}
