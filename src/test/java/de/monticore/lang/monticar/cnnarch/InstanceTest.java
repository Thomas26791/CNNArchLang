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

import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import static de.monticore.lang.monticar.cnnarch.ParserTest.ENABLE_FAIL_QUICK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InstanceTest extends AbstractSymtabTest {

    @Before
    public void setUp() {
        // ensure an empty log
        Log.getFindings().clear();
        Log.enableFailQuick(ENABLE_FAIL_QUICK);
    }

    @Test
    public void testInstanceCreation(){
        Scope symTab = createSymTab("src/test/resources/architectures");
        CNNArchCompilationUnitSymbol compilationUnitSymbol = symTab.<CNNArchCompilationUnitSymbol>resolve(
                "SequentialAlexnet",
                CNNArchCompilationUnitSymbol.KIND).orElse(null);
        assertNotNull(compilationUnitSymbol);

        compilationUnitSymbol.setParameter("classes", 100);
        ArchitectureSymbol instance1 = compilationUnitSymbol.resolve();


        CNNArchCompilationUnitSymbol compilationUnit2 = compilationUnitSymbol.preResolveDeepCopy();
        compilationUnit2.setParameter("img_height", 200);
        compilationUnit2.setParameter("img_width", 210);
        ArchitectureSymbol instance2 = compilationUnit2.resolve();

        int width1 = instance1.getInputs().get(0).getDefinition().getType().getWidth();
        int height1 = instance1.getInputs().get(0).getDefinition().getType().getHeight();
        int channels1 = instance1.getOutputs().get(0).getDefinition().getType().getChannels();
        int lastLayerChannels1 = instance1.getOutputs().get(0).getInputTypes().get(0).getChannels();

        int width2 = instance2.getInputs().get(0).getDefinition().getType().getWidth();
        int height2 = instance2.getInputs().get(0).getDefinition().getType().getHeight();
        int channels2 = instance2.getOutputs().get(0).getDefinition().getType().getChannels();
        int lastLayerChannels2 = instance2.getOutputs().get(0).getInputTypes().get(0).getChannels();

        assertEquals(224, width1);
        assertEquals(224, height1);
        assertEquals(100, channels1);
        assertEquals(100, lastLayerChannels1);
        assertEquals(210, width2);
        assertEquals(200, height2);
        assertEquals(10, channels2);
        assertEquals(10, lastLayerChannels2);
    }

    @Test
    public void testInstanceCreation2(){
        Scope symTab = createSymTab("src/test/resources/valid_tests");
        CNNArchCompilationUnitSymbol compilationUnitSymbol = symTab.<CNNArchCompilationUnitSymbol>resolve(
                "ResNeXt50_InstanceTest",
                CNNArchCompilationUnitSymbol.KIND).orElse(null);
        assertNotNull(compilationUnitSymbol);


        compilationUnitSymbol.setParameter("cardinality", 32);
        ArchitectureSymbol instance1 = compilationUnitSymbol.resolve();


        CNNArchCompilationUnitSymbol compilationUnit2 = compilationUnitSymbol.preResolveDeepCopy();
        compilationUnit2.setParameter("cardinality", 2);
        ArchitectureSymbol instance2 = compilationUnit2.resolve();
        ArchRangeExpressionSymbol range1 = (ArchRangeExpressionSymbol) ((MethodLayerSymbol)(((CompositeLayerSymbol)((CompositeLayerSymbol)((CompositeLayerSymbol)((CompositeLayerSymbol) instance1.getBody()).getLayers().get(5).getResolvedThis().get()).getLayers().get(0)).getLayers().get(0)).getLayers().get(0))).getArgument("|").get().getRhs();
        ArchRangeExpressionSymbol range2 = (ArchRangeExpressionSymbol) ((MethodLayerSymbol)(((CompositeLayerSymbol)((CompositeLayerSymbol)((CompositeLayerSymbol)((CompositeLayerSymbol) instance2.getBody()).getLayers().get(5).getResolvedThis().get()).getLayers().get(0)).getLayers().get(0)).getLayers().get(0))).getArgument("|").get().getRhs();

        assertEquals(32, range1.getElements().get().size());
        assertEquals(2, range2.getElements().get().size());

    }
}
