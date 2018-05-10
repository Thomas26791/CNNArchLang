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

import de.monticore.io.paths.ModelPath;
import de.monticore.lang.monticar.cnnarch._cocos.CNNArchCocos;
import de.monticore.lang.monticar.cnnarch._symboltable.CNNArchCompilationUnitSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.CNNArchLanguage;
import de.monticore.lang.monticar.cnnarch.generator.CNNArchGenerator;
import de.monticore.lang.monticar.cnnarch.generator.CNNArchGeneratorCli;
import de.monticore.symboltable.GlobalScope;
import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import static de.monticore.lang.monticar.cnnarch.ParserTest.ENABLE_FAIL_QUICK;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class GenerationTest {

    @Before
    public void setUp() {
        // ensure an empty log
        Log.getFindings().clear();
        Log.enableFailQuick(ENABLE_FAIL_QUICK);
    }

    private Map<String,String> generateStrings(String modelsDirPath, String modelName) throws IOException, TemplateException {
        final ModelPath mp = new ModelPath(Paths.get(modelsDirPath));
        GlobalScope scope = new GlobalScope(mp, new CNNArchLanguage());
        CNNArchCompilationUnitSymbol compilationUnit = scope.<CNNArchCompilationUnitSymbol>
                resolve(modelName, CNNArchCompilationUnitSymbol.KIND).get();
        CNNArchCocos.checkAll(compilationUnit);

        CNNArchGenerator gen =  new CNNArchGenerator();
        return gen.generateStrings(compilationUnit.getArchitecture());
    }

    private String readFileFromResources(String relativePath) throws IOException{
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(relativePath).getFile());
        Scanner scanner = new Scanner(file);
        scanner.useDelimiter("\\Z");
        String content = scanner.next() + "\n";
        scanner.close();
        return content;
    }

    @Test
    public void testCifar10Classifier() throws IOException, TemplateException {
        Log.getFindings().clear();
        Map<String,String> stringMap = generateStrings(
                "src/test/resources/valid_tests",
                "CifarClassifierNetwork");
        assertTrue(Log.getFindings().isEmpty());

        assertEquals(stringMap.get("CNNCreator_CifarClassifierNetwork.py"),
                readFileFromResources("target_code/CNNCreator_CifarClassifierNetwork.py"));
        assertEquals(stringMap.get("CNNPredictor_CifarClassifierNetwork.h"),
                readFileFromResources("target_code/CNNPredictor_CifarClassifierNetwork.h"));
        assertEquals(stringMap.get("execute_CifarClassifierNetwork"),
                readFileFromResources("target_code/execute_CifarClassifierNetwork"));
        assertEquals(stringMap.get("CNNBufferFile.h"),
                readFileFromResources("target_code/CNNBufferFile.h"));
    }

    @Test
    public void testAlexnetGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        Map<String,String> stringMap = generateStrings(
                "src/test/resources/architectures",
                "Alexnet");
        assertTrue(Log.getFindings().isEmpty());

        assertEquals(stringMap.get("CNNCreator_Alexnet.py"),
                readFileFromResources("target_code/CNNCreator_Alexnet.py"));
        assertEquals(stringMap.get("CNNPredictor_Alexnet.h"),
                readFileFromResources("target_code/CNNPredictor_Alexnet.h"));
        assertEquals(stringMap.get("execute_Alexnet"),
                readFileFromResources("target_code/execute_Alexnet"));
    }

    @Test
    public void testGeneratorVGG16() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/architectures", "-r", "VGG16"};
        CNNArchGeneratorCli.main(args);
        Map<String,String> stringMap = generateStrings(
                "src/test/resources/architectures",
                "VGG16");
        assertTrue(Log.getFindings().isEmpty());

        assertEquals(stringMap.get("CNNCreator_VGG16.py"),
                readFileFromResources("target_code/CNNCreator_VGG16.py"));
        assertEquals(stringMap.get("CNNPredictor_VGG16.h"),
                readFileFromResources("target_code/CNNPredictor_VGG16.h"));
        assertEquals(stringMap.get("execute_VGG16"),
                readFileFromResources("target_code/execute_VGG16"));
    }


    @Test
    public void testThreeInputCNNGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/architectures", "-r", "ThreeInputCNN_M14"};
        CNNArchGeneratorCli.main(args);;
        assertTrue(Log.getFindings().size() == 1);
    }

    @Test
    public void testResNeXtGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();;
        String[] args = {"-m", "src/test/resources/architectures", "-r", "ResNeXt50"};
        CNNArchGeneratorCli.main(args);;
        assertTrue(Log.getFindings().isEmpty());
    }

    @Test
    public void testMultipleOutputs() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/valid_tests", "-r", "MultipleOutputs"};
        CNNArchGeneratorCli.main(args);
        assertTrue(Log.getFindings().size() == 3);
    }
}
