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

    public static final int BATCH_SIZE_INDEX = 0;
    public static final int HEIGHT_INDEX = 1;
    public static final int WIDTH_INDEX = 2;
    public static final int CHANNEL_INDEX = 3;

    private List<ArchSimpleExpressionSymbol> dimensions =
            Arrays.asList(ArchSimpleExpressionSymbol.of(1),
                    ArchSimpleExpressionSymbol.of(1),
                    ArchSimpleExpressionSymbol.of(1),
                    ArchSimpleExpressionSymbol.of(1));

    public ShapeSymbol() {
        super("", KIND);
    }

    public ArchSimpleExpressionSymbol getBatchSizeSymbol() {
        return dimensions.get(BATCH_SIZE_INDEX);
    }

    public void setBatchSize(int batchSize) {
        dimensions.get(BATCH_SIZE_INDEX).reset();
        dimensions.get(BATCH_SIZE_INDEX).setValue(batchSize);
        dimensions.get(BATCH_SIZE_INDEX).setMathExpression(null);
    }

    protected void setBatchSize(ArchSimpleExpressionSymbol batchSize) {
        getDimensionSymbols().set(BATCH_SIZE_INDEX, batchSize);
    }

    public ArchSimpleExpressionSymbol getHeightSymbol() {
        return dimensions.get(HEIGHT_INDEX);
    }

    public void setHeight(int height) {
        dimensions.get(HEIGHT_INDEX).reset();
        dimensions.get(HEIGHT_INDEX).setValue(height);
        dimensions.get(HEIGHT_INDEX).setMathExpression(null);
    }

    protected void setHeight(ArchSimpleExpressionSymbol height) {
        getDimensionSymbols().set(HEIGHT_INDEX, height);
    }

    public ArchSimpleExpressionSymbol getWidthSymbol() {
        return dimensions.get(WIDTH_INDEX);
    }

    public void setWidth(int width) {
        dimensions.get(WIDTH_INDEX).reset();
        dimensions.get(WIDTH_INDEX).setValue(width);
        dimensions.get(WIDTH_INDEX).setMathExpression(null);
    }

    protected void setWidth(ArchSimpleExpressionSymbol width) {
        getDimensionSymbols().set(WIDTH_INDEX, width);
    }

    public ArchSimpleExpressionSymbol getChannelsSymbol() {
        return dimensions.get(CHANNEL_INDEX);
    }

    public void setChannels(int channels) {
        dimensions.get(CHANNEL_INDEX).reset();
        dimensions.get(CHANNEL_INDEX).setValue(channels);
        dimensions.get(CHANNEL_INDEX).setMathExpression(null);
    }

    protected void setChannels(ArchSimpleExpressionSymbol channels) {
        getDimensionSymbols().set(CHANNEL_INDEX, channels);
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

    public List<ArchSimpleExpressionSymbol> getDimensionSymbols() {
        return dimensions;
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
        private int height;
        private int width;
        private int channels;

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
            sym.setHeight(height);
            sym.setChannels(channels);
            sym.setWidth(width);
            return sym;
        }
    }
}
