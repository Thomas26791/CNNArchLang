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

import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedVariables;

import java.util.List;

public class ASTArchSpecialArgument extends ASTArchSpecialArgumentTOP {

    public ASTArchSpecialArgument() {
    }

    public ASTArchSpecialArgument(ASTArchExpression rhs, List<String> nEWLINETOKENs, String serial, String parallel, String conditional) {
        super(rhs, nEWLINETOKENs, serial, parallel, conditional);
    }

    @Override
    public String getName() {
        if (getParallel().isPresent()){
            return AllPredefinedVariables.PARALLEL_ARG_NAME;
        }
        else if (getSerial().isPresent()) {
            return AllPredefinedVariables.SERIAL_ARG_NAME;
        }
        else if (getConditional().isPresent()){
            return AllPredefinedVariables.CONDITIONAL_ARG_NAME;
        }
        else {
            throw new IllegalStateException();
        }
    }

}
