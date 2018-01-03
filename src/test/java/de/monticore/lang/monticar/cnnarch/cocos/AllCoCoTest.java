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
        checkValid("architectures", "Resnet34");
        checkValid("architectures", "SequentialAlexnet");
        checkValid("architectures", "ThreeInputCNN_M14");
        checkValid("architectures", "VGG16");

        checkValid("valid_tests", "Fixed_Alexnet");
        checkValid("valid_tests", "Fixed_ResNeXt50");
        checkValid("valid_tests", "Fixed_ThreeInputCNN_M14");
        checkValid("valid_tests", "ThreeInputCNN_M14_alternative");
        checkValid("valid_tests", "Alexnet_alt");
        checkValid("valid_tests", "SimpleNetworkSoftmax");
        checkValid("valid_tests", "SimpleNetworkSigmoid");
        checkValid("valid_tests", "SimpleNetworkLinear");
        checkValid("valid_tests", "SimpleNetworkRelu");
        checkValid("valid_tests", "SimpleNetworkTanh");
        /*checkValid("architectures", "Alexnet");
        checkValid("architectures", "Resnet34");
        checkValid("architectures", "ResNeXt50");
        checkValid("architectures", "SequentialAlexnet");
        checkValid("architectures", "ThreeInputCNN_M14");
        checkValid("architectures", "ThreeInputCNN_M14_alternative");
        checkValid("architectures", "VGG16");


        checkValid("valid_tests", "GroupTest");
        checkValid("valid_tests", "MultiOutputTest");
        checkValid("valid_tests", "MultiOutputArrayTest");
        checkValid("valid_tests", "VGG16_alternative");
        checkValid("valid_tests", "DirectPerception");
        checkValid("valid_tests", "SafetyNetwork");*/

    }

    /*@Test
    public void testArgumentCoCos() throws IOException{

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new ArgumentNameCheck())
                , getAstNode("invalid_tests", "DuplicateArgument")
                , new ExpectedErrorInfo(1,ArgumentNameCheck.DUPLICATE_ARG_CODE));

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new ArgumentTypeCheck())
                , getAstNode("invalid_tests", "BooleanArgumentTypeTest")
                , new ExpectedErrorInfo(3,ArgumentTypeCheck.INCORRECT_ARG_TYPE_CODE));

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new ArgumentTypeCheck())
                , getAstNode("invalid_tests", "IntegerArgumentTypeTest")
                , new ExpectedErrorInfo(7,ArgumentTypeCheck.INCORRECT_ARG_TYPE_CODE));

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new ArgumentMissingCheck())
                , getAstNode("invalid_tests", "MissingConvolutionArgument1")
                , new ExpectedErrorInfo(1,ArgumentMissingCheck.MISSING_ARG_CODE));

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new ArgumentMissingCheck())
                , getAstNode("invalid_tests", "MissingConvolutionArgument2")
                , new ExpectedErrorInfo(2, ArgumentMissingCheck.MISSING_ARG_CODE));

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new ArgumentMissingCheck())
                , getAstNode("invalid_tests", "MissingFullyConnectedArgument")
                , new ExpectedErrorInfo(1, ArgumentMissingCheck.MISSING_ARG_CODE));

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new ArgumentMissingCheck())
                , getAstNode("invalid_tests", "MissingPoolingArgument1")
                , new ExpectedErrorInfo(1, ArgumentMissingCheck.MISSING_ARG_CODE));

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new ArgumentMissingCheck())
                , getAstNode("invalid_tests", "MissingLRNArgument")
                , new ExpectedErrorInfo(1, ArgumentMissingCheck.MISSING_ARG_CODE));

        *//*;
        testInvalidModel("InvalidActivationBeforeOutput1",1,"x03015");
        testInvalidModel("InvalidLayerDimension",1,"x03018");*//*
    }

    @Test
    public void testOutputCoCos() throws IOException{
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new OutputCheck())
                , getAstNode("invalid_tests", "InvalidFixedOutputUnits")
                , new ExpectedErrorInfo(1,OutputCheck.OUTPUT_MISSING_VAR_FC_CODE));

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new OutputCheck())
                , getAstNode("invalid_tests", "InvalidFixedOutputUnitsAndMissingArgument")
                , new ExpectedErrorInfo(2,ArgumentMissingCheck.MISSING_ARG_CODE, OutputCheck.OUTPUT_MISSING_VAR_FC_CODE));

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new OutputCheck())
                , getAstNode("invalid_tests", "InvalidOutput1")
                , new ExpectedErrorInfo(1,OutputCheck.OUTPUT_MISSING_VAR_FC_CODE));

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new OutputCheck())
                , getAstNode("invalid_tests", "InvalidOutput2")
                , new ExpectedErrorInfo(1,OutputCheck.OUTPUT_MISSING_VAR_FC_CODE));
    }

    @Test
    public void testArchitectureCoCos() throws  IOException{

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new ArchitectureCheck())
                , getAstNode("invalid_tests", "MissingOutput")
                , new ExpectedErrorInfo(1, ArchitectureCheck.OUTPUT_MISSING_CODE));

        //todo: input def error
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new ArchitectureCheck())
                , getAstNode("invalid_tests", "UndefinedOutput")
                , new ExpectedErrorInfo(2, ArchitectureCheck.OUTPUT_UNDEFINED_CODE, ArchitectureCheck.OUTPUT_MISSING_CODE));
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new ArchitectureCheck())
                , getAstNode("invalid_tests", "UnusedOutputDef")
                , new ExpectedErrorInfo(1, ArchitectureCheck.OUTPUT_UNUSED_CODE));

        checkInvalid(new CNNArchCoCoChecker().addCoCo(new ArchitectureCheck())
                , getAstNode("invalid_tests", "DuplicateOutputAssignment")
                , new ExpectedErrorInfo(1, ArchitectureCheck.OUTPUT_DUPLICATE_CODE));
    }*/

}