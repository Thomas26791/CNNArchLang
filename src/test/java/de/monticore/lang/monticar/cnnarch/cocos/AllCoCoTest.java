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
import org.junit.Test;

import java.io.IOException;

public class AllCoCoTest extends AbstractCoCoTest {
    String baseDir="src/test/resources";

    public AllCoCoTest() {
        Log.enableFailQuick(false);
    }

    @Test
    public void testValidCoCos() throws IOException {

        checkValid("architectures", "Alexnet");
        checkValid("architectures", "ResNeXt50");
        checkValid("architectures", "ResNet34");
        checkValid("architectures", "SequentialAlexnet");
        checkValid("architectures", "ThreeInputCNN_M14");
        checkValid("architectures", "VGG16");

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

    }

    @Test
    public void testInvalidPreResolveCocos(){
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckIOLayer()),
                new CNNArchCoCoChecker(),
                getAstNode("invalid_tests", "UnknownIO"),
                new ExpectedErrorInfo(2, ErrorCodes.UNKNOWN_IO_CODE));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckMethodLayer()),
                new CNNArchCoCoChecker(),
                getAstNode("invalid_tests", "DuplicatedArgument"),
                new ExpectedErrorInfo(1, ErrorCodes.DUPLICATED_ARG_CODE));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckArgument()).addCoCo(new CheckMethodLayer()),
                new CNNArchCoCoChecker(),
                getAstNode("invalid_tests", "WrongArgument"),
                new ExpectedErrorInfo(4, ErrorCodes.UNKNOWN_ARGUMENT_CODE, ErrorCodes.MISSING_ARGUMENT_CODE));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckMethodDeclaration()),
                new CNNArchCoCoChecker(),
                getAstNode("invalid_tests", "InvalidRecursion"),
                new ExpectedErrorInfo(1, ErrorCodes.RECURSION_ERROR_CODE));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckArgument()).addCoCo(new CheckVariable()),
                new CNNArchCoCoChecker(),
                getAstNode("invalid_tests", "InvalidVariableType"),
                new ExpectedErrorInfo(5, ErrorCodes.ILLEGAL_ASSIGNMENT_CODE));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckArgument()).addCoCo(new CheckVariable()),
                new CNNArchCoCoChecker(),
                getAstNode("invalid_tests", "InvalidVariableType2"),
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT_CODE));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckMethodLayer()),
                new CNNArchCoCoChecker(),
                getAstNode("invalid_tests", "MissingArgument"),
                new ExpectedErrorInfo(3, ErrorCodes.MISSING_ARGUMENT_CODE));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckVariable()).addCoCo(new CheckMethodDeclaration()),
                new CNNArchCoCoChecker(),
                getAstNode("invalid_tests", "IllegalName"),
                new ExpectedErrorInfo(2, ErrorCodes.ILLEGAL_NAME_CODE));
    }

    @Test
    public void testInvalidPostResolveCocos(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchCoCoChecker().addCoCo(new CheckLayerInputs()),
                getAstNode("invalid_tests", "InvalidInputShape"),
                new ExpectedErrorInfo(2, ErrorCodes.INVALID_LAYER_INPUT));
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchCoCoChecker().addCoCo(new CheckLayerInputs()),
                getAstNode("invalid_tests", "MissingMerge"),
                new ExpectedErrorInfo(2, ErrorCodes.MISSING_MERGE));
    }

}