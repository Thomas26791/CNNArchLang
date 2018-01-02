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

import java.util.*;


public class ShapeSymbol extends CommonSymbol {

    public static final ShapeKind KIND = new ShapeKind();

    public static final int HEIGHT_INDEX = 1;
    public static final int WIDTH_INDEX = 2;
    public static final int CHANNEL_INDEX = 3;

    private List<DimensionSymbol> dimensions = Arrays.asList(DimensionSymbol.of(-1),DimensionSymbol.of(-1), DimensionSymbol.of(-1), DimensionSymbol.of(-1));

    public ShapeSymbol() {
        super("", KIND);
    }

    public DimensionSymbol getHeightSymbol() {
        return dimensions.get(HEIGHT_INDEX);
    }

    public void setHeightSymbol(DimensionSymbol heightSymbol) {
        dimensions.set(HEIGHT_INDEX, heightSymbol);
    }

    public DimensionSymbol getWidthSymbol() {
        return dimensions.get(WIDTH_INDEX);
    }

    public void setWidthSymbol(DimensionSymbol widthSymbol) {
        dimensions.set(WIDTH_INDEX, widthSymbol);
    }

    public DimensionSymbol getChannelsSymbol() {
        return dimensions.get(CHANNEL_INDEX);
    }

    public void setChannelsSymbol(DimensionSymbol channelsSymbol) {
        dimensions.set(CHANNEL_INDEX, channelsSymbol);
    }

    public Optional<Integer> getWidth(){
        return getWidthSymbol().getValue();
    }

    public Optional<Integer> getHeight(){
        return getHeightSymbol().getValue();
    }

    public Optional<Integer> getChannels(){
        return getChannelsSymbol().getValue();
    }

    public List<DimensionSymbol> getDimensionSymbols() {
        return dimensions;
    }


    public List<VariableSymbol> getIOVariables(){
        List<VariableSymbol> vars = new ArrayList<>(4);
        for (DimensionSymbol dim : getDimensionSymbols()){
            if (dim.getIoVariable().isPresent()){
                vars.add(dim.getIoVariable().get());
            }
        }
        return vars;
    }

    public Set<String> computeUnresolvableNames(){
        Set<String> unresolvableNames = new HashSet<>();
        for (VariableSymbol variable : getIOVariables()){
            if (!variable.hasValue()){
                unresolvableNames.add(variable.getName());
            }
        }
        return unresolvableNames;
    }

    public Set<String> resolve() {
        if (!isResolved()){
            if (isResolvable()){
                for (DimensionSymbol dimension : getDimensionSymbols()){
                    dimension.getValueExpression().resolveOrError();
                }
            }
        }
        return getUnresolvableNames();
    }

    public boolean isResolvable(){
        boolean isResolvable = true;
        for (DimensionSymbol dimension : getDimensionSymbols()){
            if (!dimension.getValueExpression().isResolvable()){
                isResolvable = false;
            }
        }
        return isResolvable;
    }

    public boolean isResolved(){
        boolean isResolved = true;
        for (DimensionSymbol dimension : getDimensionSymbols()){
            if (!dimension.getValueExpression().isResolved()){
                isResolved = false;
            }
        }
        return isResolved;
    }

    public Set<String> getUnresolvableNames(){
        Set<String> unresolvableNames = new HashSet<>();
        for (DimensionSymbol dimension : getDimensionSymbols()){
            unresolvableNames.addAll(dimension.getValueExpression().getUnresolvableNames());
        }
        return unresolvableNames;
    }

    public static class Builder{
        private DimensionSymbol height = DimensionSymbol.of(1);
        private DimensionSymbol width = DimensionSymbol.of(1);
        private DimensionSymbol channels = DimensionSymbol.of(1);

        public Builder height(int height){
            this.height = DimensionSymbol.of(height);
            return this;
        }

        public Builder height(DimensionSymbol height){
            this.height = height;
            return this;
        }

        public Builder width(int width){
            this.width = DimensionSymbol.of(width);
            return this;
        }

        public Builder width(DimensionSymbol width){
            this.width = width;
            return this;
        }

        public Builder channels(int channels){
            this.channels = DimensionSymbol.of(channels);
            return this;
        }

        public Builder channels(DimensionSymbol channels){
            this.channels = channels;
            return this;
        }

        public ShapeSymbol build(){
            ShapeSymbol sym = new ShapeSymbol();
            sym.setHeightSymbol(height);
            sym.setChannelsSymbol(channels);
            sym.setWidthSymbol(width);
            return sym;
        }
    }
}
