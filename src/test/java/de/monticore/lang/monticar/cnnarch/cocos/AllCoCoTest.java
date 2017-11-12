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

import de.monticore.lang.monticar.cnnarch._cocos.CNNArchCocos;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class AllCoCoTest extends AbstractCoCoTest {
    String baseDir="src/test/resources";
    @Test
    public void testCoCosSimulator() throws IOException {
        testModel("SimpleNetwork");
        testModel("SimpleNetwork2");
        testModel("Alexnet");

        testInvalidModel("DuplicateArgument",1,"x03011");
        testInvalidModel("IntegerArgumentTypeTest",9,"x03012");
        testInvalidModel("InvalidActivationBeforeOutput1",1,"x03015");
        testInvalidModel("InvalidLayerDimension",1,"x03018");
        testInvalidModel("MissingConvolutionArgument1",1,"x0301A");
        testInvalidModel("MissingConvolutionArgument2",1,"x0301A");
        testInvalidModel("MissingFullyConnectedArgument",1,"x0301A");
        testInvalidModel("MissingLRNArgument",1,"x0301A");
        testInvalidModel("MissingPoolingArgument1",1,"x0301A");
        testInvalidModel("MissingPoolingArgument2",1,"x0301A");


    }

    private void testModel(String modelName) {
        checkValid("",modelName);
    }

    private void testInvalidModel(String modelName, int numExpectedFindings, String... expectedErrorCodes) {
        ExpectedErrorInfo errorInfo = new ExpectedErrorInfo(numExpectedFindings, expectedErrorCodes);
        checkInvalid(CNNArchCocos.createChecker(), getAstNode("", modelName), errorInfo);
    }

}