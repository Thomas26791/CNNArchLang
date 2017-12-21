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
package de.monticore.lang.monticar.cnnarch;

import de.monticore.lang.monticar.cnnarch._symboltable.MethodDeclarationSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.MethodLayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ShapeSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.VariableSymbol;
import org.jscience.mathematics.number.Rational;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PredefinedMethods {

    public static MethodDeclarationSymbol createFullyConnected(){
        return new MethodDeclarationSymbol.Builder()
                .name("FullyConnected")
                .parameters(
                        new VariableSymbol.Builder()
                                .name("units")
                                .build(),
                        new VariableSymbol.Builder()
                                .name("no_bias")
                                .defaultValue(false)
                                .build()
                )
                .shapeFunction((inputShapes, method) -> Collections.singletonList(new ShapeSymbol.Builder()
                        .height(1)
                        .width(1)
                        .channels(method.getIntValue("units").get())
                        .build()))
                .build();
    }

    public static MethodDeclarationSymbol createConvolution(){
        return new MethodDeclarationSymbol.Builder()
                .name("Convolution")
                .parameters(
                        new VariableSymbol.Builder()
                                .name("kernel")
                                .build(),
                        new VariableSymbol.Builder()
                                .name("channels")
                                .build(),
                        new VariableSymbol.Builder()
                                .name("stride")
                                .defaultValue(1,1)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("no_bias")
                                .defaultValue(false)
                                .build()
                )
                .shapeFunction((inputShapes, method) ->
                        strideShapeFunction(inputShapes.get(0),
                                method,
                                method.getIntValue("channels").get()))
                .build();
    }

    private static List<ShapeSymbol> strideShapeFunction(ShapeSymbol inputShape, MethodLayerSymbol method, int channels){
        int strideHeight = method.getIntTupleValue("stride").get().get(0);
        int strideWidth = method.getIntTupleValue("stride").get().get(1);
        int kernelHeight = method.getIntTupleValue("kernel").get().get(0);
        int kernelWidth = method.getIntTupleValue("kernel").get().get(1);
        int inputHeight = inputShape.getHeight().get();
        int inputWidth = inputShape.getWidth().get();

        //assume padding with border_mode='same'
        int outputWidth = 1 + ((inputWidth - kernelWidth + strideWidth - 1) / strideWidth);
        int outputHeight = 1 + ((inputHeight - kernelHeight + strideHeight - 1) / strideHeight);

        return Collections.singletonList(new ShapeSymbol.Builder()
                .height(outputHeight)
                .width(outputWidth)
                .channels(channels)
                .build());
    }

    public static MethodDeclarationSymbol createSoftmax(){
        return new MethodDeclarationSymbol.Builder()
                .name("Softmax")
                .build();
    }

    public static MethodDeclarationSymbol createSigmoid(){
        return new MethodDeclarationSymbol.Builder()
                .name("Sigmoid")
                .build();
    }

    public static MethodDeclarationSymbol createTanh(){
        return new MethodDeclarationSymbol.Builder()
                .name("Tanh")
                .build();
    }

    public static MethodDeclarationSymbol createRelu(){
        return new MethodDeclarationSymbol.Builder()
                .name("Relu")
                .build();
    }

    public static MethodDeclarationSymbol createDropout(){
        return new MethodDeclarationSymbol.Builder()
                .name("Dropout")
                .parameters(
                        new VariableSymbol.Builder()
                                .name("p")
                                .defaultValue(Rational.valueOf(1,2))//0.5
                                .build()
                )
                .build();
    }

    public static MethodDeclarationSymbol createMaxPooling(){
        return new MethodDeclarationSymbol.Builder()
                .name("MaxPooling")
                .parameters(
                        new VariableSymbol.Builder()
                                .name("kernel")
                                .build(),
                        new VariableSymbol.Builder()
                                .name("stride")
                                .defaultValue(1,1)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("global")
                                .defaultValue(false)
                                .build()
                )
                .shapeFunction((inputShapes, method) ->
                        strideShapeFunction(inputShapes.get(0),
                                method,
                                inputShapes.get(0).getChannels().get()))
                .build();
    }

    public static MethodDeclarationSymbol createAveragePooling(){
        return new MethodDeclarationSymbol.Builder()
                .name("AveragePooling")
                .parameters(
                        new VariableSymbol.Builder()
                                .name("kernel")
                                .build(),
                        new VariableSymbol.Builder()
                                .name("stride")
                                .defaultValue(1,1)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("global")
                                .defaultValue(false)
                                .build()
                )
                .shapeFunction((inputShapes, method) ->
                        strideShapeFunction(inputShapes.get(0),
                                method,
                                inputShapes.get(0).getChannels().get()))
                .build();
    }

    public static MethodDeclarationSymbol createLrn(){
        return new MethodDeclarationSymbol.Builder()
                .name("Lrn")
                .parameters(
                        new VariableSymbol.Builder()
                                .name("nsize")
                                .build(),
                        new VariableSymbol.Builder()
                                .name("knorm")
                                .defaultValue(2)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("alpha")
                                .defaultValue(Rational.valueOf(1,10000))//0.0001
                                .build(),
                        new VariableSymbol.Builder()
                                .name("beta")
                                .defaultValue(Rational.valueOf(3,4))//0.75
                                .build()
                )
                .build();
    }

    public static MethodDeclarationSymbol createBatchNorm(){
        return new MethodDeclarationSymbol.Builder()
                .name("BatchNorm")
                .parameters(
                        //todo
                )
                .build();
    }

    public static MethodDeclarationSymbol createSplit(){
        return new MethodDeclarationSymbol.Builder()
                .name("Split")
                .parameters(
                        new VariableSymbol.Builder()
                                .name("index")
                                .build(),
                        new VariableSymbol.Builder()
                                .name("n")
                                .build()
                )
                .shapeFunction((inputShapes, method) -> splitShapeFunction(inputShapes.get(0), method))
                .build();
    }

    private static List<ShapeSymbol> splitShapeFunction(ShapeSymbol inputShape, MethodLayerSymbol method){
        int numberOfSplits = method.getIntValue("n").get();
        int groupIndex = method.getIntValue("index").get();
        int inputChannels = inputShape.getChannels().get();

        int outputChannels = inputChannels / numberOfSplits;
        int outputChannelsLast = inputChannels - numberOfSplits*outputChannels;

        if (groupIndex == numberOfSplits - 1){
            return Collections.singletonList(new ShapeSymbol.Builder()
                    .height(1)
                    .width(1)
                    .channels(outputChannelsLast)
                    .build());
        }
        else {
            return Collections.singletonList(new ShapeSymbol.Builder()
                    .height(1)
                    .width(1)
                    .channels(outputChannels)
                    .build());
        }
    }

    public static MethodDeclarationSymbol createGet(){
        return new MethodDeclarationSymbol.Builder()
                .name("Get")
                .parameters(
                        new VariableSymbol.Builder()
                                .name("index")
                                .build()
                )
                .shapeFunction((inputShapes, method) ->
                        Collections.singletonList(inputShapes.get(method.getIntValue("index").get())))
                .build();
    }

    public static MethodDeclarationSymbol createAdd(){
        return new MethodDeclarationSymbol.Builder()
                .name("Add")
                .shapeFunction((inputShapes, method) -> Collections.singletonList(inputShapes.get(0)))
                .build();
    }

    public static MethodDeclarationSymbol createConcatenate(){
        return new MethodDeclarationSymbol.Builder()
                .name("Concatenate")
                .shapeFunction(PredefinedMethods::concatenateShapeFunction)
                .build();
    }

    private static List<ShapeSymbol> concatenateShapeFunction(List<ShapeSymbol> inputShapes, MethodLayerSymbol method){
        int channels = 0;
        for (ShapeSymbol inputShape : inputShapes){
            channels += inputShape.getChannels().get();
        }
        return Collections.singletonList(new ShapeSymbol.Builder()
                .height(inputShapes.get(0).getHeight().get())
                .width(inputShapes.get(0).getWidth().get())
                .channels(channels)
                .build());
    }


    public static List<MethodDeclarationSymbol> createList(){
        return Arrays.asList(
                createFullyConnected(),
                createConvolution(),
                createSoftmax(),
                createSigmoid(),
                createTanh(),
                createRelu(),
                createDropout(),
                createMaxPooling(),
                createAveragePooling(),
                createLrn(),
                createBatchNorm(),
                createSplit(),
                createGet(),
                createAdd(),
                createConcatenate());
    }
}
