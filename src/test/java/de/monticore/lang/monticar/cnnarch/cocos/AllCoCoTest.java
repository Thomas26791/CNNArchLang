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
    public void testIllegalIONames(){
        checkInvalid(
                new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckIOName()),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "IllegalIOName",
                new ExpectedErrorInfo(2, ErrorCodes.ILLEGAL_NAME));
    }

    @Test
    public void testUnknownMethod(){
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckLayer()),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "UnknownMethod",
                new ExpectedErrorInfo(1, ErrorCodes.UNKNOWN_LAYER));
    }

    @Test
    public void testDuplicatedNames(){
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckVariableName()).addCoCo(new CheckLayerName()),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "DuplicatedNames",
                new ExpectedErrorInfo(2, ErrorCodes.DUPLICATED_NAME));
    }

    @Test
    public void testDuplicatedIONames(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckIOName()),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "DuplicatedIONames",
                new ExpectedErrorInfo(1, ErrorCodes.DUPLICATED_NAME));
    }

    @Test
    public void testUnknownVariableName(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckExpressions()),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "UnknownVariableName",
                new ExpectedErrorInfo(1, ErrorCodes.UNKNOWN_VARIABLE_NAME));
    }

    @Test
    public void testUnknownIO(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckIOName()),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "UnknownIO",
                new ExpectedErrorInfo(2, ErrorCodes.UNKNOWN_IO));
    }

    @Test
    public void testDuplicatedArgument(){
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckLayer()),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "DuplicatedArgument",
                new ExpectedErrorInfo(1, ErrorCodes.DUPLICATED_ARG));
    }

    @Test
    public void testWrongArgument(){
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckArgument()).addCoCo(new CheckLayer()),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "WrongArgument",
                new ExpectedErrorInfo(4, ErrorCodes.UNKNOWN_ARGUMENT, ErrorCodes.MISSING_ARGUMENT));
    }

    @Test
    public void testInvalidRecursion(){
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckLayerRecursion()),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "InvalidRecursion",
                new ExpectedErrorInfo(1, ErrorCodes.RECURSION_ERROR));
    }

    @Test
    public void testArgumentConstraintTest1(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "ArgumentConstraintTest1",
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT));
    }

    @Test
    public void testArgumentConstraintTest2(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "ArgumentConstraintTest2",
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT));
    }

    @Test
    public void testWrongRangeOperator(){
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckRangeOperators()),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "WrongRangeOperator",
                new ExpectedErrorInfo(2, ErrorCodes.DIFFERENT_RANGE_OPERATORS));
    }

    @Test
    public void testArgumentConstraintTest3(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "ArgumentConstraintTest3",
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT));
    }

    @Test
    public void testArgumentConstraintTest4(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "ArgumentConstraintTest4",
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT));
    }

    @Test
    public void testArgumentConstraintTest5(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "ArgumentConstraintTest5",
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT));
    }

    @Test
    public void testArgumentConstraintTest6(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "ArgumentConstraintTest6",
                new ExpectedErrorInfo(1, ErrorCodes.ILLEGAL_ASSIGNMENT));
    }

    @Test
    public void testMissingArgument(){
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckLayer()),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "MissingArgument",
                new ExpectedErrorInfo(3, ErrorCodes.MISSING_ARGUMENT));
    }

    @Test
    public void testIllegalName(){
        checkInvalid(new CNNArchCoCoChecker().addCoCo(new CheckVariableName()).addCoCo(new CheckLayerName()),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                "invalid_tests", "IllegalName",
                new ExpectedErrorInfo(2, ErrorCodes.ILLEGAL_NAME));
    }

    @Test
    public void testUnfinishedArchitecture(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckArchitectureFinished()),
                "invalid_tests", "UnfinishedArchitecture",
                new ExpectedErrorInfo(1, ErrorCodes.UNFINISHED_ARCHITECTURE));
    }

    @Test
    public void testInvalidInputShape(){
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckElementInputs()),
                "invalid_tests", "InvalidInputShape",
                new ExpectedErrorInfo(2, ErrorCodes.INVALID_ELEMENT_INPUT_SHAPE));
    }

    @Test
    public void testWrongIOType() {
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckElementInputs()),
                "invalid_tests", "WrongIOType",
                new ExpectedErrorInfo(1, ErrorCodes.INVALID_ELEMENT_INPUT_DOMAIN));
    }

    @Test
    public void testInvalidIOShape1() {
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckIOType()),
                "invalid_tests", "InvalidIOShape1",
                new ExpectedErrorInfo(2, ErrorCodes.INVALID_IO_TYPE));
    }

    @Test
    public void testInvalidIOShape2() {
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckIOType()),
                "invalid_tests", "InvalidIOShape2",
                new ExpectedErrorInfo(2, ErrorCodes.INVALID_IO_TYPE));
    }

    @Test
    public void testNotIOArray() {
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckIOAccessAndIOMissing()),
                "invalid_tests", "NotIOArray",
                new ExpectedErrorInfo(2, ErrorCodes.INVALID_ARRAY_ACCESS));
    }

    @Test
    public void testMissingIO2() {
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckIOAccessAndIOMissing()),
                "invalid_tests", "MissingIO2",
                new ExpectedErrorInfo(2, ErrorCodes.MISSING_IO));
    }

    @Test
    public void testInvalidArrayAccessValue() {
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckIOAccessAndIOMissing()),
                "invalid_tests", "InvalidArrayAccessValue",
                new ExpectedErrorInfo(1, ErrorCodes.INVALID_ARRAY_ACCESS));
    }

    @Test
    public void testMissingMerge() {
        checkInvalid(new CNNArchCoCoChecker(),
                new CNNArchSymbolCoCoChecker(),
                new CNNArchSymbolCoCoChecker().addCoCo(new CheckElementInputs()),
                "invalid_tests", "MissingMerge",
                new ExpectedErrorInfo(2, ErrorCodes.MISSING_MERGE));
    }

}