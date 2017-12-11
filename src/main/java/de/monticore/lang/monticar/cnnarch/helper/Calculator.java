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


    public Object calculate(MathExpressionSymbol expression){
        Object obj = null;
        try {
            obj = engine.eval(expression.getTextualRepresentation());
        }
        catch (ScriptException e){
            Log.error("Calculation error in the arithmetic expression: " + expression.getTextualRepresentation()
                    , expression.getSourcePosition());
        }
        return obj;
    }

}
