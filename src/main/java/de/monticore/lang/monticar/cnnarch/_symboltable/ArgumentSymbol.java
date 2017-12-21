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
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;

public class ArgumentSymbol extends CommonSymbol {

    public static final ArgumentKind KIND = new ArgumentKind();

    private VariableSymbol parameter;
    private ArchExpressionSymbol rhs;

    protected ArgumentSymbol(String name) {
        super(name, KIND);
    }

    public VariableSymbol getParameter() {
        if (parameter == null){
            Optional<VariableSymbol> optParam = getEnclosingScope().resolve(getName(), VariableSymbol.KIND);
            if (optParam.isPresent()){
                parameter = optParam.get();
            }
            else {
                Log.error("Parameter with name " + getName() + " could not be resolved", getSourcePosition());
            }
        }
        return parameter;
    }

    protected void setParameter(VariableSymbol parameter) {
        this.parameter = parameter;
    }

    public ArchExpressionSymbol getRhs() {
        return rhs;
    }

    public Optional<Object> getValue(){
        return getRhs().getValue();
    }

    protected void setRhs(ArchExpressionSymbol rhs) {
        this.rhs = rhs;
    }

    public List<List<ArgumentSymbol>> split(){
        //todo
        return null;
    }

    public List<List<ArgumentSymbol>> expandedSplit(int parallelLength, int serialLength){
        //todo
        return null;
    }


    public static class Builder{
        private VariableSymbol parameter;
        private ArchExpressionSymbol value;
        private String name;

        //will be assigned automatically by name if not set
        public Builder parameter(VariableSymbol parameter) {
            this.parameter = parameter;
            return this;
        }

        public Builder value(ArchExpressionSymbol value) {
            this.value = value;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public ArgumentSymbol build(){
            if (name == null || name.equals("")){
                throw new IllegalStateException("Missing name for ArgumentSymbol");
            }
            ArgumentSymbol sym = new ArgumentSymbol(name);
            sym.setParameter(parameter);
            sym.setRhs(value);
            return sym;
        }
    }
}
