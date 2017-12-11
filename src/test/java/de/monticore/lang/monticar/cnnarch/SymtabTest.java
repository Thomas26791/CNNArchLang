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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.monticore.lang.monticar.cnnarch._parser.CNNArchParser;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.symboltable.Scope;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

public class SymtabTest extends AbstractSymtabTest {

    @Test
    public void testParsing() throws Exception {
        CNNArchParser parser = new CNNArchParser();
        assertTrue(parser.parse("src/test/resources/architectures/Alexnet.cnna").isPresent());
    }

    @Test
    public void testAlexnet(){
        Scope symTab = createSymTab("src/test/resources/architectures");
        ArchitectureSymbol a = symTab.<ArchitectureSymbol>resolve(
                "Alexnet",
                ArchitectureSymbol.KIND).orElse(null);
        assertNotNull(a);
    }



}
