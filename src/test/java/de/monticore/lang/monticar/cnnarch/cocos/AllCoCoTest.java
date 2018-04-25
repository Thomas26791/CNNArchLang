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
package de.monticore.lang.monticar.cnnarch.cocos;

import de.monticore.lang.monticar.cnnarch._cocos.*;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static de.monticore.lang.monticar.cnnarch.ParserTest.ENABLE_FAIL_QUICK;

public class AllCoCoTest extends AbstractCoCoTest {
    String baseDir="src/test/resources";

    public AllCoCoTest() {
        Log.enableFailQuick(false);
    }

    @Before
    public void setUp() {
        // ensure an empty log
        Log.getFindings().clear();
        Log.enableFailQuick(ENABLE_FAIL_QUICK);
    }

    @Test
    public void testValidCoCos() throws IOException {

        checkValid("architectures", "ResNet152");
        checkValid("architectures", "Alexnet");
        checkValid("architectures", "ResNeXt50");
        checkValid("architectures", "ResNet34");
        checkValid("architectures", "SequentialAlexnet");
        checkValid("architectures", "ThreeInputCNN_M14");
        checkValid("architectures", "VGG16");

        checkValid("valid_tests", "ArgumentSequenceTest");
        checkValid("valid_tests", "Fixed_Alexnet");
        checkValid("valid_tests", "Fixed_ThreeInputCNN_M14");
        checkValid("valid_tests", "ThreeInputCNN_M14_alternative");
        checkValid("valid_tests", "Alexnet_alt");
        checkValid("valid_tests", "SimpleNetworkSoftmax");
        checkValid("valid_tests", "SimpleNetworkSigmoid");
        checkValid("valid_tests", "SimpleNetworkLinear");
        checkValid("valid_tests", "SimpleNetworkRelu");
        checkValid("valid_tests", "SimpleNetworkTanh");
        checkValid("valid_tests", "ResNeXt50_alt");
        checkValid("valid_tests", "Alexnet_alt2");
        checkValid("valid_tests", "MultipleOutputs");

    }

    @Test
    public void testInvalidPreResolveCocos(){
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckIOName()),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "IllegalIOName",
                new ExpectedErrorInfo(2, ErrorCodes.ILLEGAL_NAME));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckMethodLayer()),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "UnknownMethod",
                new ExpectedErrorInfo(1, ErrorCodes.UNKNOWN_METHOD));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckIOName()).addCoCo(new CheckVariableName()).addCoCo(new CheckMethodName()),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "DuplicatedNames",
                new ExpectedErrorInfo(3, ErrorCodes.DUPLICATED_NAME));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckNameExpression()),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "UnknownVariableName",
                new ExpectedErrorInfo(1, ErrorCodes.UNKNOWN_VARIABLE_NAME));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckIOName()),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "UnknownIO",
                new ExpectedErrorInfo(2, ErrorCodes.UNKNOWN_IO));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckMethodLayer()),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "DuplicatedArgument",
                new ExpectedErrorInfo(1, ErrorCodes.DUPLICATED_ARG));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckArgument()).addCoCo(new CheckMethodLayer()),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "WrongArgument",
                new ExpectedErrorInfo(4, ErrorCodes.UNKNOWN_ARGUMENT, ErrorCodes.MISSING_ARGUMENT));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckMethodRecursion()),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "InvalidRecursion",
                new ExpectedErrorInfo(1, ErrorCodes.RECURSION_ERROR));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "ArgumentConstraintTest1",
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "ArgumentConstraintTest2",
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckRangeOperators()),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "WrongRangeOperator",
                new ExpectedErrorInfo(2, ErrorCodes.DIFFERENT_RANGE_OPERATORS));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "ArgumentConstraintTest3",
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "ArgumentConstraintTest4",
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "ArgumentConstraintTest5",
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "ArgumentConstraintTest6",
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckMethodLayer()),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "MissingArgument",
                new ExpectedErrorInfo(3, ErrorCodes.MISSING_ARGUMENT));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckVariableName()).addCoCo(new CheckMethodName()),
                new CNNArchExtendedCoCoChecker(),
                "invalid_tests", "IllegalName",
                new ExpectedErrorInfo(2, ErrorCodes.ILLEGAL_NAME));
    }

    @Test
    public void testInvalidPostResolveCocos(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker().addCoCo(new CheckArchitectureFinished()),
                "invalid_tests", "UnfinishedArchitecture",
                new ExpectedErrorInfo(1, ErrorCodes.UNFINISHED_ARCHITECTURE));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker().addCoCo(new CheckLayerInputs()),
                "invalid_tests", "InvalidInputShape",
                new ExpectedErrorInfo(2, ErrorCodes.INVALID_LAYER_INPUT_SHAPE));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker().addCoCo(new CheckLayerInputs()),
                "invalid_tests", "WrongIOType",
                new ExpectedErrorInfo(1, ErrorCodes.INVALID_LAYER_INPUT_TYPE));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker().addCoCo(new CheckIOType()),
                "invalid_tests", "InvalidIOShape1",
                new ExpectedErrorInfo(2, ErrorCodes.INVALID_IO_TYPE));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker().addCoCo(new CheckIOType()),
                "invalid_tests", "InvalidIOShape2",
                new ExpectedErrorInfo(2, ErrorCodes.INVALID_IO_TYPE));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker().addCoCo(new CheckIOAccessAndIOMissing()),
                "invalid_tests", "NotIOArray",
                new ExpectedErrorInfo(2, ErrorCodes.INVALID_ARRAY_ACCESS));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker().addCoCo(new CheckIOAccessAndIOMissing()),
                "invalid_tests", "MissingIO2",
                new ExpectedErrorInfo(2, ErrorCodes.MISSING_IO));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker().addCoCo(new CheckIOAccessAndIOMissing()),
                "invalid_tests", "InvalidArrayAccessValue",
                new ExpectedErrorInfo(1, ErrorCodes.INVALID_ARRAY_ACCESS));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchExtendedCoCoChecker().addCoCo(new CheckLayerInputs()),
                "invalid_tests", "MissingMerge",
                new ExpectedErrorInfo(2, ErrorCodes.MISSING_MERGE));
    }

}