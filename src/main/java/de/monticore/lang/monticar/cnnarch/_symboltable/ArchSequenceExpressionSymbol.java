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

import de.monticore.symboltable.Scope;

import java.util.*;

public class ArchSequenceExpressionSymbol extends ArchAbstractSequenceExpression {

    private List<List<ArchSimpleExpressionSymbol>> elements;

    public ArchSequenceExpressionSymbol() {
        super();
    }


    @Override
    public Optional<List<List<ArchSimpleExpressionSymbol>>> getElements() {
        return Optional.of(_getElements());
    }

    protected List<List<ArchSimpleExpressionSymbol>> _getElements(){
        return elements;
    }

    public void setElements(List<List<ArchSimpleExpressionSymbol>> elements) {
        this.elements = elements;
    }

    @Override
    public boolean isSerialSequence(){
        boolean isSerial = !isParallelSequence();
        for (List<ArchSimpleExpressionSymbol> serialElement : _getElements()){
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
    public Optional<Integer> getMaxSerialLength() {
        if (!elements.isEmpty()){
            int maxLenght = 0;
            for (List<ArchSimpleExpressionSymbol> element : _getElements()){
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
    public Set<String> resolve(Scope resolvingScope) {
        if (!isResolved()){
            checkIfResolvable();
            if (isResolvable()){

                for (List<ArchSimpleExpressionSymbol> serialList : _getElements()) {
                    for (ArchSimpleExpressionSymbol element : serialList) {
                        element.resolveOrError(resolvingScope);
                    }
                }
            }
        }
        return getUnresolvableNames();
    }

    @Override
    public boolean isResolved() {
        boolean isResolved = true;
        for (List<ArchSimpleExpressionSymbol> serialElements : _getElements()){
            for (ArchSimpleExpressionSymbol element : serialElements){
                if (!element.isResolved()){
                    isResolved = false;
                }
            }
        }
        return isResolved;
    }

    @Override
    protected Set<String> computeUnresolvableNames() {
        Set<String> unresolvableNames = new HashSet<>();
        for (List<ArchSimpleExpressionSymbol> serialElements : _getElements()){
            for (ArchSimpleExpressionSymbol element : serialElements){
                unresolvableNames.addAll(element.computeUnresolvableNames());
            }
        }
        return unresolvableNames;
    }
}
