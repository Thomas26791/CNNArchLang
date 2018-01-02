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

import java.util.*;

public class PredefinedMethods {

    public static final String FULLY_CONNECTED_NAME = "FullyConnected";
    public static final String CONVOLUTION_NAME = "Convolution";
    public static final String SOFTMAX_NAME = "Softmax";
    public static final String SIGMOID_NAME = "Sigmoid";
    public static final String TANH_NAME = "Tanh";
    public static final String RELU_NAME = "Relu";
    public static final String DROPOUT_NAME = "Dropout";
    public static final String MAX_POOLING_NAME = "MaxPooling";
    public static final String AVG_POOLING_NAME = "AveragePooling";
    public static final String LRN_NAME = "Lrn";
    public static final String BATCHNORM_NAME = "BatchNorm";
    public static final String SPLIT_NAME = "Split";
    public static final String GET_NAME = "Get";
    public static final String ADD_NAME = "Add";
    public static final String CONCATENATE_NAME = "Concatenate";

    public static final List<String> NAME_LIST = Arrays.asList(
            FULLY_CONNECTED_NAME,
            CONVOLUTION_NAME,
            SOFTMAX_NAME,
            SIGMOID_NAME,
            TANH_NAME,
            RELU_NAME,
            DROPOUT_NAME,
            MAX_POOLING_NAME,
            AVG_POOLING_NAME,
            LRN_NAME,
            BATCHNORM_NAME,
            SPLIT_NAME,
            GET_NAME,
            ADD_NAME,
            CONCATENATE_NAME);


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


