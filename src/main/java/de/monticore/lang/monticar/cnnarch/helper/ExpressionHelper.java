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
package de.monticore.lang.monticar.cnnarch.helper;

import de.monticore.lang.math.math._symboltable.expression.*;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchSimpleExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.TupleExpressionSymbol;
import de.se_rwth.commons.logging.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class ExpressionHelper {


    public static List<MathExpressionSymbol> createExpressionList(MathExpressionSymbol expression){
        List<MathExpressionSymbol> list = new LinkedList<>();
        list.add(expression);

        if (expression instanceof MathParenthesisExpressionSymbol){
            MathParenthesisExpressionSymbol exp = (MathParenthesisExpressionSymbol) expression;
            list.addAll(createExpressionList(exp.getMathExpressionSymbol()));
        }
        else if (expression instanceof MathCompareExpressionSymbol){
            MathCompareExpressionSymbol exp = (MathCompareExpressionSymbol) expression;
            list.addAll(createExpressionList(exp.getLeftExpression()));
            list.addAll(createExpressionList(exp.getRightExpression()));
        }
        else if (expression instanceof MathArithmeticExpressionSymbol){
            MathArithmeticExpressionSymbol exp = (MathArithmeticExpressionSymbol) expression;
            list.addAll(createExpressionList(exp.getLeftExpression()));
            list.addAll(createExpressionList(exp.getRightExpression()));
        }
        else if (expression instanceof MathPreOperatorExpressionSymbol){
            MathPreOperatorExpressionSymbol exp = (MathPreOperatorExpressionSymbol) expression;
            list.addAll(createExpressionList(exp.getMathExpressionSymbol()));
        }
        else if (expression instanceof TupleExpressionSymbol){
            TupleExpressionSymbol tuple = (TupleExpressionSymbol) expression;
            for (MathExpressionSymbol exp : tuple.getExpressions()){
                list.addAll(createExpressionList(exp));
            }
        }
        else if (expression instanceof MathValueExpressionSymbol){
            //do nothing
        }
        else {
            throw new IllegalArgumentException("Unknown expression: " + expression.getClass().getSimpleName());
        }

        return list;
    }

    public static void replace(ArchSimpleExpressionSymbol container, Map<MathExpressionSymbol, MathExpressionSymbol> replacementMap){
        if (replacementMap.containsKey(container.getExpression())){
            container.setExpression(replacementMap.get(container.getExpression()));
        }
        else {
            replace(container.getExpression(), replacementMap);
        }
    }

    private static void replace(MathExpressionSymbol expression, Map<MathExpressionSymbol, MathExpressionSymbol> replacementMap){

        if (expression instanceof MathParenthesisExpressionSymbol){
            MathParenthesisExpressionSymbol exp = (MathParenthesisExpressionSymbol) expression;
            if (replacementMap.containsKey(exp.getMathExpressionSymbol())){
                exp.setMathExpressionSymbol(replacementMap.get(expression));
            }
            else{
                replace(exp.getMathExpressionSymbol(), replacementMap);
            }
        }
        else if (expression instanceof MathCompareExpressionSymbol){
            MathCompareExpressionSymbol exp = (MathCompareExpressionSymbol) expression;
            if (replacementMap.containsKey(exp.getLeftExpression())){
                exp.setLeftExpression(replacementMap.get(expression));
            }
            else{
                replace(exp.getLeftExpression(), replacementMap);
            }
            if (replacementMap.containsKey(exp.getRightExpression())){
                exp.setRightExpression(replacementMap.get(expression));
            }
            else{
                replace(exp.getRightExpression(), replacementMap);
            }
        }
        else if (expression instanceof MathArithmeticExpressionSymbol){
            MathArithmeticExpressionSymbol exp = (MathArithmeticExpressionSymbol) expression;
            if (replacementMap.containsKey(exp.getLeftExpression())){
                exp.setLeftExpression(replacementMap.get(expression));
            }
            else{
                replace(exp.getLeftExpression(), replacementMap);
            }
            if (replacementMap.containsKey(exp.getRightExpression())){
                exp.setRightExpression(replacementMap.get(expression));
            }
            else{
                replace(exp.getRightExpression(), replacementMap);
            }
        }
        else if (expression instanceof MathPreOperatorExpressionSymbol){
            MathPreOperatorExpressionSymbol exp = (MathPreOperatorExpressionSymbol) expression;
            if (replacementMap.containsKey(exp.getMathExpressionSymbol())){
                exp.setMathExpressionSymbol(replacementMap.get(expression));
            }
            else{
                replace(exp.getMathExpressionSymbol(), replacementMap);
            }
        }
        else if (expression instanceof TupleExpressionSymbol){
            TupleExpressionSymbol tuple = (TupleExpressionSymbol) expression;
            ListIterator<MathExpressionSymbol> iterator = tuple.getExpressions().listIterator();
            while (iterator.hasNext()){
                MathExpressionSymbol exp = iterator.next();
                if (replacementMap.containsKey(exp)){
                    iterator.set(replacementMap.get(exp));
                }
                else {
                    replace(exp, replacementMap);
                }
            }
        }
        else if (expression instanceof MathValueExpressionSymbol){
            //do nothing
        }
        else {
            throw new IllegalArgumentException("Unknown expression: " + expression.getClass().getSimpleName());
        }
    }

}
