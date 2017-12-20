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

import java.util.*;

public class ArchSequenceExpressionSymbol extends ArchAbstractSequenceExpression {

    private List<List<ArchSimpleExpressionSymbol>> elements;

    public ArchSequenceExpressionSymbol() {
        super();
    }


    public List<List<ArchSimpleExpressionSymbol>> getElements() {
        return elements;
    }

    public void setElements(List<List<ArchSimpleExpressionSymbol>> elements) {
        this.elements = elements;
    }

    @Override
    public boolean isSerialSequence(){
        boolean isSerial = !isParallelSequence();
        for (List<ArchSimpleExpressionSymbol> serialElement : getElements()){
            if (serialElement.size() >= 2){
                isSerial = true;
            }
        }
        return isSerial;
    }

    @Override
    public boolean isParallelSequence(){
        return elements.size() >= 2;
    }

    @Override
    public Optional<Integer> getParallelLength() {
        return Optional.of(elements.size());
    }

    @Override
    public Optional<Integer> getSerialLength() {
        if (!elements.isEmpty()){
            int maxLenght = 0;
            for (List<ArchSimpleExpressionSymbol> element : getElements()){
                if (maxLenght < element.size()){
                    maxLenght = element.size();
                }
            }
            return Optional.of(maxLenght);
        }
        else {
            return Optional.of(0);
        }
    }

    @Override
    public Optional<Object> getValue() {
        if (isFullyResolved()){
            List<List<Object>> valueLists = new ArrayList<>(4);

            for (List<ArchSimpleExpressionSymbol> serialList : getElements()) {

                List<Object> values = new ArrayList<>(4);
                for (ArchSimpleExpressionSymbol element : serialList) {
                    values.add(element.getValue().get());
                }
                valueLists.add(values);
            }

            if (valueLists.isEmpty()) {
                valueLists.add(new ArrayList<Object>(2));
            }

            return Optional.of(valueLists);
        }
        else{
            return Optional.empty();
        }
    }

    @Override
    public Set<String> resolve() {
        Set<String> unresolvableSet = new HashSet<>();
        if (!isFullyResolved()){

            for (List<ArchSimpleExpressionSymbol> serialList : getElements()) {
                for (ArchSimpleExpressionSymbol element : serialList) {
                    unresolvableSet.addAll(element.resolve());
                }
            }

            if (unresolvableSet.isEmpty()) {
                setFullyResolved(true);
            }
        }
        return unresolvableSet;
    }

    @Override
    protected void checkIfResolved() {
        boolean isResolved = true;
        for (List<ArchSimpleExpressionSymbol> serialList : getElements()) {
            for (ArchSimpleExpressionSymbol element : serialList) {
                element.checkIfResolved();
                if (!element.isFullyResolved()){
                    isResolved = false;
                }
            }
        }
        setFullyResolved(isResolved);
    }

}
