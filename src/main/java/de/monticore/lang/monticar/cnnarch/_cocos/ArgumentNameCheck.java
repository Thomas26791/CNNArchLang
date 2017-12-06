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

import de.monticore.lang.monticar.cnnarch._ast.ASTArgumentAssignment;
import de.monticore.lang.monticar.cnnarch._ast.ASTMethod;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.Set;

public class ArgumentNameCheck implements CNNArchASTMethodCoCo {

    public static final String PLACEHOLDER_ARG_CODE = "x0303B";
    public static final String DUPLICATE_ARG_CODE = "x03031";
    public static final String PLACEHOLDER_ARG_MSG = "0"+PLACEHOLDER_ARG_CODE+" \"_placeholder\" is not an argument. ";
    public static final String DUPLICATE_ARG_MSG = "0"+DUPLICATE_ARG_CODE+" Multiple assignments of the same argument are not allowed. ";


    @Override
    public void check(ASTMethod node) {
        checkDuplicateArgument(node);
        checkPlaceholderArgument(node);
    }

    public void checkDuplicateArgument(ASTMethod node){
        Set<Enum> set = new HashSet<>();
        for (ASTArgumentAssignment assignment : node.getArgumentListing().getArguments()) {
            if (set.contains(assignment.getLhs())) {
                Log.error(DUPLICATE_ARG_MSG,
                        assignment.get_SourcePositionStart());
            }
            else {
                set.add(assignment.getLhs());
            }
        }
    }

    public void checkPlaceholderArgument(ASTMethod node){
        for(ASTArgumentAssignment assignment:node.getArgumentListing().getArguments()){
            if (assignment.getLhs().name().equals("_placeholder")){
                Log.error(PLACEHOLDER_ARG_MSG,
                        assignment.get_SourcePositionStart());
            }
        }
    }

}
