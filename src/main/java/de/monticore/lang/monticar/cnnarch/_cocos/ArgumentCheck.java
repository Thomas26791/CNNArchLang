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

import de.monticore.lang.monticar.cnnarch._ast.*;
import de.se_rwth.commons.logging.Log;
import org.jscience.mathematics.number.Rational;
import siunit.monticoresiunit.si._ast.ASTNumber;
import siunit.monticoresiunit.si._ast.ASTUnitNumber;

import javax.measure.unit.Unit;
import java.util.*;

public class ArgumentCheck implements CNNArchASTMethodCoCo {

    @Override
    public void check(ASTMethod node) {
        checkDuplicateArgument(node);
        checkPlaceholderArgument(node);
        checkArgumentType(node);
    }

    public void checkDuplicateArgument(ASTMethod node){
        Set<Enum> set = new HashSet<>();
        for (ASTArgumentAssignment assignment : node.getArgumentListing().getArguments()) {
            if (set.contains(assignment.getLhs())) {
                Log.error("0x03011 Multiple assignments of the same argument are not allowed",
                        assignment.get_SourcePositionStart());
            }
            else {
                set.add(assignment.getLhs());
            }
        }
    }

    public void checkPlaceholderArgument(ASTMethod node){
        for(ASTArgumentAssignment assignment:node.getArgumentListing().getArguments()){
            if (assignment.getLhs().name().equals("_placeholder")){
                Log.error("0x0301B \"_placeholder\" is not an argument",
                        assignment.get_SourcePositionStart());
            }
        }
    }

    public void checkRequiredArguments(ASTMethod node){

    }

    public void checkArgumentType(ASTMethod node){
        final String msg = "0x03424 Incorrect argument type. ";
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
                Log.error(msg + msgAddon);
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
