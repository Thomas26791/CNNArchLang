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

import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;

import static de.monticore.lang.monticar.cnnarch.helper.ErrorCodes.ILLEGAL_ASSIGNMENT_CODE;

public enum Constraints {
    NUMBER {
        @Override
        public boolean isValid(ArchSimpleExpressionSymbol exp) {
            return exp.isNumber();
        }
        @Override
        public String msgString() {
            return "a number";
        }
    },
    INTEGER {
        @Override
        public boolean isValid(ArchSimpleExpressionSymbol exp) {
            return exp.isInt().get();
        }
        @Override
        public String msgString() {
            return "an integer";
        }
    },
    BOOLEAN {
        @Override
        public boolean isValid(ArchSimpleExpressionSymbol exp) {
            return exp.isBoolean();
        }
        @Override
        public String msgString() {
            return "a boolean";
        }
    },
    TUPLE {
        @Override
        public boolean isValid(ArchSimpleExpressionSymbol exp) {
            return exp.isTuple();
        }
        @Override
        public String msgString() {
            return "a tuple";
        }
    },
    INTEGER_TUPLE {
        @Override
        public boolean isValid(ArchSimpleExpressionSymbol exp) {
            return exp.isIntTuple().get();
        }
        @Override
        public String msgString() {
            return "a tuple of integers";
        }
    },
    POSITIVE {
        @Override
        public boolean isValid(ArchSimpleExpressionSymbol exp) {
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
        public boolean isValid(ArchSimpleExpressionSymbol exp) {
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
        public boolean isValid(ArchSimpleExpressionSymbol exp) {
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
    },
    PADDING_TYPE {
        @Override
        public boolean isValid(ArchSimpleExpressionSymbol exp) {
            Optional<String> optString= exp.getStringValue();
            if (optString.isPresent()){
                if (optString.get().equals(PredefinedMethods.PADDING_VALID) || optString.get().equals(PredefinedMethods.PADDING_SAME)){
                    return true;
                }
            }
            return false;
        }

        @Override
        protected String msgString() {
            return PredefinedMethods.PADDING_VALID + " or " + PredefinedMethods.PADDING_SAME;
        }
    };

    protected abstract boolean isValid(ArchSimpleExpressionSymbol exp);

    abstract protected String msgString();

    public static boolean check(VariableSymbol variable){
        boolean valid = true;
        for (Constraints constraint : variable.getConstraints()) {
            valid = valid &&
                    constraint.check(variable.getExpression(), variable.getSourcePosition(), variable.getName());
        }
        return valid;
    }

    public static boolean check(ArgumentSymbol argument){
        boolean valid = true;
        VariableSymbol variable = argument.getParameter();
        for (Constraints constraint : variable.getConstraints()) {
            valid = valid &&
                    constraint.check(argument.getRhs(), argument.getSourcePosition(), variable.getName());
        }
        return valid;
    }

    public boolean check(ArchExpressionSymbol exp, SourcePosition sourcePosition, String name){
        if (exp instanceof ArchRangeExpressionSymbol){
            ArchRangeExpressionSymbol range = (ArchRangeExpressionSymbol)exp;
            if (!INTEGER.check(range.getStartSymbol(), sourcePosition, name)
                    || !INTEGER.check(range.getEndSymbol(), sourcePosition, name)){
                return false;
            }
        }
        for (List<ArchSimpleExpressionSymbol> expList : exp.getElements().get()) {
            for (ArchSimpleExpressionSymbol singleExp : expList) {
                if (!isValid(singleExp)) {
                    Log.error("0" + ILLEGAL_ASSIGNMENT_CODE + " Illegal assignment of '" + name + "'. " +
                                    "Expression must be " + msgString() + "."
                            , sourcePosition);
                    return false;
                }
            }
        }
        return true;
    }

}
