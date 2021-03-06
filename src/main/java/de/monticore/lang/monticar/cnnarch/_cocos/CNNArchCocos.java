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

import de.monticore.lang.monticar.cnnarch._ast.ASTCNNArchNode;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.CNNArchCompilationUnitSymbol;
import de.se_rwth.commons.logging.Log;

//check all cocos
public class CNNArchCocos {

    public static void checkAll(ArchitectureSymbol architecture){
        ASTCNNArchNode node = (ASTCNNArchNode) architecture.getAstNode().get();
        int findings = Log.getFindings().size();
        createASTChecker().checkAll(node);
        if (findings == Log.getFindings().size()) {
            createCNNArchPreResolveSymbolChecker().checkAll(architecture);
            if (findings == Log.getFindings().size()) {
                architecture.resolve();
                if (findings == Log.getFindings().size()) {
                    createCNNArchPostResolveSymbolChecker().checkAll(architecture);
                }
            }
        }
    }

    public static void checkAll(CNNArchCompilationUnitSymbol compilationUnit){
        ASTCNNArchNode node = (ASTCNNArchNode) compilationUnit.getAstNode().get();
        int findings = Log.getFindings().size();
        createASTChecker().checkAll(node);
        if (findings == Log.getFindings().size()) {
            createCNNArchPreResolveSymbolChecker().checkAll(compilationUnit);
            if (findings == Log.getFindings().size()) {
                compilationUnit.getArchitecture().resolve();
                if (findings == Log.getFindings().size()) {
                    createCNNArchPostResolveSymbolChecker().checkAll(compilationUnit);
                }
            }
        }
    }

    //checks cocos based on symbols after the resolve method of the ArchitectureSymbol is called
    public static CNNArchSymbolCoCoChecker createCNNArchPostResolveSymbolChecker() {
        return new CNNArchSymbolCoCoChecker()
                .addCoCo(new CheckIOType())
                .addCoCo(new CheckElementInputs())
                .addCoCo(new CheckIOAccessAndIOMissing())
                .addCoCo(new CheckArchitectureFinished());
    }

    //checks cocos based on symbols before the resolve method of the ArchitectureSymbol is called
    public static CNNArchSymbolCoCoChecker createCNNArchPreResolveSymbolChecker() {
        return new CNNArchSymbolCoCoChecker()
                .addCoCo(new CheckIOName())
                .addCoCo(new CheckExpressions());
    }

    //checks all normal cocos
    public static CNNArchCoCoChecker createASTChecker() {
        return new CNNArchCoCoChecker()
                .addCoCo(new CheckLayer())
                .addCoCo(new CheckRangeOperators())
                .addCoCo(new CheckVariableName())
                .addCoCo(new CheckLayerName())
                .addCoCo(new CheckArgument())
                .addCoCo(new CheckLayerRecursion());
    }
}
