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

import de.monticore.ast.ASTNode;
import de.monticore.lang.monticar.cnnarch._ast.*;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

public class ArgumentMissingCheck implements CNNArchASTMethodCoCo {

    public static final String MISSING_ARG_CODE = "x06021";
    public static final String MISSING_ARG_MSG = "0"+MISSING_ARG_CODE+" Missing argument. ";

    public void check(ASTMethod node){
        boolean isInOutput = false;

        //check if the the node is in a OutputStructure scope
        if (node.getEnclosingScope().isPresent()){
            Optional<? extends ASTNode> optAst = node.getEnclosingScope().get().getAstNode();
            if (optAst.isPresent() && optAst.get() instanceof ASTOutputStructure){
                isInOutput = true;
            }
        }

        //switch(node)
        if (node instanceof ASTFullyConnectedMethod){
            Optional<ASTArgumentRhs> unitArg = node.getArgumentListing().getArgument(ASTFullyConnectedArgument.UNITS);

            if (!unitArg.isPresent() && !isInOutput){
                Log.error(MISSING_ARG_MSG + "Argument 'units' is required. ", node.get_SourcePositionEnd());
            }
        }
        else if (node instanceof ASTConvolutionMethod){
            Optional<ASTArgumentRhs> kernelArg = node.getArgumentListing().getArgument(ASTConvolutionArgument.KERNEL);
            Optional<ASTArgumentRhs> filtersArg = node.getArgumentListing().getArgument(ASTConvolutionArgument.FILTERS);

            if (!kernelArg.isPresent()){
                Log.error(MISSING_ARG_MSG + "Argument 'kernel' is required. ", node.get_SourcePositionEnd());
            }
            if (!filtersArg.isPresent()){
                Log.error(MISSING_ARG_MSG + "Argument 'filters' is required. ", node.get_SourcePositionEnd());
            }
        }
        else if (node instanceof ASTPoolingMethod){
            Optional<ASTArgumentRhs> kernelArg = node.getArgumentListing().getArgument(ASTPoolingArgument.KERNEL);
            Optional<ASTArgumentRhs> globalArg = node.getArgumentListing().getArgument(ASTPoolingArgument.GLOBAL);

            if (!kernelArg.isPresent() && !globalArg.isPresent()){
                Log.error(MISSING_ARG_MSG + "Argument 'kernel' or 'global' is required. ", node.get_SourcePositionEnd());
            }
        }
        else if (node instanceof ASTLrnMethod){
            Optional<ASTArgumentRhs> nsizeArg = node.getArgumentListing().getArgument(ASTLrnArgument.NSIZE);

            if (!nsizeArg.isPresent()){
                Log.error(MISSING_ARG_MSG + "Argument 'nsize' is required. ", node.get_SourcePositionEnd());
            }
        }

    }

}
