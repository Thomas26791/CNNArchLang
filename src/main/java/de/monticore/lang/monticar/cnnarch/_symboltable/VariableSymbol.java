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
package de.monticore.lang.monticar.cnnarch._symboltable;

import de.monticore.symboltable.CommonSymbol;
import de.monticore.symboltable.SymbolKind;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

import static de.monticore.lang.monticar.cnnarch.ErrorMessages.MISSING_VAR_VALUE_CODE;

public class VariableSymbol extends CommonSymbol {

    public static final VariableKind KIND = new VariableKind();

    private VariableType type;
    private ArchValueSymbol defaultValue = null; //Optional
    private ArchValueSymbol currentValue = null; //Optional


    public VariableSymbol(String name) {
        super(name, KIND);
    }


    public VariableType getType() {
        return type;
    }

    public void setType(VariableType type) {
        this.type = type;
    }

    public Optional<ArchValueSymbol> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public void setDefaultValue(ArchValueSymbol defaultValue) {
        this.defaultValue = defaultValue;
    }

    protected Optional<ArchValueSymbol> getCurrentValue() {
        return Optional.ofNullable(currentValue);
    }

    public boolean isConstant(){
        return type == VariableType.CONSTANT;
    }

    public boolean isIOVariable(){
        return type == VariableType.IOVariable;
    }

    public boolean isParameter(){
        return type == VariableType.PARAMETER;
    }


    public boolean hasValueSymbol(){
        return getCurrentValue().isPresent() || getDefaultValue().isPresent();
    }

    public ArchValueSymbol getValueSymbol(){
        ArchValueSymbol value = null;
        if (hasValueSymbol()){
            if (getCurrentValue().isPresent()){
                value = getCurrentValue().get();
            }
            else {
                value = getDefaultValue().get();
            }
        }
        else {
            String msg = "0" + MISSING_VAR_VALUE_CODE + " The variable " + getName() + " has no value.";
            if (getAstNode().isPresent()){
                Log.error(msg, getAstNode().get().get_SourcePositionStart());
            }
            else {
                Log.error(msg);
            }
        }
        return value;
    }

    public void setValueSymbol(ArchValueSymbol value){
        currentValue = value;
    }

    public void reset(){
        currentValue = null;
    }

}