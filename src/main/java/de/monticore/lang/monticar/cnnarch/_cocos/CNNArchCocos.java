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
import de.se_rwth.commons.logging.Log;

//check all cocos
public class CNNArchCocos {

    public static void checkAll(ArchitectureSymbol architecture){
        ASTCNNArchNode node = (ASTCNNArchNode) architecture.getAstNode().get();
        createPreResolveChecker().checkAll(node);
        if (Log.getFindings().isEmpty()){
            architecture.resolve();
            if (Log.getFindings().isEmpty()){
                createPostResolveChecker().checkAll(node);
            }
        }
    }

    public static CNNArchCoCoChecker createPostResolveChecker() {
        return new CNNArchCoCoChecker()
                .addCoCo(new CheckIOType())
                .addCoCo(new CheckLayerInputs())
                .addCoCo(new CheckIOAccessAndIOMissing())
                .addCoCo(new CheckUnusedASTIODeclaration())
                .addCoCo(new CheckArchitectureFinished());
    }

    public static CNNArchCoCoChecker createPreResolveChecker() {
        return new CNNArchCoCoChecker()
                .addCoCo(new CheckIOName())
                .addCoCo(new CheckNameExpression())
                .addCoCo(new CheckMethodLayer())
                .addCoCo(new CheckRangeOperators())
                .addCoCo(new CheckVariableName())
                .addCoCo(new CheckMethodName())
                .addCoCo(new CheckArgument())
                .addCoCo(new CheckMethodRecursion());
    }
}
