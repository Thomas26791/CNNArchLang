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

import de.monticore.lang.monticar.cnnarch._ast.ASTArchArgument;
import de.monticore.lang.monticar.cnnarch._ast.ASTMethodLayer;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.MethodDeclarationSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.MethodLayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.VariableSymbol;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckMethodLayer implements CNNArchASTMethodLayerCoCo{

    @Override
    public void check(ASTMethodLayer node) {
        Set<String> nameSet = new HashSet<>();
        for (ASTArchArgument argument : node.getArguments()){
            String name = argument.getName();
            if (nameSet.contains(name)){
                Log.error("0" + ErrorCodes.DUPLICATED_ARG + " Duplicated name: " + name +
                                ". Multiple values assigned to the same argument."
                        , argument.get_SourcePositionStart());
            }
            else {
                nameSet.add(name);
            }
        }

        MethodDeclarationSymbol method = ((MethodLayerSymbol) node.getSymbol().get()).getMethod();
        if (method == null){
            ArchitectureSymbol architecture = node.getSymbol().get().getEnclosingScope().<ArchitectureSymbol>resolve("", ArchitectureSymbol.KIND).get();
            Log.error("0" + ErrorCodes.UNKNOWN_METHOD + " Unknown method error. " +
                            "Method with name '" + node.getName() + "' does not exist. " +
                            "Existing methods: " + Joiners.COMMA.join(architecture.getMethodDeclarations()) + "."
                    , node.get_SourcePositionStart());
        }
        else {
            Set<String> requiredArguments = new HashSet<>();
            for (VariableSymbol param : method.getParameters()){
                if (!param.getDefaultExpression().isPresent()){
                    requiredArguments.add(param.getName());
                }
            }
            for (ASTArchArgument argument : node.getArguments()){
                requiredArguments.remove(argument.getName());
            }

            for (String missingArgumentName : requiredArguments){
                Log.error("0"+ErrorCodes.MISSING_ARGUMENT + " Missing argument. " +
                                "The argument '" + missingArgumentName + "' is required."
                        , node.get_SourcePositionStart());
            }
        }
    }

}
