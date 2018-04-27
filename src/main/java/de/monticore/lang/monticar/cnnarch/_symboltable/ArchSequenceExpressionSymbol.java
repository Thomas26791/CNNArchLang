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

import com.google.common.base.Joiner;
import de.monticore.symboltable.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ArchSequenceExpressionSymbol extends ArchAbstractSequenceExpression {

    private List<List<ArchSimpleExpressionSymbol>> elements;

    protected ArchSequenceExpressionSymbol() {
        super();
    }


    @Override
    public Optional<List<List<ArchSimpleExpressionSymbol>>> getElements() {
        return Optional.of(_getElements());
    }

    protected List<List<ArchSimpleExpressionSymbol>> _getElements(){
        return elements;
    }

    protected void setElements(List<List<ArchSimpleExpressionSymbol>> elements) {
        this.elements = elements;
    }

    @Override
    public void reset() {
        for (List<ArchSimpleExpressionSymbol> serialElements : _getElements()){
            for (ArchSimpleExpressionSymbol element : serialElements){
                element.reset();
            }
        }
        setUnresolvableVariables(null);
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
    public Set<VariableSymbol> resolve() {
        if (!isResolved()) {
            if (isResolvable()) {

                for (List<ArchSimpleExpressionSymbol> serialList : _getElements()) {
                    for (ArchSimpleExpressionSymbol element : serialList) {
                        element.resolveOrError();
                    }
                }
            }
        }
        return getUnresolvableVariables();
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
    protected void computeUnresolvableVariables(Set<VariableSymbol> unresolvableVariables, Set<VariableSymbol> allVariables) {
        for (List<ArchSimpleExpressionSymbol> serialElements : _getElements()){
            for (ArchSimpleExpressionSymbol element : serialElements){
                element.checkIfResolvable(allVariables);
                unresolvableVariables.addAll(element.getUnresolvableVariables());
            }
        }
    }

    @Override
    public String getTextualRepresentation() {
        List<String> parallelExpressions = new ArrayList<>();
        for ( List<ArchSimpleExpressionSymbol> serialElements : _getElements()){
            parallelExpressions.add(Joiner.on("->").skipNulls().join(serialElements.stream().map(ArchSimpleExpressionSymbol::getTextualRepresentation).collect(Collectors.toList())));
        }
        return Joiner.on('|').skipNulls().join(parallelExpressions);
    }

    @Override
    protected void putInScope(Scope scope) {
        super.putInScope(scope);
        for (List<ArchSimpleExpressionSymbol> serialList : _getElements()){
            for (ArchSimpleExpressionSymbol element : serialList){
                element.putInScope(scope);
            }
        }
    }

    @Override
    public ArchSequenceExpressionSymbol preResolveDeepCopy(){
        ArchSequenceExpressionSymbol copy = new ArchSequenceExpressionSymbol();
        if (getAstNode().isPresent()){
            copy.setAstNode(getAstNode().get());
        }
        List<List<ArchSimpleExpressionSymbol>> elementsCopy = new ArrayList<>(_getElements().size());
        for (List<ArchSimpleExpressionSymbol> serialList : _getElements()){
            List<ArchSimpleExpressionSymbol> serialListCopy = new ArrayList<>(serialList.size());
            for (ArchSimpleExpressionSymbol element : serialList){
                serialListCopy.add(element.preResolveDeepCopy());
            }
            elementsCopy.add(serialListCopy);
        }
        copy.setElements(elementsCopy);
        return copy;
    }


    public static ArchSequenceExpressionSymbol of(List<List<ArchSimpleExpressionSymbol>> elements){
        ArchSequenceExpressionSymbol sym = new ArchSequenceExpressionSymbol();
        sym.setElements(elements);
        return sym;
    }
}
