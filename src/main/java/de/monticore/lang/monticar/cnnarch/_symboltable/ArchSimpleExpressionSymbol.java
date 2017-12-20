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
import org.jscience.mathematics.number.Rational;

import java.util.*;

public class ArchSimpleExpressionSymbol extends ArchExpressionSymbol implements TextualExpression {

    private static final MathExpressionSymbol MATH_ONE = new MathNumberExpressionSymbol(Rational.ONE);
    private static final MathExpressionSymbol MATH_ZERO = new MathNumberExpressionSymbol(Rational.ZERO);
    private static final MathExpressionSymbol MATH_TRUE = getEqualsOneExpression(MATH_ONE);
    private static final MathExpressionSymbol MATH_FALSE = getEqualsOneExpression(MATH_ZERO);
    public static final ArchSimpleExpressionSymbol ONE = new ArchSimpleExpressionSymbol(MATH_ONE);
    public static final ArchSimpleExpressionSymbol ZERO = new ArchSimpleExpressionSymbol(MATH_ZERO);
    public static final ArchSimpleExpressionSymbol TRUE = new ArchSimpleExpressionSymbol(MATH_TRUE);
    public static final ArchSimpleExpressionSymbol FALSE = new ArchSimpleExpressionSymbol(MATH_FALSE);

    private MathExpressionSymbol expression;

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
    public boolean isBoolean(){
        return (getExpression().getRealMathExpressionSymbol() instanceof MathCompareExpressionSymbol);
    }

    @Override
    public boolean isNumber(){
        return (getExpression().getRealMathExpressionSymbol() instanceof MathArithmeticExpressionSymbol);
    }

    @Override
    public boolean isTuple(){
        return (getExpression().getRealMathExpressionSymbol() instanceof TupleExpressionSymbol);
    }

    @Override
    public Optional<Boolean> isInt(){
        Optional<Boolean> isInt = Optional.empty();
        if (isNumber()){
            Optional<Object> optValue = getValue();
            if (optValue.isPresent()){
                if (optValue.get() instanceof Integer){
                    isInt = Optional.of(true);
                }
                else {
                    isInt = Optional.of(false);
                }
            }
        }
        return isInt;
    }

    @Override
    public boolean isSimpleValue() {
        return true;
    }

    @Override
    public Optional<Object> getValue() {
        if (isFullyResolved()){
            Object value = Calculator.getInstance().calculate(getExpression());
            return Optional.of(value);
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public Set<String> resolve() {
        Set<String> unresolvableSet = new HashSet<>();
        Map<MathExpressionSymbol, MathExpressionSymbol> replacementMap = new HashMap<>();

        if (!isFullyResolved()) {
            for (MathExpressionSymbol exp : ExpressionHelper.createExpressionList(getExpression())) {
                if (exp instanceof MathNameExpressionSymbol) {
                    String name = ((MathNameExpressionSymbol) exp).getNameToAccess();
                    Optional<VariableSymbol> var = getEnclosingScope().resolve(name, VariableSymbol.KIND);
                    if (var.isPresent() && var.get().hasValueSymbol()) {
                        ArchSimpleExpressionSymbol resolventSymbol = (ArchSimpleExpressionSymbol) var.get().getValueSymbol();
                        unresolvableSet.addAll(resolventSymbol.resolve());
                        replacementMap.put(exp, resolventSymbol.getExpression());
                    } else {
                        unresolvableSet.add(name);
                    }
                }
            }

            if (!replacementMap.isEmpty()){
                ExpressionHelper.replace(this, replacementMap);
            }

            if (unresolvableSet.isEmpty()) {
                setFullyResolved(true);
            }
        }
        return unresolvableSet;
    }

    @Override
    protected void checkIfResolved() {
        boolean isResolved = true;
        for (MathExpressionSymbol exp : ExpressionHelper.createExpressionList(getExpression())){
            if (exp instanceof MathNameExpressionSymbol){
                MathNameExpressionSymbol nameExpression = (MathNameExpressionSymbol) exp;
                if (!nameExpression.getNameToAccess().equals("true") && !nameExpression.getNameToAccess().equals("false")){
                    isResolved = false;
                }
            }
        }
        setFullyResolved(isResolved);
    }

    @Override
    public String getTextualRepresentation() {
        return getExpression().getTextualRepresentation();
    }



    public static ArchSimpleExpressionSymbol of(int value){
        MathNumberExpressionSymbol exp = new MathNumberExpressionSymbol(Rational.valueOf(value, 1));
        return new ArchSimpleExpressionSymbol(exp);
    }

    public static ArchSimpleExpressionSymbol of(Rational value){
        MathNumberExpressionSymbol exp = new MathNumberExpressionSymbol(value);
        return new ArchSimpleExpressionSymbol(exp);
    }

    public static ArchSimpleExpressionSymbol of(boolean value){
        if (value){
            return TRUE;
        }
        else {
            return FALSE;
        }
    }

    public static ArchSimpleExpressionSymbol of(int... tupleValues){
        List<MathExpressionSymbol> expList = new ArrayList<>(tupleValues.length);
        for (int tupleValue : tupleValues) {
            MathNumberExpressionSymbol exp = new MathNumberExpressionSymbol(Rational.valueOf(tupleValue, 1));
            expList.add(exp);
        }
        TupleExpressionSymbol tupleExpression = new TupleExpressionSymbol(expList);
        return new ArchSimpleExpressionSymbol(tupleExpression);
    }

    /*
        only used to create 'true' and 'false' expression.
        This is necessary because true and false are at the moment just names in the MontiMath SMI.
    */
    private static MathExpressionSymbol getEqualsOneExpression(MathExpressionSymbol leftExpression){
        MathCompareExpressionSymbol exp = new MathCompareExpressionSymbol();
        exp.setLeftExpression(leftExpression);
        exp.setRightExpression(MATH_ONE);
        exp.setCompareOperator("==");
        return exp;
    }
}
