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

import java.util.Optional;

public class ASTArgumentRhs extends ASTArgumentRhsTOP{

    private boolean containsBoolean;

    public ASTArgumentRhs() {
    }

    public ASTArgumentRhs(String stringVal, String intVal, String doubleVal, ASTIntTuple intTuple, String refOrBool) {
        super(stringVal, intVal, doubleVal, intTuple, refOrBool);
    }

    @Override
    public void setRefOrBool(String refOrBool) {
        if (refOrBool.equalsIgnoreCase("true")
                || refOrBool.equalsIgnoreCase("false")){
            containsBoolean = true;
            super.setRefOrBool(refOrBool.toLowerCase());
        }
        else {
            containsBoolean = false;
            super.setRefOrBool(refOrBool);
        }
    }

    public Optional<String> getRef(){
        if (containsBoolean) {
            return Optional.empty();
        }
        else {
            return getRefOrBool();
        }
    }

    public Optional<String> getBooleanVal(){
        if (containsBoolean) {
            return getRefOrBool();
        }
        else {
            return Optional.empty();
        }
    }
}