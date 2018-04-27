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

import de.monticore.lang.monticar.types2._ast.ASTElementType;
import de.monticore.symboltable.CommonSymbol;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;

import java.util.*;


public class ArchTypeSymbol extends CommonSymbol {

    public static final ArchTypeKind KIND = new ArchTypeKind();

    protected static final String DEFAULT_ELEMENT_TYPE = "Q(-oo:oo)";
    private ASTElementType elementType;

    private int channelIndex = -1;
    private int heightIndex = -1;
    private int widthIndex = -1;
    private List<ArchSimpleExpressionSymbol> dimensions = new ArrayList<>();


    public ArchTypeSymbol() {
        super("", KIND);
        ASTElementType elementType = new ASTElementType();
        elementType.setTElementType(DEFAULT_ELEMENT_TYPE);
        setElementType(elementType);
    }

    public ASTElementType getElementType() {
        return elementType;
    }

    public void setElementType(ASTElementType elementType) {
        this.elementType = elementType;
    }

    public int getHeightIndex() {
        return heightIndex;
    }

    public void setHeightIndex(int heightIndex) {
        this.heightIndex = heightIndex;
    }

    public int getWidthIndex() {
        return widthIndex;
    }

    public void setWidthIndex(int widthIndex) {
        this.widthIndex = widthIndex;
    }

    public int getChannelIndex() {
        return channelIndex;
    }

    public void setChannelIndex(int channelIndex) {
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

    public Integer getWidth(){
        return getWidthSymbol().getIntValue().get();
    }

    public Integer getHeight(){
        return getHeightSymbol().getIntValue().get();
    }

    public Integer getChannels(){
        return getChannelsSymbol().getIntValue().get();
    }

    public void setDimensionSymbols(List<ArchSimpleExpressionSymbol> dimensions) {
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

    public void putInScope(Scope scope) {
        Collection<Symbol> symbolsInScope = scope.getLocalSymbols().get(getName());
        if (symbolsInScope == null || !symbolsInScope.contains(this)) {
            scope.getAsMutableScope().add(this);
            for (ArchSimpleExpressionSymbol dimension : getDimensionSymbols()){
                dimension.putInScope(scope);
            }
        }
    }

    public ArchTypeSymbol preResolveDeepCopy() {
        ArchTypeSymbol copy = new ArchTypeSymbol();
        if (getAstNode().isPresent()){
            copy.setAstNode(getAstNode().get());
        }

        copy.setElementType(getElementType());
        copy.setWidthIndex(getWidthIndex());
        copy.setChannelIndex(getChannelIndex());
        copy.setHeightIndex(getHeightIndex());
        List<ArchSimpleExpressionSymbol> dimensionCopies = new ArrayList<>();
        for (ArchSimpleExpressionSymbol dimension : getDimensionSymbols()){
            dimensionCopies.add(dimension.preResolveDeepCopy());
        }
        copy.setDimensionSymbols(dimensionCopies);

        return copy;
    }

    public static class Builder{
        private int height = 1;
        private int width = 1;
        private int channels = 1;
        private ASTElementType elementType = null;

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
        public Builder elementType(ASTElementType elementType){
            this.elementType = elementType;
            return this;
        }
        public Builder elementType(String start, String end){
            elementType = new ASTElementType();
            elementType.setTElementType("Q(" + start + ":" + end +")");
            return this;
        }

        public ArchTypeSymbol build(){
            ArchTypeSymbol sym = new ArchTypeSymbol();
            sym.setChannelIndex(0);
            sym.setHeightIndex(1);
            sym.setWidthIndex(2);
            sym.setDimensions(Arrays.asList(channels, height, width));

            if (elementType == null){
                elementType = new ASTElementType();
                elementType.setTElementType(DEFAULT_ELEMENT_TYPE);
            }
            sym.setElementType(elementType);
            return sym;
        }
    }
}
