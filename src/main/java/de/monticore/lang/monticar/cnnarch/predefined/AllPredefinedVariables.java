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
package de.monticore.lang.monticar.cnnarch.predefined;

import de.monticore.lang.monticar.cnnarch._symboltable.Constraints;
import de.monticore.lang.monticar.cnnarch._symboltable.VariableSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.VariableType;

public class AllPredefinedVariables {

    public static final String CONDITIONAL_ARG_NAME = "?";
    public static final String SERIAL_ARG_NAME = "->";
    public static final String PARALLEL_ARG_NAME = "|";
    public static final String TRUE_NAME = "true";
    public static final String FALSE_NAME = "false";

    public static VariableSymbol createConditionalParameter(){
        return new VariableSymbol.Builder()
                .name(CONDITIONAL_ARG_NAME)
                .type(VariableType.LAYER_PARAMETER)
                .constraints(Constraints.BOOLEAN)
                .defaultValue(true)
                .build();
    }

    public static VariableSymbol createSerialParameter(){
        return new VariableSymbol.Builder()
                .name(SERIAL_ARG_NAME)
                .type(VariableType.LAYER_PARAMETER)
                .constraints(Constraints.INTEGER, Constraints.POSITIVE)
                .defaultValue(1)
                .build();
    }

    public static VariableSymbol createParallelParameter(){
        return new VariableSymbol.Builder()
                .name(PARALLEL_ARG_NAME)
                .type(VariableType.LAYER_PARAMETER)
                .constraints(Constraints.INTEGER, Constraints.POSITIVE)
                .defaultValue(1)
                .build();
    }

    //necessary because true is currently only a name in MontiMath and it needs to be evaluated at compile time for this language
    public static VariableSymbol createTrueConstant(){
        return new VariableSymbol.Builder()
                .name(TRUE_NAME)
                .type(VariableType.CONSTANT)
                .defaultValue(true)
                .build();
    }

    //necessary because false is currently only a name in MontiMath and it needs to be evaluated at compile time for this language
    public static VariableSymbol createFalseConstant() {
        return new VariableSymbol.Builder()
                .name(FALSE_NAME)
                .type(VariableType.CONSTANT)
                .defaultValue(false)
                .build();
    }
}
