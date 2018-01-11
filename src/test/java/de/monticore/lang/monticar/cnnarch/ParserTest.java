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

import de.monticore.lang.monticar.cnnarch._ast.ASTCNNArchCompilationUnit;
import de.monticore.lang.monticar.cnnarch._parser.CNNArchParser;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class ParserTest {

    public static final boolean ENABLE_FAIL_QUICK = false;
    private static List<String> expectedParseErrorModels = Arrays.asList(
            // incorrect argument name for a layer
            "src/test/resources/invalid_tests/MissingParallelBrackets.cnna",
            "src/test/resources/invalid_tests/MissingLayerOperator.cnna")

            .stream().map(s -> Paths.get(s).toString())
            .collect(Collectors.toList());

    @Before
    public void setUp() {
        // ensure an empty log
        Log.getFindings().clear();
        Log.enableFailQuick(ENABLE_FAIL_QUICK);
    }

    @Test
    public void testCNNArch() throws Exception {
        test("cnna");
        if (Log.getErrorCount() > 0) {
            throw new Exception("Test Failed, found errors");
        }
    }

    private void test(String fileEnding) throws IOException {
        ParseTest parserTest = new ParseTest("." + fileEnding);
        Files.walkFileTree(Paths.get("src/test/resources"), parserTest);

        if (!parserTest.getModelsInError().isEmpty()) {
            Log.debug("Models in error", "ParserTest");
            for (String model : parserTest.getModelsInError()) {
                Log.debug("  " + model, "ParserTest");
            }
        }
        Log.info("Count of tested models: " + parserTest.getTestCount(), "ParserTest");
        Log.info("Count of correctly parsed models: "
                + (parserTest.getTestCount() - parserTest.getModelsInError().size()), "ParserTest");

        assertTrue("There were models that could not be parsed", parserTest.getModelsInError()
                .isEmpty());
    }

    /**
     * Visits files of the given file ending and checks whether they are parsable.
     *
     * @author Robert Heim
     * @see Files#walkFileTree(Path, java.nio.file.FileVisitor)
     */
    private static class ParseTest extends SimpleFileVisitor<Path> {

        private String fileEnding;

        private List<String> modelsInError = new ArrayList<>();

        private int testCount = 0;

        public ParseTest(String fileEnding) {
            super();
            this.fileEnding = fileEnding;
        }

        /**
         * @return testCount
         */
        public int getTestCount() {
            return this.testCount;
        }

        /**
         * @return modelsInError
         */
        public List<String> getModelsInError() {
            return this.modelsInError;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            if (file.toFile().isFile()
                    && (file.toString().toLowerCase().endsWith(fileEnding))) {

                Log.debug("Parsing file " + file.toString(), "ParserTest");
                testCount++;
                Optional<ASTCNNArchCompilationUnit> model = Optional.empty();
                boolean expectingError = ParserTest.expectedParseErrorModels.contains(file.toString());

                CNNArchParser parser = new CNNArchParser();
                try {
                    if (expectingError) {
                        Log.enableFailQuick(false);
                    }
                    model = parser.parse(file.toString());
                }
                catch (Exception e) {
                    if (!expectingError) {
                        Log.error("Exception during test", e);
                    }
                }
                if (!expectingError && (parser.hasErrors() || !model.isPresent())) {
                    modelsInError.add(file.toString());
                    Log.error("There were unexpected parser errors");
                }
                else {
                    Log.getFindings().clear();
                }
                Log.enableFailQuick(ParserTest.ENABLE_FAIL_QUICK);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
