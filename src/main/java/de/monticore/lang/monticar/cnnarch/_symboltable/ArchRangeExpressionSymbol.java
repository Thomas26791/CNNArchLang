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

public class ArchRangeExpressionSymbol extends ArchAbstractSequenceExpression {

    private ArchSimpleExpressionSymbol startSymbol;
    private ArchSimpleExpressionSymbol endSymbol;
    private boolean parallel;


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
        if (isFullyResolved()) {
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
    public Optional<Integer> getSerialLength() {
        if (isSerialSequence()) {
            return getLength();
        }
        else {
            return Optional.of(1);
        }
    }

    @Override
    public Optional<Object> getValue() {
        if (isFullyResolved()){
            //todo check in CoCo: startSymbol.isInt() && endSymbol.isInt()
            int startInt = (Integer) startSymbol.getValue().get();
            int endInt = (Integer) endSymbol.getValue().get();
            int step = 1;
            if (endInt < startInt){
                step = -1;
            }
            List<List<Integer>> valueLists = new ArrayList<>();

            if (isParallel()){
                for (int i = startInt; i <= endInt; i = i + step){
                    List<Integer> values = new ArrayList<>(1);
                    values.add(i);
                    valueLists.add(values);
                }
            }
            else {
                List<Integer> values = new ArrayList<>();
                for (int i = startInt; i <= endInt; i = i + step){
                    values.add(i);
                }
                valueLists.add(values);
            }

            return Optional.of(valueLists);
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public Set<String> resolve() {
        Set<String> unresolvableSet = new HashSet<>();
        if (!isFullyResolved()){

            unresolvableSet.addAll(startSymbol.resolve());
            unresolvableSet.addAll(endSymbol.resolve());

            if (unresolvableSet.isEmpty()) {
                setFullyResolved(true);
            }
        }
        return unresolvableSet;
    }

    @Override
    protected void checkIfResolved() {
        startSymbol.checkIfResolved();
        endSymbol.checkIfResolved();
        setFullyResolved(startSymbol.isFullyResolved() && endSymbol.isFullyResolved());
    }

    @Override
    public boolean isResolved() {
        //todo
        return false;
    }

    @Override
    public List<List<ArchSimpleExpressionSymbol>> getElements() {
        //todo
        return null;
    }
}
