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
package de.monticore.lang.monticar.cnnarch;

import de.monticore.lang.monticar.cnnarch._symboltable.VariableSymbol;

public class PredefinedVariables {

    public static final String IF_NAME = "_if";
    public static final String FOR_NAME = "_for";

    public static VariableSymbol createIfParameter(){
        return new VariableSymbol.Builder()
                .name(IF_NAME)
                .constraints(Constraint.BOOLEAN)
                .defaultValue(true)
                .build();
    }

    public static VariableSymbol createForParameter(){
        return new VariableSymbol.Builder()
                .name(FOR_NAME)
                .defaultValue(1)
                .build();
    }

    //todo true and false
}
