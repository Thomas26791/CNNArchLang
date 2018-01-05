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

import de.monticore.lang.monticar.cnnarch.helper.PredefinedVariables;

public class ASTPredefinedArgument extends ASTPredefinedArgumentTOP {

    public ASTPredefinedArgument() {
    }

    public ASTPredefinedArgument(ASTArchExpression rhs2, String serial, String parallel, String name, ASTArchExpression rhs) {
        super(rhs2, serial, parallel, name, rhs);
    }

    @Override
    public void setRhs2(ASTArchExpression rhs2) {
        super.setRhs2(rhs2);
        setRhs(rhs2);
    }

    @Override
    public void setParallel(String parallel) {
        super.setParallel(parallel);
        if (parallel != null && !parallel.isEmpty()){
            setName(PredefinedVariables.CARDINALITY_NAME);
        }
    }

    @Override
    public void setSerial(String serial) {
        super.setSerial(serial);
        if (serial != null && !serial.isEmpty()) {
            setName(PredefinedVariables.FOR_NAME);
        }
    }
}
