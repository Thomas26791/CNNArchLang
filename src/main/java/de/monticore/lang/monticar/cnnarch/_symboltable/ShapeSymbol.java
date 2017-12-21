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

import java.util.Optional;


public class ShapeSymbol extends CommonSymbol {

    public static final ShapeKind KIND = new ShapeKind();

    private DimensionSymbol heightSymbol;
    private DimensionSymbol widthSymbol;
    private DimensionSymbol channelsSymbol;

    public ShapeSymbol() {
        super("", KIND);
    }

    public DimensionSymbol getHeightSymbol() {
        return heightSymbol;
    }

    public void setHeightSymbol(DimensionSymbol heightSymbol) {
        this.heightSymbol = heightSymbol;
    }

    public DimensionSymbol getWidthSymbol() {
        return widthSymbol;
    }

    public void setWidthSymbol(DimensionSymbol widthSymbol) {
        this.widthSymbol = widthSymbol;
    }

    public DimensionSymbol getChannelsSymbol() {
        return channelsSymbol;
    }

    public void setChannelsSymbol(DimensionSymbol channelsSymbol) {
        this.channelsSymbol = channelsSymbol;
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


    public static class Builder{
        private DimensionSymbol height = DimensionSymbol.of(1);
        private DimensionSymbol width = DimensionSymbol.of(1);
        private DimensionSymbol channels = DimensionSymbol.of(1);

        public Builder height(int height){
            this.height = DimensionSymbol.of(height);
            return this;
        }

        public Builder width(int width){
            this.width = DimensionSymbol.of(width);
            return this;
        }

        public Builder channels(int channels){
            this.channels = DimensionSymbol.of(channels);
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
