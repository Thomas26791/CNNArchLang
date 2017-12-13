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

public class ErrorMessages {

    public static final String OUTPUT_UNDEFINED_CODE = "x0601D";
    public static final String OUTPUT_UNDEFINED_MSG = "0" + OUTPUT_UNDEFINED_CODE + " Undefined output. ";
    public static final String OUTPUT_MISSING_CODE = "x0601C";
    public static final String OUTPUT_MISSING_MSG = "0" + OUTPUT_MISSING_CODE + " Missing output. Architecture has to declare at least one output.";
    public static final String OUTPUT_UNUSED_CODE = "x0601E";
    public static final String OUTPUT_UNUSED_MSG = "0" + OUTPUT_UNUSED_CODE + " ";
    public static final String OUTPUT_DUPLICATE_CODE = "x0601B";
    public static final String OUTPUT_DUPLICATE_MSG = "0"+OUTPUT_DUPLICATE_CODE+" ";

    public static final String MISSING_ARG_CODE = "x06021";
    public static final String MISSING_ARG_MSG = "0"+MISSING_ARG_CODE+" Missing argument. ";

    public static final String PLACEHOLDER_ARG_CODE = "x0303B";
    public static final String PLACEHOLDER_ARG_MSG = "0"+PLACEHOLDER_ARG_CODE+" \"_placeholder\" is not an argument. ";
    public static final String DUPLICATE_ARG_CODE = "x03031";
    public static final String DUPLICATE_ARG_MSG = "0"+DUPLICATE_ARG_CODE+" Multiple assignments of the same argument are not allowed. ";

    public static final String MISSING_VAR_VALUE_CODE = "x02572";


    public static final String INCORRECT_ARG_TYPE_CODE = "x03444";
    public static final String INCORRECT_ARG_TYPE_MSG = "0"+INCORRECT_ARG_TYPE_CODE+" Incorrect argument type. ";

    public static final String OUTPUT_MISSING_VAR_FC_CODE = "x06068";
    public static final String OUTPUT_MISSING_VAR_FC_MSG = "0" + OUTPUT_MISSING_VAR_FC_CODE +
                        " The output structure has to contain a fullyConnected-Layer without the argument 'units'. " +
                        "This is because the number of outputs is variable for all architectures. " +
                        "Example: output{ fullyConnected() activation.softmax() } -> out";



}
