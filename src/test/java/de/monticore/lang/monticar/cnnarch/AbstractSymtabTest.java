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

import de.monticore.ModelingLanguageFamily;
import de.monticore.io.paths.ModelPath;
import de.monticore.lang.monticar.cnnarch._ast.ASTCNNArchCompilationUnit;
import de.monticore.lang.monticar.cnnarch._symboltable.CNNArchCompilationUnitSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.CNNArchLanguage;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;

import java.nio.file.Paths;

import static de.monticore.lang.monticar.cnnarch.ParserTest.ENABLE_FAIL_QUICK;
import static org.junit.Assert.assertNotNull;

public class AbstractSymtabTest {

    private static final String MODEL_PATH = "src/test/resources/";

    protected static Scope createSymTab(String... modelPath) {
        ModelingLanguageFamily fam = new ModelingLanguageFamily();

        fam.addModelingLanguage(new CNNArchLanguage());

        final ModelPath mp = new ModelPath();
        for (String m : modelPath) {
            mp.addEntry(Paths.get(m));
        }
        GlobalScope scope = new GlobalScope(mp, fam);

        return scope;
    }

/*    protected static ASTCNNArchCompilationUnit getAstNode(String modelPath, String model) {
        Scope symTab = createSymTab(MODEL_PATH + modelPath);
        CNNArchCompilationUnitSymbol comp = symTab.<CNNArchCompilationUnitSymbol> resolve(
                model, CNNArchCompilationUnitSymbol.KIND).orElse(null);
        assertNotNull("Could not resolve model " + model, comp);

        return (ASTCNNArchCompilationUnit) comp.getAstNode().get();
    }*/

    protected static CNNArchCompilationUnitSymbol getCompilationUnitSymbol(String modelPath, String model) {
        Scope symTab = createSymTab(MODEL_PATH + modelPath);
        CNNArchCompilationUnitSymbol comp = symTab.<CNNArchCompilationUnitSymbol> resolve(
                model, CNNArchCompilationUnitSymbol.KIND).orElse(null);
        assertNotNull("Could not resolve model " + model, comp);

        return comp;
    }
}
