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
package de.monticore.lang.monticar.cnnarch.generator;

import de.monticore.io.paths.ModelPath;
import de.monticore.lang.monticar.cnnarch._cocos.CNNArchCocos;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.CNNArchCompilationUnitSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.CNNArchLanguage;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CNNArchGenerator {

    private String generationTargetPath;

    public CNNArchGenerator() {
        setGenerationTargetPath("./target/generated-sources-cnnarch/");
    }

    public String getGenerationTargetPath() {
        if (generationTargetPath.charAt(generationTargetPath.length() - 1) != '/') {
            this.generationTargetPath = generationTargetPath + "/";
        }
        return generationTargetPath;
    }

    public void setGenerationTargetPath(String generationTargetPath) {
        this.generationTargetPath = generationTargetPath;
    }

    public void generate(Path modelsDirPath, String rootModelName){
        final ModelPath mp = new ModelPath(modelsDirPath);
        GlobalScope scope = new GlobalScope(mp, new CNNArchLanguage());
        generate(scope, rootModelName);
    }

    public void generate(Scope scope, String rootModelName){
        Optional<CNNArchCompilationUnitSymbol> compilationUnit = scope.resolve(rootModelName, CNNArchCompilationUnitSymbol.KIND);
        if (!compilationUnit.isPresent()){
            Log.error("could not resolve architecture " + rootModelName);
            System.exit(1);
        }

        CNNArchCocos.checkAll(compilationUnit.get());

        try{
            ArchitectureSymbol architecture = compilationUnit.get().getArchitecture();
            generateFiles(architecture);
        }
        catch (IOException e){
            Log.error(e.toString());
        }
    }

    //check cocos with CNNArchCocos.checkAll(architecture) before calling this method.
    public Map<String, String> generateStrings(ArchitectureSymbol architecture){
        Map<String, String> fileContentMap = new HashMap<>();
        CNNArchTemplateController archTc = new CNNArchTemplateController(architecture);
        Map.Entry<String, String> temp;

        temp = archTc.process("CNNPredictor", Target.CPP);
        fileContentMap.put(temp.getKey(), temp.getValue());

        temp = archTc.process("CNNCreator", Target.PYTHON);
        fileContentMap.put(temp.getKey(), temp.getValue());

        temp = archTc.process("execute", Target.CPP);
        fileContentMap.put(temp.getKey().replace(".h", ""), temp.getValue());

        temp = archTc.process("CNNBufferFile", Target.CPP);
        fileContentMap.put("CNNBufferFile.h", temp.getValue());

        checkValidGeneration(architecture);

        return fileContentMap;
    }

    private void checkValidGeneration(ArchitectureSymbol architecture){
        if (architecture.getInputs().size() > 1){
            Log.warn("This cnn architecture has multiple inputs, " +
                            "which is currently not supported by the generator. " +
                            "The generated code will not work correctly."
                    , architecture.getSourcePosition());
        }
        if (architecture.getOutputs().size() > 1){
            Log.warn("This cnn architecture has multiple outputs, " +
                            "which is currently not supported by the generator. " +
                            "The generated code will not work correctly."
                    , architecture.getSourcePosition());
        }
        if (architecture.getOutputs().get(0).getDefinition().getType().getWidth() != 1 ||
                architecture.getOutputs().get(0).getDefinition().getType().getHeight() != 1){
            Log.error("This cnn architecture has a multi-dimensional output, " +
                            "which is currently not supported by the generator."
                    , architecture.getSourcePosition());
        }
    }

    //check cocos with CNNArchCocos.checkAll(architecture) before calling this method.
    public void generateFiles(ArchitectureSymbol architecture) throws IOException{
        CNNArchTemplateController archTc = new CNNArchTemplateController(architecture);
        Map<String, String> fileContentMap = generateStrings(architecture);

        for (String fileName : fileContentMap.keySet()){
            File f = new File(getGenerationTargetPath() + fileName);
            Log.info(f.getName(), "FileCreation:");
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                if (!f.createNewFile()) {
                    Log.error("File could not be created");
                }
            }

            FileWriter writer = new FileWriter(f);
            writer.write(fileContentMap.get(fileName));
            writer.close();
        }
    }

}
