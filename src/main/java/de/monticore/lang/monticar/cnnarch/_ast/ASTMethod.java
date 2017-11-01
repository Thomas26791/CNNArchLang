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
package de.monticore.lang.monticar.cnnarch._ast;


import java.util.LinkedList;
import java.util.List;

public interface ASTMethod extends ASTMethodTOP {

    String getName();

    default List<? extends ASTArgumentAssignment> getAssignments() {
        return new LinkedList<>();
    }

    default ASTArgumentRhs get(String lhsName) {
        ASTArgumentRhs rhs = null;
        lhsName = lhsName.replace("_", "");
        for (ASTArgumentAssignment assignment : getAssignments()) {
            String assignmentLhs = assignment.getLhs().name();
            if (assignmentLhs.equalsIgnoreCase(lhsName)) {
                rhs =  assignment.getRhs();
            }
        }
        return rhs;
    }
}
