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

import java.util.Optional;

public class ArchRangeValueSymbol extends ArchAbstractSequenceValue {

    private ArchSimpleValueSymbol startSymbol;
    private ArchSimpleValueSymbol endSymbol;
    private boolean parallel;

    public ArchRangeValueSymbol() {
        super();
    }

    public ArchRangeValueSymbol(ArchSimpleValueSymbol startSymbol, ArchSimpleValueSymbol endSymbol, boolean parallel) {
        this.startSymbol = startSymbol;
        this.endSymbol = endSymbol;
        this.parallel = parallel;
    }

    public ArchSimpleValueSymbol getStartSymbol() {
        return startSymbol;
    }

    public void setStartSymbol(ArchSimpleValueSymbol startSymbol) {
        this.startSymbol = startSymbol;
    }

    public ArchSimpleValueSymbol getEndSymbol() {
        return endSymbol;
    }

    public void setEndSymbol(ArchSimpleValueSymbol endSymbol) {
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
        ArchRangeValueSymbol resolvedSymbol = resolve();
        if (resolvedSymbol.isFullyResolved()){
            Object startValue = resolvedSymbol.getEndSymbol().getValue().get();
            Object endValue = resolvedSymbol.getEndSymbol().getValue().get();
            if (startValue instanceof Integer && endValue instanceof Integer){
                int start = (Integer)startValue;
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
        return null;
    }

    @Override
    public ArchRangeValueSymbol resolve() {
        return null;
    }
}
