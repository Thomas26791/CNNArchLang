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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

abstract public class ArchAbstractSequenceExpression extends ArchExpressionSymbol {


    public ArchAbstractSequenceExpression() {
        super();
    }

    abstract public Optional<List<List<ArchSimpleExpressionSymbol>>> getElements();

    abstract public boolean isParallelSequence();

    abstract public boolean isSerialSequence();

    @Override
    public boolean isSequence(){
        return true;
    }

    @Override
    public Optional<Object> getValue() {
        if (isResolved()){
            List<List<Object>> parallelValues = new ArrayList<>(getParallelLength().get());
            for (List<ArchSimpleExpressionSymbol> serialElements : getElements().get()){
                List<Object> serialValues = new ArrayList<>();
                for (ArchSimpleExpressionSymbol element : serialElements){
                    serialValues.add(element.getValue().get());
                }
                parallelValues.add(serialValues);
            }
            return Optional.of(parallelValues);
        }
        else{
            return Optional.empty();
        }
    }

    @Override
    public boolean isBoolean(){
        return false;
    }

    @Override
    public boolean isNumber(){
        return false;
    }

    @Override
    public boolean isTuple(){
        return false;
    }

}
