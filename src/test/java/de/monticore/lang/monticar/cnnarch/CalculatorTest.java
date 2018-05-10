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
package de.monticore.lang.monticar.cnnarch;

import de.monticore.lang.math.math._symboltable.expression.MathArithmeticExpressionSymbol;
import de.monticore.lang.math.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.math.math._symboltable.expression.MathPreOperatorExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchSimpleExpressionSymbol;
import de.monticore.lang.monticar.cnnarch.helper.Calculator;
import de.monticore.lang.numberunit.Rationals;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import static de.monticore.lang.monticar.cnnarch.ParserTest.ENABLE_FAIL_QUICK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CalculatorTest {

    @Before
    public void setUp() {
        // ensure an empty log
        Log.getFindings().clear();
        Log.enableFailQuick(ENABLE_FAIL_QUICK);
    }

    @Test
    public void TestArchExpressionResolve1(){
        MathArithmeticExpressionSymbol mathExp = new MathArithmeticExpressionSymbol();
        mathExp.setLeftExpression(new MathNumberExpressionSymbol(Rationals.doubleToRational(1.0)));
        MathPreOperatorExpressionSymbol preMinus = new MathPreOperatorExpressionSymbol();
        preMinus.setOperator("-");
        preMinus.setMathExpressionSymbol(new MathNumberExpressionSymbol(Rationals.doubleToRational(3.0)));
        mathExp.setRightExpression(preMinus);
        mathExp.setOperator("+");
        ArchSimpleExpressionSymbol exp = ArchSimpleExpressionSymbol.of(mathExp);
        exp.resolve();
        Object result = exp.getIntValue().get();
        assertEquals(-2, ((Integer) result).intValue());
    }

    @Test
    public void TestArchExpressionResolve2(){
        MathArithmeticExpressionSymbol mathExp = new MathArithmeticExpressionSymbol();
        mathExp.setLeftExpression(new MathNumberExpressionSymbol(Rationals.doubleToRational(2.0)));
        mathExp.setRightExpression(new MathNumberExpressionSymbol(Rationals.doubleToRational(3.4)));
        mathExp.setOperator("*");
        ArchSimpleExpressionSymbol exp = ArchSimpleExpressionSymbol.of(mathExp);
        exp.resolve();
        double result = exp.getDoubleValue().get();
        assertEquals(6.8, (Double) result, 0.00000001);
    }

    @Test
    public void TestArchExpressionResolve3(){
        MathArithmeticExpressionSymbol mathExp = new MathArithmeticExpressionSymbol();
        mathExp.setLeftExpression(new MathNumberExpressionSymbol(Rationals.doubleToRational(2.0)));
        mathExp.setRightExpression(new MathNumberExpressionSymbol(Rationals.doubleToRational(3.0)));
        mathExp.setOperator("*");
        ArchSimpleExpressionSymbol exp = ArchSimpleExpressionSymbol.of(mathExp);
        exp.resolve();
        int result = exp.getIntValue().get();
        assertEquals(6, result, 0.00000001);
    }

    @Test
    public void TestArchExpressionResolve4(){
        MathArithmeticExpressionSymbol mathExp = new MathArithmeticExpressionSymbol();
        mathExp.setLeftExpression(new MathNumberExpressionSymbol(Rationals.doubleToRational(2.0)));
        mathExp.setRightExpression(new MathNumberExpressionSymbol(Rationals.doubleToRational(3.0)));
        mathExp.setOperator("*");
        ArchSimpleExpressionSymbol exp = ArchSimpleExpressionSymbol.of(mathExp);
        exp.resolve();
        double result = exp.getDoubleValue().get();
        assertEquals(6.0, result, 0.00000001);
    }

    @Test
    public void TestAddition1(){
        Object result = Calculator.getInstance().calculate("3 + 1.5");
        assertTrue(result instanceof Double);
        assertEquals(4.5, (Double) result, 0.00000001);
    }

    @Test
    public void TestMultiplication1(){
        Object result = Calculator.getInstance().calculate("2 + 3 * 5 + 1");
        assertTrue(result instanceof Integer);
        assertEquals(18, ((Integer) result).intValue());
    }

    @Test
    public void TestMultiplication2(){
        Object result = Calculator.getInstance().calculate("(2 + 3) * 5 + 1");
        assertTrue(result instanceof Integer);
        assertEquals(26, ((Integer) result).intValue());
    }

    @Test
    public void TestMultiplication3(){
        Object result = Calculator.getInstance().calculate("3 * -6");
        assertTrue(result instanceof Integer);
        assertEquals(-18, ((Integer) result).intValue());
    }

    @Test
    public void TestSubtraction1(){
        Object result = Calculator.getInstance().calculate("5 - 1");
        assertTrue(result instanceof Integer);
        assertEquals(4, ((Integer) result).intValue());
    }

    @Test
    public void TestDivision1(){
        Object result = Calculator.getInstance().calculate("1+5 / 2");
        assertTrue(result instanceof Double);
        assertEquals(3.5, (Double) result, 0.00000001);
    }

    @Test
    public void TestDivision2(){
        Object result = Calculator.getInstance().calculate("4 / -2");
        assertTrue(result instanceof Integer);
        assertEquals(-2, ((Integer) result).intValue());
    }

    @Test
    public void TestAND1(){
        Object result = Calculator.getInstance().calculate("true && false");
        assertTrue(result instanceof Boolean);
        assertEquals(false, result);
    }

    @Test
    public void TestOR1(){
        Object result = Calculator.getInstance().calculate("true || false");
        assertTrue(result instanceof Boolean);
        assertEquals(true, result);
    }

    @Test
    public void TestNegation1(){
        Object result = Calculator.getInstance().calculate("!(true || false)");
        assertTrue(result instanceof Boolean);
        assertEquals(false, result);
    }

    @Test
    public void TestLogicalExpression(){
        Object result = Calculator.getInstance().calculate("true || !false && false");
        assertTrue(result instanceof Boolean);
        assertEquals(true, result);
    }

    @Test
    public void TestEqual(){
        Object result = Calculator.getInstance().calculate("3 == 3 + 1");
        assertTrue(result instanceof Boolean);
        assertEquals(false, result);
    }

    @Test
    public void TestUnEqual(){
        Object result = Calculator.getInstance().calculate("3 + 1!= 4");
        assertTrue(result instanceof Boolean);
        assertEquals(false, result);
    }

    @Test
    public void TestLesserThan(){
        Object result = Calculator.getInstance().calculate("4 < 8 - 5");
        assertTrue(result instanceof Boolean);
        assertEquals(false, result);
    }

    @Test
    public void TestLesserOrEqualThan1(){
        Object result = Calculator.getInstance().calculate("4 <= 8 - 4");
        assertTrue(result instanceof Boolean);
        assertEquals(true, result);
    }

    @Test
    public void TestLesserOrEqualThan2(){
        Object result = Calculator.getInstance().calculate("4 <= 8 - 5");
        assertTrue(result instanceof Boolean);
        assertEquals(false, result);
    }

    @Test
    public void TestGreaterThan(){
        Object result = Calculator.getInstance().calculate("4 > 8 - 5");
        assertTrue(result instanceof Boolean);
        assertEquals(true, result);
    }

    @Test
    public void TestGreaterOrEqualThan1(){
        Object result = Calculator.getInstance().calculate("4 >= 8 - 4");
        assertTrue(result instanceof Boolean);
        assertEquals(true, result);
    }

    @Test
    public void TestGreaterOrEqualThan2(){
        Object result = Calculator.getInstance().calculate("4 >= 8 - 3");
        assertTrue(result instanceof Boolean);
        assertEquals(false, result);
    }

}
