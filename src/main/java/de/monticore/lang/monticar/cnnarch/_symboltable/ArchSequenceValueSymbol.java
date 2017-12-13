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

public class ArchSequenceValueSymbol extends ArchAbstractSequenceValue {

    private List<List<ArchSimpleValueSymbol>> elements;

    public ArchSequenceValueSymbol(List<List<ArchSimpleValueSymbol>> elements) {
        this.elements = elements;
    }



    public List<List<ArchSimpleValueSymbol>> getElements() {
        return elements;
    }

    public void setElements(List<List<ArchSimpleValueSymbol>> elements) {
        this.elements = elements;
    }

    @Override
    public boolean isSerialSequence(){
        boolean isSerial = !isParallelSequence();
        for (List<ArchSimpleValueSymbol> serialElement : getElements()){
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
            for (List<ArchSimpleValueSymbol> element : getElements()){
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
        ArchSequenceValueSymbol resolvedSymbol = resolve();
        if (resolvedSymbol.isFullyResolved()){
            List<List<Object>> valueLists = new ArrayList<>(4);

            for (List<ArchSimpleValueSymbol> serialList : resolvedSymbol.getElements()) {

                List<Object> values = new ArrayList<>(4);
                for (ArchSimpleValueSymbol element : serialList) {
                    values.add(element.getValue().get());
                }
                valueLists.add(values);
            }


            if (isParallelSequence() || valueLists.isEmpty()) {
                return Optional.of(valueLists);
            }
            else {
                return Optional.of(valueLists.get(0));
            }
        }
        else{
            return Optional.empty();
        }
    }

    @Override
    public ArchSequenceValueSymbol resolve() {
        if (isFullyResolved()){
            return this;
        }
        else {
            boolean isFullyResolved = true;

            List<List<ArchSimpleValueSymbol>> resolvedElements = new ArrayList<>(4);
            for (List<ArchSimpleValueSymbol> serialList : getElements()) {

                List<ArchSimpleValueSymbol> resolvedSerialList = new ArrayList<>(4);
                for (ArchSimpleValueSymbol element : serialList) {

                    ArchSimpleValueSymbol resolvedElement = element.resolve();
                    if (!resolvedElement.isFullyResolved()) {
                        isFullyResolved = false;
                    }
                    resolvedSerialList.add(element.resolve());
                }
                resolvedElements.add(resolvedSerialList);
            }

            ArchSequenceValueSymbol resolvedCopy = new ArchSequenceValueSymbol(resolvedElements);
            resolvedCopy.setFullyResolved(isFullyResolved);
            return resolvedCopy;
        }
    }

}
