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

import de.monticore.lang.math.math._symboltable.expression.MathExpressionSymbol;
import de.se_rwth.commons.logging.Log;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Calculator {

    private static Calculator instance = null;

    private ScriptEngine engine;

    private Calculator() {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByExtension("js");
    }

    public static Calculator getInstance(){
        if (instance == null){
            instance = new Calculator();
        }
        return instance;
    }

    public static void clear(){
        instance = null;
    }


    public Object calculate(String expression){
        try {
            return engine.eval(expression);
        }
        catch (ScriptException e){
            throw new IllegalArgumentException("Calculation error in the expression: " + expression);
        }
    }

}