    public static MethodDeclarationSymbol createFullyConnected(){
        return new MethodDeclarationSymbol.Builder()
                .name(FULLY_CONNECTED_NAME)
                .parameters(
                        new VariableSymbol.Builder()
                                .name("units")
                                .constraints(Constraint.INTEGER, Constraint.POSITIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("no_bias")
                                .constraints(Constraint.BOOLEAN)
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
                .name(CONVOLUTION_NAME)
                .parameters(
                        new VariableSymbol.Builder()
                                .name("kernel")
                                .constraints(Constraint.INTEGER_TUPLE, Constraint.POSITIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("channels")
                                .constraints(Constraint.INTEGER, Constraint.POSITIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("stride")
                                .constraints(Constraint.INTEGER_TUPLE, Constraint.POSITIVE)
                                .defaultValue(Arrays.asList(1, 1))
                                .build(),
                        new VariableSymbol.Builder()
                                .name("no_bias")
                                .constraints(Constraint.BOOLEAN)
                                .defaultValue(false)
                                .build()
                )
                .shapeFunction((inputShapes, method) ->
                        strideShapeFunction(inputShapes.get(0),
                                method,
                                method.getIntValue("channels").get()))
                .build();
    }

    public static MethodDeclarationSymbol createSoftmax(){
        return new MethodDeclarationSymbol.Builder()
                .name(SOFTMAX_NAME)
                .shapeFunction((inputShapes, method) -> inputShapes)
                .build();
    }

    public static MethodDeclarationSymbol createSigmoid(){
        return new MethodDeclarationSymbol.Builder()
                .name(SIGMOID_NAME)
                .shapeFunction((inputShapes, method) -> inputShapes)
                .build();
    }

    public static MethodDeclarationSymbol createTanh(){
        return new MethodDeclarationSymbol.Builder()
                .name(TANH_NAME)
                .shapeFunction((inputShapes, method) -> inputShapes)
                .build();
    }

    public static MethodDeclarationSymbol createRelu(){
        return new MethodDeclarationSymbol.Builder()
                .name(RELU_NAME)
                .shapeFunction((inputShapes, method) -> inputShapes)
                .build();
    }

    public static MethodDeclarationSymbol createDropout(){
        return new MethodDeclarationSymbol.Builder()
                .name(DROPOUT_NAME)
                .parameters(
                        new VariableSymbol.Builder()
                                .name("p")
                                .constraints(Constraint.NUMBER, Constraint.BETWEEN_ZERO_AND_ONE)
                                .defaultValue(0.5)
                                .build()
                )
                .shapeFunction((inputShapes, method) -> inputShapes)
                .build();
    }

    public static MethodDeclarationSymbol createMaxPooling(){
        return new MethodDeclarationSymbol.Builder()
                .name(MAX_POOLING_NAME)
                .parameters(
                        new VariableSymbol.Builder()
                                .name("kernel")
                                .constraints(Constraint.INTEGER_TUPLE, Constraint.POSITIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("stride")
                                .constraints(Constraint.INTEGER_TUPLE, Constraint.POSITIVE)
                                .defaultValue(Arrays.asList(1, 1))
                                .build(),
                        new VariableSymbol.Builder()
                                .name("global")
                                .constraints(Constraint.BOOLEAN)
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
                .name(AVG_POOLING_NAME)
                .parameters(
                        new VariableSymbol.Builder()
                                .name("kernel")
                                .constraints(Constraint.INTEGER_TUPLE, Constraint.POSITIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("stride")
                                .constraints(Constraint.INTEGER_TUPLE, Constraint.POSITIVE)
                                .defaultValue(Arrays.asList(1, 1))
                                .build(),
                        new VariableSymbol.Builder()
                                .name("global")
                                .constraints(Constraint.BOOLEAN)
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
                .name(LRN_NAME)
                .parameters(
                        new VariableSymbol.Builder()
                                .name("nsize")
                                .constraints(Constraint.INTEGER, Constraint.NON_NEGATIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("knorm")
                                .constraints(Constraint.NUMBER)
                                .defaultValue(2)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("alpha")
                                .constraints(Constraint.NUMBER)
                                .defaultValue(0.0001)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("beta")
                                .constraints(Constraint.NUMBER)
                                .defaultValue(0.75)
                                .build()
                )
                .shapeFunction((inputShapes, method) -> inputShapes)
                .build();
    }

    public static MethodDeclarationSymbol createBatchNorm(){
        return new MethodDeclarationSymbol.Builder()
                .name(BATCHNORM_NAME)
                .parameters(
                        new VariableSymbol.Builder()
                                .name("fix_gamma")
                                .constraints(Constraint.BOOLEAN)
                                .defaultValue(true)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("axis")
                                .constraints(Constraint.INTEGER, Constraint.NON_NEGATIVE)
                                .defaultValue(ShapeSymbol.CHANNEL_INDEX)
                                .build()
                )
                .shapeFunction((inputShapes, method) -> inputShapes)
                .build();
    }

    public static MethodDeclarationSymbol createSplit(){
        return new MethodDeclarationSymbol.Builder()
                .name(SPLIT_NAME)
                .parameters(
                        new VariableSymbol.Builder()
                                .name("index")
                                .constraints(Constraint.INTEGER, Constraint.NON_NEGATIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name("n")
                                .constraints(Constraint.INTEGER, Constraint.POSITIVE)
                                .build()
                )
                .shapeFunction((inputShapes, method) -> splitShapeFunction(inputShapes.get(0), method))
                .build();
    }

    public static MethodDeclarationSymbol createGet(){
        return new MethodDeclarationSymbol.Builder()
                .name(GET_NAME)
                .parameters(
                        new VariableSymbol.Builder()
                                .name("index")
                                .constraints(Constraint.INTEGER, Constraint.NON_NEGATIVE)
                                .build()
                )
                .shapeFunction((inputShapes, method) ->
                        Collections.singletonList(inputShapes.get(method.getIntValue("index").get())))
                .build();
    }

    public static MethodDeclarationSymbol createAdd(){
        return new MethodDeclarationSymbol.Builder()
                .name(ADD_NAME)
                .shapeFunction((inputShapes, method) -> Collections.singletonList(inputShapes.get(0)))
                .build();
    }

    public static MethodDeclarationSymbol createConcatenate(){
        return new MethodDeclarationSymbol.Builder()
                .name(CONCATENATE_NAME)
                .shapeFunction(PredefinedMethods::concatenateShapeFunction)
                .build();
    }

    private static List<ShapeSymbol> strideShapeFunction(ShapeSymbol inputShape, MethodLayerSymbol method, int channels) {
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

    private static List<ShapeSymbol> splitShapeFunction(ShapeSymbol inputShape, MethodLayerSymbol method) {
        int numberOfSplits = method.getIntValue("n").get();
        int groupIndex = method.getIntValue("index").get();
        int inputChannels = inputShape.getChannels().get();

        int outputChannels = inputChannels / numberOfSplits;
        int outputChannelsLast = inputChannels - numberOfSplits * outputChannels;

        if (groupIndex == numberOfSplits - 1) {
            return Collections.singletonList(new ShapeSymbol.Builder()
                    .height(1)
                    .width(1)
                    .channels(outputChannelsLast)
                    .build());
        } else {
            return Collections.singletonList(new ShapeSymbol.Builder()
                    .height(1)
                    .width(1)
                    .channels(outputChannels)
                    .build());
        }
    }

    private static List<ShapeSymbol> concatenateShapeFunction(List<ShapeSymbol> inputShapes, MethodLayerSymbol method) {
        int channels = 0;
        for (ShapeSymbol inputShape : inputShapes) {
            channels += inputShape.getChannels().get();
        }
        return Collections.singletonList(new ShapeSymbol.Builder()
                .height(inputShapes.get(0).getHeight().get())
                .width(inputShapes.get(0).getWidth().get())
                .channels(channels)
                .build());
    }
}
