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

import de.monticore.lang.monticar.cnnarch._symboltable.ArchSimpleExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArgumentSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.VariableSymbol;
import de.se_rwth.commons.logging.Log;

import java.util.List;

import static de.monticore.lang.monticar.cnnarch.ErrorMessages.ILLEGAL_ASSIGNMENT_CODE;

public enum Constraint {
    NUMBER {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            return exp.isNumber();
        }
        @Override
        public String msgString() {
            return "a number";
        }
    },
    INTEGER {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            return exp.isInt().get();
        }
        @Override
        public String msgString() {
            return "an integer";
        }
    },
    BOOLEAN {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            return exp.isBoolean();
        }
        @Override
        public String msgString() {
            return "a boolean";
        }
    },
    TUPLE {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            return exp.isTuple();
        }
        @Override
        public String msgString() {
            return "a tuple";
        }
    },
    INTEGER_TUPLE {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            return exp.isIntTuple().get();
        }
        @Override
        public String msgString() {
            return "a tuple of integers";
        }
    },
    POSITIVE {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            if (exp.getDoubleValue().isPresent()){
                return exp.getDoubleValue().get() > 0;
            }
            else if (exp.getDoubleTupleValues().isPresent()){
                boolean isPositive = true;
                for (double value : exp.getDoubleTupleValues().get()){
                    if (value <= 0){
                        isPositive = false;
                    }
                }
                return isPositive;
            }
            return false;
        }
        @Override
        public String msgString() {
            return "a positive number";
        }
    },
    NON_NEGATIVE {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            if (exp.getDoubleValue().isPresent()){
                return exp.getDoubleValue().get() >= 0;
            }
            else if (exp.getDoubleTupleValues().isPresent()){
                boolean isPositive = true;
                for (double value : exp.getDoubleTupleValues().get()){
                    if (value < 0){
                        isPositive = false;
                    }
                }
                return isPositive;
            }
            return false;
        }
        @Override
        public String msgString() {
            return "a non-negative number";
        }
    },
    BETWEEN_ZERO_AND_ONE {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            if (exp.getDoubleValue().isPresent()){
                return exp.getDoubleValue().get() >= 0 && exp.getDoubleValue().get() <= 1;
            }
            else if (exp.getDoubleTupleValues().isPresent()){
                boolean isPositive = true;
                for (double value : exp.getDoubleTupleValues().get()){
                    if (value < 0 || value > 1){
                        isPositive = false;
                    }
                }
                return isPositive;
            }
            return false;
        }
        @Override
        public String msgString() {
            return "between one and zero";
        }
    };

    abstract public boolean check(ArchSimpleExpressionSymbol exp);

    abstract protected String msgString();

    public static void check(VariableSymbol variable){
        for (Constraint constraint : variable.getConstraints()) {
            if (!constraint.check(variable.getExpression())){
                Log.error("0"+ ILLEGAL_ASSIGNMENT_CODE +" Illegal assignment. The variable '"
                                + variable.getName()  +"' must be " + constraint.msgString() + "."
                        , variable.getSourcePosition());
            }
        }
    }

    public static void check(ArgumentSymbol argument){
        VariableSymbol variable = argument.getParameter();
        for (List<ArchSimpleExpressionSymbol> expList : argument.getRhs().getElements().get()){
            for (ArchSimpleExpressionSymbol exp : expList){
                for (Constraint constraint : variable.getConstraints()) {
                    if (!constraint.check(exp)){
                        Log.error("0"+ ILLEGAL_ASSIGNMENT_CODE +" Illegal assignment. This parameter '"
                                        + variable.getName()  +"' must be " + constraint.msgString() + "."
                                , argument.getSourcePosition());
                    }
                }
            }
        }
    }
}
