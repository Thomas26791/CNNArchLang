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
import de.monticore.symboltable.MutableScope;

import java.util.*;


public class ShapeSymbol extends CommonSymbol {

    public static final ShapeKind KIND = new ShapeKind();

    private int channelIndex = -1;
    private int heightIndex = -1;
    private int widthIndex = -1;

    private List<ArchSimpleExpressionSymbol> dimensions = new ArrayList<>();

    public ShapeSymbol() {
        super("", KIND);
    }

    public int getHeightIndex() {
        return heightIndex;
    }

    protected void setHeightIndex(int heightIndex) {
        this.heightIndex = heightIndex;
    }

    public int getWidthIndex() {
        return widthIndex;
    }

    protected void setWidthIndex(int widthIndex) {
        this.widthIndex = widthIndex;
    }

    public int getChannelIndex() {
        return channelIndex;
    }

    protected void setChannelIndex(int channelIndex) {
        this.channelIndex = channelIndex;
    }

    public ArchSimpleExpressionSymbol getHeightSymbol() {
        if (getHeightIndex() == -1){
            return ArchSimpleExpressionSymbol.of(1);
        }
        return getDimensionSymbols().get(getHeightIndex());
    }

    public ArchSimpleExpressionSymbol getWidthSymbol() {
        if (getWidthIndex() == -1){
            return ArchSimpleExpressionSymbol.of(1);
        }
        return getDimensionSymbols().get(getWidthIndex());
    }

    public ArchSimpleExpressionSymbol getChannelsSymbol() {
        if (getChannelIndex() == -1){
            return ArchSimpleExpressionSymbol.of(1);
        }
        return getDimensionSymbols().get(getChannelIndex());
    }

    public Optional<Integer> getWidth(){
        return getWidthSymbol().getIntValue();
    }

    public Optional<Integer> getHeight(){
        return getHeightSymbol().getIntValue();
    }

    public Optional<Integer> getChannels(){
        return getChannelsSymbol().getIntValue();
    }

    protected void setDimensionSymbols(List<ArchSimpleExpressionSymbol> dimensions) {
        this.dimensions = dimensions;
    }

    public List<ArchSimpleExpressionSymbol> getDimensionSymbols() {
        return dimensions;
    }

    public void setDimensions(List<Integer> dimensionList){
        List<ArchSimpleExpressionSymbol> symbolList = new ArrayList<>(dimensionList.size());
        for (int e : dimensionList){
            symbolList.add(ArchSimpleExpressionSymbol.of(e));
        }
        setDimensionSymbols(symbolList);
    }

    public List<Integer> getDimensions(){
        List<Integer> dimensionList = new ArrayList<>(3);
        for (ArchSimpleExpressionSymbol exp : getDimensionSymbols()){
            dimensionList.add(exp.getIntValue().get());
        }
        return dimensionList;
    }

    public Set<VariableSymbol> resolve() {
        if (!isResolved()){
            if (isResolvable()){
                for (ArchSimpleExpressionSymbol dimension : getDimensionSymbols()){
                    dimension.resolveOrError();
                }
            }
        }
        return getUnresolvableVariables();
    }

    public boolean isResolvable(){
        boolean isResolvable = true;
        for (ArchSimpleExpressionSymbol dimension : getDimensionSymbols()){
            if (!dimension.isResolvable()){
                isResolvable = false;
            }
        }
        return isResolvable;
    }

    public boolean isResolved(){
        boolean isResolved = true;
        for (ArchSimpleExpressionSymbol dimension : getDimensionSymbols()){
            if (!dimension.isResolved()){
                isResolved = false;
            }
        }
        return isResolved;
    }

    public Set<VariableSymbol> getUnresolvableVariables(){
        Set<VariableSymbol> unresolvableVariables = new HashSet<>();
        for (ArchSimpleExpressionSymbol dimension : getDimensionSymbols()){
            unresolvableVariables.addAll(dimension.getUnresolvableVariables());
        }
        return unresolvableVariables;
    }

    public void checkIfResolvable(Set<VariableSymbol> seenVariables) {
        for (ArchSimpleExpressionSymbol dimension : getDimensionSymbols()){
            dimension.checkIfResolvable(seenVariables);
        }
    }

    @Override
    public void setEnclosingScope(MutableScope scope) {
        super.setEnclosingScope(scope);
        for (ArchSimpleExpressionSymbol dimension : getDimensionSymbols()){
            dimension.putInScope(scope);
        }
    }

    public static class Builder{
        private int height = 1;
        private int width = 1;
        private int channels = 1;

        public Builder height(int height){
            this.height = height;
            return this;
        }
        public Builder width(int width){
            this.width = width;
            return this;
        }
        public Builder channels(int channels){
            this.channels = channels;
            return this;
        }

        public ShapeSymbol build(){
            ShapeSymbol sym = new ShapeSymbol();
            sym.setChannelIndex(0);
            sym.setHeightIndex(1);
            sym.setWidthIndex(2);
            sym.setDimensions(Arrays.asList(channels, height, width));
            return sym;
        }
    }
}
