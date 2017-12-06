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
package de.monticore.lang.monticar.cnnarch._cocos;

import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.cnnarch._symboltable.OutputDefSymbol;
import de.se_rwth.commons.logging.Log;

import java.util.*;

public class ArchitectureCheck implements CNNArchASTArchitectureCoCo {

    public static final String OUTPUT_UNDEFINED_CODE = "x0601D";
    public static final String OUTPUT_UNDEFINED_MSG = "0" + OUTPUT_UNDEFINED_CODE + " Undefined output. ";
    public static final String OUTPUT_MISSING_CODE = "x0601C";
    public static final String OUTPUT_MISSING_MSG = "0" + OUTPUT_MISSING_CODE + " Missing output. Architecture has to declare at least one output.";
    public static final String OUTPUT_UNUSED_CODE = "x0601E";
    public static final String OUTPUT_UNUSED_MSG = "0" + OUTPUT_UNUSED_CODE + " ";
    public static final String OUTPUT_DUPLICATE_CODE = "x0601B";
    public static final String OUTPUT_DUPLICATE_MSG = "0"+OUTPUT_DUPLICATE_CODE+" ";



    @Override
    public void check(ASTArchitecture node) {
        List<ASTOutputDef> outDefs = new LinkedList<>();
        List<ASTOutputStructure> outStructures = new LinkedList<>();
        Map<ASTOutputDef, Integer> countOutputMap = new HashMap<>();

        for (ASTVarDef def : node.getDefs()){
            if (def instanceof ASTOutputDef){
                outDefs.add((ASTOutputDef) def);
                countOutputMap.put((ASTOutputDef) def, 0);
            }
        }

        for(ASTArchitectureElement element : node.getElements()){
            if (element instanceof ASTOutputStructure){
                outStructures.add((ASTOutputStructure) element);
            }
        }

        if (outDefs.isEmpty()){
            Log.error(OUTPUT_MISSING_MSG, node.get_SourcePositionStart());
        }

        for(ASTOutputStructure outStr : outStructures){
            Optional<OutputDefSymbol> outSymbol = outStr.getOutputSymbol();
            if (outSymbol.isPresent() && outSymbol.get().getAstNode().isPresent()){

                ASTOutputDef def = (ASTOutputDef) outSymbol.get().getAstNode().get();
                countOutputMap.put(def, countOutputMap.get(def) + 1);
            }
            else {
                Log.error(OUTPUT_UNDEFINED_MSG + "The output name " + outStr.getOutput() + " was never defined. "
                        , outStr.get_SourcePositionEnd());
            }
        }

        for (ASTOutputDef def : outDefs){
            if (countOutputMap.get(def).equals(0)){
                Log.error(OUTPUT_UNUSED_MSG + "Output with the name " + def.getName() + " was declared but not used."
                        , def.get_SourcePositionStart());
            }
            else {
                if (def.getArrayDeclaration().isPresent()){
                    /*if (!countOutputMap.get(def).equals(
                            def.getArrayDeclaration().get().getIntLiteral().getNumber().get().getDividend().intValue())){

                    }*/
                }
                else {
                    if (!countOutputMap.get(def).equals(1)){
                        Log.error(OUTPUT_DUPLICATE_MSG + "Output with the name " + def.getName() + " was assigned multiple times."
                                , def.get_SourcePositionStart());
                    }
                }
            }
        }
    }

}
