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
package de.monticore.lang.monticar.cnnarch._cocos;

import de.monticore.lang.monticar.cnnarch._ast.ASTArgumentAssignment;
import de.monticore.lang.monticar.cnnarch._ast.ASTArgumentRhs;
import de.monticore.lang.monticar.cnnarch._ast.ASTMethod;
import de.monticore.lang.monticar.cnnarch._ast.ASTTuple;
import de.se_rwth.commons.logging.Log;
import org.jscience.mathematics.number.Rational;
import siunit.monticoresiunit.si._ast.ASTNumber;
import siunit.monticoresiunit.si._ast.ASTUnitNumber;

import javax.measure.unit.Unit;

public class ArgumentTypeCheck implements CNNArchASTMethodCoCo {

    public static final String INCORRECT_ARG_TYPE_CODE = "x03444";
    public static final String INCORRECT_ARG_TYPE_MSG = "0"+INCORRECT_ARG_TYPE_CODE+" Incorrect argument type. ";


    public void check(ASTMethod node){
        String msgAddon;

        for(ASTArgumentAssignment assignment:node.getArgumentListing().getArguments()){
            boolean isValid = false;
            ASTArgumentRhs rhs = assignment.getRhs();

            switch(assignment.getLhs().name()){
                case "UNITS":
                    isValid = checkInt(rhs);
                    msgAddon = "Argument 'units' has to be an integer.";
                    break;
                case "NOBIAS":
                    isValid = checkBool(rhs);
                    msgAddon = "Argument 'no_bias' has to be a boolean.";
                    break;
                case "FILTERS":
                    msgAddon = "Argument 'filters' has to be an integer.";
                    isValid = checkInt(rhs);
                    break;
                case "KERNEL":
                    isValid = checkInt(rhs) || checkIntTuple(rhs);
                    msgAddon = "Argument 'kernel' has to be an integer or an integer-tuple.";
                    break;
                case "STRIDE":
                    isValid = checkInt(rhs) || checkIntTuple(rhs);
                    msgAddon = "Argument 'stride' has to be an integer or an integer-tuple.";
                    break;
                case "GLOBAL":
                    isValid = checkBool(rhs);
                    msgAddon = "Argument 'global' has to be a boolean.";
                    break;
                case "P":
                    isValid = checkFloat(rhs) && checkBetweenOneAndZero(rhs);
                    msgAddon = "Argument 'p' has to be a float between 0 and 1.";
                    break;
                case "NSIZE":
                    isValid = checkInt(rhs);
                    msgAddon = "Argument 'nsize' has to be an integer.";
                    break;
                case "KNORM":
                    isValid = checkFloat(rhs);
                    msgAddon = "Argument 'knorm' has to be a float.";
                    break;
                case "ALPHA":
                    isValid = checkFloat(rhs);
                    msgAddon = "Argument 'alpha' has to be a float.";
                    break;
                case "BETA":
                    isValid = checkFloat(rhs);
                    msgAddon = "Argument 'beta' has to be a float.";
                    break;
                default:
                    msgAddon = "Argument " +assignment.getLhs().toString().toLowerCase()+ " is unknwown (see ArgumentCheck.java).";
            }

            if (!isValid){
                Log.error(INCORRECT_ARG_TYPE_MSG + msgAddon, rhs.get_SourcePositionStart());
            }
        }
    }


    private boolean checkFloat(ASTNumber number){
        boolean res = false;
        if (number.getUnitNumber().isPresent()){
            ASTUnitNumber unitNum = number.getUnitNumber().get();
            if(!unitNum.getInfSign().isPresent()
                    && unitNum.getNumber().isPresent()
                    &&(!unitNum.getUnit().isPresent() || unitNum.getUnit().get().equals(Unit.ONE))){
                res = true;
            }
        }
        return res;
    }

    private boolean checkInt(ASTNumber number){
        return checkFloat(number)
                && number.getUnitNumber().get().getNumber().get().getDivisor().equals(1);
    }

    private boolean checkBool(ASTArgumentRhs rhs){
        return rhs.getBooleanVal().isPresent();
    }

    private boolean checkFloat(ASTArgumentRhs rhs){
        boolean res = false;
        if (rhs.getNumber().isPresent()){
            ASTNumber number = rhs.getNumber().get();
            res = checkFloat(number);
        }
        return res;
    }

    private boolean checkInt(ASTArgumentRhs rhs){
        boolean res = false;
        if (rhs.getNumber().isPresent()){
            ASTNumber number = rhs.getNumber().get();
            res = checkInt(number);
        }
        return res;
    }

    private boolean checkIntTuple(ASTArgumentRhs rhs){
        boolean res = false;
        if(rhs.getTuple().isPresent()){
            ASTTuple tuple = rhs.getTuple().get();
            res = true;
            for (ASTNumber value : tuple.getValues()){
                res = res && checkInt(value);
            }
        }
        return res;
    }

    private boolean checkBetweenOneAndZero(ASTArgumentRhs rhs){
        boolean isValid = checkFloat(rhs);
        if (isValid){
            boolean isUnitNum = rhs.getNumber().get().getUnitNumber().isPresent();

            if (isUnitNum){
                Rational rat = rhs.getNumber().get().getUnitNumber().get().getNumber().get();
                isValid = (rat.isGreaterThan(Rational.ZERO) && rat.isLessThan(Rational.ONE))
                        || rat.equals(Rational.ONE) || rat.equals(Rational.ZERO);
            }
        }
        return isValid;
    }

}
