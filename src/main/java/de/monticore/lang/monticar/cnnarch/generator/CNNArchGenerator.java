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
import java.util.Optional;

public class CNNArchGenerator {

    private Target targetLanguage;
    private String generationTargetPath;

    public CNNArchGenerator() {
        setTargetLanguage(Target.CPP);
        setGenerationTargetPath("./target/generated-sources-cnnarch/");
    }

    public Target getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(Target targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String getGenerationTargetPath() {
        return generationTargetPath;
    }

    public void setGenerationTargetPath(String generationTargetPath) {
        if (generationTargetPath.charAt(generationTargetPath.length() - 1) != '/') {
            this.generationTargetPath = generationTargetPath + "/";
        }
        else {
            this.generationTargetPath = generationTargetPath;
        }
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
            generateNetworkFile(architecture);
        }
        catch (IOException e){
            Log.error(e.toString());
        }
    }

    public String generateNetworkString(ArchitectureSymbol architecture){
        TemplateController archTc = new TemplateController(architecture, targetLanguage);
        return archTc.process();
    }

    public void generateNetworkFile(ArchitectureSymbol architecture) throws IOException{
        File f = new File(getGenerationTargetPath() + getFileName(architecture));
        Log.info(f.getName(), "FileCreation:");
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            if (!f.createNewFile()) {
                Log.error("File could not be created");
            }
        }

        FileWriter writer = new FileWriter(f);
        TemplateController archTc = new TemplateController(architecture, targetLanguage);
        archTc.process(writer);
        writer.close();
    }

    public String getFileName(ArchitectureSymbol architecture){
        String name = architecture.getEnclosingScope().getSpanningSymbol().get().getFullName();
        name = name.replaceAll("\\.", "_").replaceAll("\\[", "_").replaceAll("\\]", "_");

        String fileEnding = getTargetLanguage().toString();
        if (getTargetLanguage() == Target.CPP){
            fileEnding = ".h";
        }

        return name + "__network" + fileEnding;
    }

}
