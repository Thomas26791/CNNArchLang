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
package de.monticore.lang.monticar.cnnarch.helper;

import de.monticore.lang.monticar.cnnarch._symboltable.MethodDeclarationSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.MethodLayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ShapeSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.VariableSymbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PredefinedMethods {

    //predefined method names
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
    public static final String SPLIT_NAME = "SplitData";
    public static final String GET_NAME = "Get";
    public static final String ADD_NAME = "Add";
    public static final String CONCATENATE_NAME = "Concatenate";

    //predefined argument names
    public static final String KERNEL_NAME = "kernel";
    public static final String CHANNELS_NAME = "channels";
    public static final String STRIDE_NAME = "stride";
    public static final String UNITS_NAME = "units";
    public static final String NOBIAS_NAME = "no_bias";
    public static final String GLOBAL_NAME = "global";
    public static final String P_NAME = "p";
    public static final String INDEX_NAME = "index";
    public static final String N_NAME = "n";
    public static final String AXIS_NAME = "axis";
    public static final String FIX_GAMMA_NAME = "fix_gamma";
    public static final String NSIZE_NAME = "nsize";
    public static final String KNORM_NAME = "knorm";
    public static final String ALPHA_NAME = "alpha";
    public static final String BETA_NAME = "beta";

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
                                .name(UNITS_NAME)
                                .constraints(Constraints.INTEGER, Constraints.POSITIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name(NOBIAS_NAME)
                                .constraints(Constraints.BOOLEAN)
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
                                .name(KERNEL_NAME)
                                .constraints(Constraints.INTEGER_TUPLE, Constraints.POSITIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name(CHANNELS_NAME)
                                .constraints(Constraints.INTEGER, Constraints.POSITIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name(STRIDE_NAME)
                                .constraints(Constraints.INTEGER_TUPLE, Constraints.POSITIVE)
                                .defaultValue(Arrays.asList(1, 1))
                                .build(),
                        new VariableSymbol.Builder()
                                .name(NOBIAS_NAME)
                                .constraints(Constraints.BOOLEAN)
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
                                .name(P_NAME)
                                .constraints(Constraints.NUMBER, Constraints.BETWEEN_ZERO_AND_ONE)
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
                                .name(KERNEL_NAME)
                                .constraints(Constraints.INTEGER_TUPLE, Constraints.POSITIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name(STRIDE_NAME)
                                .constraints(Constraints.INTEGER_TUPLE, Constraints.POSITIVE)
                                .defaultValue(Arrays.asList(1, 1))
                                .build(),
                        new VariableSymbol.Builder()
                                .name(GLOBAL_NAME)
                                .constraints(Constraints.BOOLEAN)
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
                                .name(KERNEL_NAME)
                                .constraints(Constraints.INTEGER_TUPLE, Constraints.POSITIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name(STRIDE_NAME)
                                .constraints(Constraints.INTEGER_TUPLE, Constraints.POSITIVE)
                                .defaultValue(Arrays.asList(1, 1))
                                .build(),
                        new VariableSymbol.Builder()
                                .name(GLOBAL_NAME)
                                .constraints(Constraints.BOOLEAN)
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
                                .name(NSIZE_NAME)
                                .constraints(Constraints.INTEGER, Constraints.NON_NEGATIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name(KNORM_NAME)
                                .constraints(Constraints.NUMBER)
                                .defaultValue(2)
                                .build(),
                        new VariableSymbol.Builder()
                                .name(ALPHA_NAME)
                                .constraints(Constraints.NUMBER)
                                .defaultValue(0.0001)
                                .build(),
                        new VariableSymbol.Builder()
                                .name(BETA_NAME)
                                .constraints(Constraints.NUMBER)
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
                                .name(FIX_GAMMA_NAME)
                                .constraints(Constraints.BOOLEAN)
                                .defaultValue(true)
                                .build(),
                        new VariableSymbol.Builder()
                                .name(AXIS_NAME)
                                .constraints(Constraints.INTEGER, Constraints.NON_NEGATIVE)
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
                                .name(INDEX_NAME)
                                .constraints(Constraints.INTEGER, Constraints.NON_NEGATIVE)
                                .build(),
                        new VariableSymbol.Builder()
                                .name(N_NAME)
                                .constraints(Constraints.INTEGER, Constraints.POSITIVE)
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
                                .name(INDEX_NAME)
                                .constraints(Constraints.INTEGER, Constraints.NON_NEGATIVE)
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
        Optional<Boolean> optGlobal = method.getBooleanValue(GLOBAL_NAME);
        if (optGlobal.isPresent() && optGlobal.get()){
            return Collections.singletonList(new ShapeSymbol.Builder()
                    .height(1)
                    .width(1)
                    .channels(channels)
                    .build());
        }
        else{
            int strideHeight = method.getIntTupleValue(STRIDE_NAME).get().get(0);
            int strideWidth = method.getIntTupleValue(STRIDE_NAME).get().get(1);
            int kernelHeight = method.getIntTupleValue(KERNEL_NAME).get().get(0);
            int kernelWidth = method.getIntTupleValue(KERNEL_NAME).get().get(1);
            int inputHeight = inputShape.getHeight().get();
            int inputWidth = inputShape.getWidth().get();

            //assume padding with border_mode='same'
            int outputWidth = (inputWidth + strideWidth - 1) / strideWidth;
            int outputHeight = (inputHeight + strideWidth - 1) / strideHeight;

            //border_mode=valid
            //int outputWidth = 1 + Math.max(0, ((inputWidth - kernelWidth + strideWidth - 1) / strideWidth));
            //int outputHeight = 1 + Math.max(0, ((inputHeight - kernelHeight + strideHeight - 1) / strideHeight));

            return Collections.singletonList(new ShapeSymbol.Builder()
                    .height(outputHeight)
                    .width(outputWidth)
                    .channels(channels)
                    .build());
        }
    }

    private static List<ShapeSymbol> splitShapeFunction(ShapeSymbol inputShape, MethodLayerSymbol method) {
        int numberOfSplits = method.getIntValue(N_NAME).get();
        int groupIndex = method.getIntValue(INDEX_NAME).get();
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
