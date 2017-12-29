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

public class ArchRangeExpressionSymbol extends ArchAbstractSequenceExpression {

    private ArchSimpleExpressionSymbol startSymbol;
    private ArchSimpleExpressionSymbol endSymbol;
    private boolean parallel;
    private List<List<ArchSimpleExpressionSymbol>> elements = null;


    public ArchRangeExpressionSymbol() {
        super();
    }

    public ArchSimpleExpressionSymbol getStartSymbol() {
        return startSymbol;
    }

    public void setStartSymbol(ArchSimpleExpressionSymbol startSymbol) {
        this.startSymbol = startSymbol;
    }

    public ArchSimpleExpressionSymbol getEndSymbol() {
        return endSymbol;
    }

    public void setEndSymbol(ArchSimpleExpressionSymbol endSymbol) {
        this.endSymbol = endSymbol;
    }

    protected boolean isParallel() {
        return parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    @Override
    public boolean isParallelSequence() {
        return isParallel();
    }

    @Override
    public boolean isSerialSequence() {
        return !isParallel();
    }

    private Optional<Integer> getLength(){
        Optional<Integer> optLength = Optional.empty();
        if (isResolved()){
            Object startValue = getEndSymbol().getValue().get();
            Object endValue = getEndSymbol().getValue().get();
            if (startValue instanceof Integer && endValue instanceof Integer) {
                int start = (Integer) startValue;
                int end = (Integer) endValue;
                optLength = Optional.of(Math.abs(end - start) + 1);
            }
        }
        return optLength;
    }

    @Override
    public Optional<Integer> getParallelLength() {
        if (isParallelSequence()) {
            return getLength();
        }
        else {
            return Optional.of(1);
        }
    }

    @Override
    public Optional<Integer> getMaxSerialLength() {
        if (isSerialSequence()) {
            return getLength();
        }
        else {
            return Optional.of(1);
        }
    }

    @Override
    public Set<String> resolve(Scope resolvingScope) {
        if (!isResolved()){
            checkIfResolvable();
            if (isResolvable()){

                getStartSymbol().resolveOrError(resolvingScope);
                getEndSymbol().resolveOrError(resolvingScope);
            }
        }
        return getUnresolvableNames();
    }

    @Override
    public boolean isResolved() {
        return getStartSymbol().isResolved() && getEndSymbol().isResolved();
    }

    @Override
    public Optional<List<List<ArchSimpleExpressionSymbol>>> getElements() {
        if (elements == null){
            if (isResolved()){
                int start = startSymbol.getIntValue().get();
                int end = endSymbol.getIntValue().get();
                int step = 1;
                if (end < start){
                    step = -1;
                }

                List<List<ArchSimpleExpressionSymbol>> elementList = new ArrayList<>(getParallelLength().get());
                if (isParallel()){
                    for (int i = start; i <= end; i = i + step){
                        List<ArchSimpleExpressionSymbol> values = new ArrayList<>(1);
                        values.add(ArchSimpleExpressionSymbol.of(i));
                        elementList.add(values);
                    }
                }
                else {
                    List<ArchSimpleExpressionSymbol> values = new ArrayList<>(getMaxSerialLength().get());
                    for (int i = start; i <= end; i = i + step){
                        values.add(ArchSimpleExpressionSymbol.of(i));
                    }
                    elementList.add(values);
                }

                this.elements = elementList;
            }
        }
        return Optional.ofNullable(elements);
    }

    @Override
    protected Set<String> computeUnresolvableNames() {
        Set<String> unresolvableNames = new HashSet<>();
        unresolvableNames.addAll(getStartSymbol().computeUnresolvableNames());
        unresolvableNames.addAll(getEndSymbol().computeUnresolvableNames());
        return unresolvableNames;
    }
}
