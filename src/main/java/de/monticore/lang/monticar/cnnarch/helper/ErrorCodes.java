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

public class ErrorCodes {

    public static final String OUTPUT_UNDEFINED_CODE = "x0601D";
    public static final String OUTPUT_UNDEFINED_MSG = "0" + OUTPUT_UNDEFINED_CODE + " Undefined output. ";
    public static final String OUTPUT_MISSING_CODE = "x0601C";
    public static final String OUTPUT_MISSING_MSG = "0" + OUTPUT_MISSING_CODE + " Missing output. Architecture has to declare at least one output.";
    public static final String OUTPUT_UNUSED_CODE = "x0601E";
    public static final String OUTPUT_UNUSED_MSG = "0" + OUTPUT_UNUSED_CODE + " ";
    public static final String OUTPUT_DUPLICATE_CODE = "x0601B";
    public static final String OUTPUT_DUPLICATE_MSG = "0"+OUTPUT_DUPLICATE_CODE+" ";


    public static final String MISSING_VAR_VALUE_CODE = "x02572";


    public static final String ILLEGAL_ASSIGNMENT_CODE = "x03444";

    public static final String OUTPUT_MISSING_VAR_FC_CODE = "x06068";
    public static final String OUTPUT_MISSING_VAR_FC_MSG = "0" + OUTPUT_MISSING_VAR_FC_CODE +
                        " The output structure has to contain a fullyConnected-Layer without the argument 'units'. " +
                        "This is because the number of outputs is variable for all architectures. " +
                        "Example: output{ fullyConnected() activation.softmax() } -> out";


    public static final String ILLEGAL_SEQUENCE_LENGTH_CODE = "x24772";
    public static final String ILLEGAL_SEQUENCE_LENGTH_MSG = "0" + ILLEGAL_SEQUENCE_LENGTH_CODE + " Illegal sequence length. ";

    public static final String UNKNOWN_IO_CODE = "x32487";
    public static final String UNKNOWN_METHOD_CODE = "x32585";
    public static final String ILLEGAL_NAME_CODE = "x93567";
    public static final String UNKNOWN_ARGUMENT_CODE = "x93527";
    public static final String DUPLICATED_NAME_CODE = "x93569";
    public static final String DUPLICATED_ARG_CODE = "x03031";
    public static final String MISSING_ARGUMENT_CODE = "x06021";
    public static final String RECURSION_ERROR_CODE = "x25833";

}
