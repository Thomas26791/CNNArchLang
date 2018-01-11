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

import java.util.*;

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
    public static final String SPLIT_NAME = "Split";
    public static final String GET_NAME = "Get";
    public static final String ADD_NAME = "Add";
    public static final String CONCATENATE_NAME = "Concatenate";
    public static final String FLATTEN_NAME = "Flatten";

    //predefined argument names
    public static final String KERNEL_NAME = "kernel";
    public static final String CHANNELS_NAME = "channels";
    public static final String STRIDE_NAME = "stride";
    public static final String UNITS_NAME = "units";
    public static final String NOBIAS_NAME = "no_bias";
    public static final String GLOBAL_NAME = "global";
    public static final String P_NAME = "p";
    public static final String INDEX_NAME = "index";
    public static final String NUM_SPLITS_NAME = "n";
    public static final String AXIS_NAME = "axis";
    public static final String FIX_GAMMA_NAME = "fix_gamma";
    public static final String NSIZE_NAME = "nsize";
    public static final String KNORM_NAME = "knorm";
    public static final String ALPHA_NAME = "alpha";
    public static final String BETA_NAME = "beta";
    public static final String PADDING_NAME = "padding";
    public static final String PADDING_VALID = "\"valid\"";
    public static final String PADDING_SAME = "\"same\"";


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
                createConcatenate(),
                createFlatten());
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
                        .channels(method.getIntValue(UNITS_NAME).get())
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
                                .build(),
                        new VariableSymbol.Builder()
                                .name(PADDING_NAME)
                                .constraints(Constraints.PADDING_TYPE)
                                .defaultValue(PADDING_SAME)
                                .build()
                )
                .shapeFunction((inputShapes, method) ->
                        strideShapeFunction(inputShapes.get(0),
                                method,
                                method.getIntValue(CHANNELS_NAME).get()))
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

    public static MethodDeclarationSymbol createFlatten(){
        return new MethodDeclarationSymbol.Builder()
                .name(FLATTEN_NAME)
                .shapeFunction((inputShapes, method) -> Collections.singletonList(new ShapeSymbol.Builder()
                        .height(1)
                        .width(1)
                        .channels(inputShapes.get(0).getHeight().get()
                                * inputShapes.get(0).getWidth().get()
                                * inputShapes.get(0).getChannels().get())
                        .build()))
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
                                .build(),
                        new VariableSymbol.Builder()
                                .name(PADDING_NAME)
                                .constraints(Constraints.PADDING_TYPE)
                                .defaultValue(PADDING_SAME)
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
                                .build(),
                        new VariableSymbol.Builder()
                                .name(PADDING_NAME)
                                .constraints(Constraints.PADDING_TYPE)
                                .defaultValue(PADDING_SAME)
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
                                .name(NUM_SPLITS_NAME)
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
                        Collections.singletonList(inputShapes.get(method.getIntValue(INDEX_NAME).get())))
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
            //argument global is true which means the pooling is applied to the whole input and is flattened. kernel, stride and border_mode is ignored.
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
            int outputWidth;
            int outputHeight;

            String borderModeSetting = method.getStringValue(PADDING_NAME).get();
            if (borderModeSetting.equals(PADDING_SAME)){
                //padding with border_mode='same'
                outputWidth = (inputWidth + strideWidth - 1) / strideWidth;
                outputHeight = (inputHeight + strideWidth - 1) / strideHeight;
            }
            else if (borderModeSetting.equals(PADDING_VALID)){
                //padding with border_mode=valid
                outputWidth = 1 + Math.max(0, ((inputWidth - kernelWidth + strideWidth - 1) / strideWidth));
                outputHeight = 1 + Math.max(0, ((inputHeight - kernelHeight + strideHeight - 1) / strideHeight));
            }
            else{
                throw new IllegalStateException("border_mode is " + borderModeSetting + ". This should never happen.");
            }

            return Collections.singletonList(new ShapeSymbol.Builder()
                    .height(outputHeight)
                    .width(outputWidth)
                    .channels(channels)
                    .build());
        }
    }

    private static List<ShapeSymbol> splitShapeFunction(ShapeSymbol inputShape, MethodLayerSymbol method) {
        int numberOfSplits = method.getIntValue(NUM_SPLITS_NAME).get();
        int inputHeight = inputShape.getHeight().get();
        int inputWidth = inputShape.getWidth().get();
        int inputChannels = inputShape.getChannels().get();

        int outputChannels = inputChannels / numberOfSplits;
        int outputChannelsLast = inputChannels - (numberOfSplits-1) * outputChannels;

        List<ShapeSymbol> outputShapes  = new ArrayList<>(numberOfSplits);
        for (int i = 0; i < numberOfSplits; i++){
            if (i == numberOfSplits - 1) {
                outputShapes.add(new ShapeSymbol.Builder()
                        .height(inputHeight)
                        .width(inputWidth)
                        .channels(outputChannelsLast)
                        .build());
            } else {
                outputShapes.add(new ShapeSymbol.Builder()
                        .height(inputHeight)
                        .width(inputWidth)
                        .channels(outputChannels)
                        .build());
            }
        }
        return outputShapes;
    }

    private static List<ShapeSymbol> concatenateShapeFunction(List<ShapeSymbol> inputShapes, MethodLayerSymbol method) {
        int height = inputShapes.get(0).getHeight().get();
        int width = inputShapes.get(0).getWidth().get();
        int channels = 0;
        for (ShapeSymbol inputShape : inputShapes) {
            channels += inputShape.getChannels().get();
            if (height != inputShape.getHeight().get() || width != inputShape.getWidth().get()){
                throw new IllegalStateException("Concatenation of inputs with different resolutions is not possible. " +
                        "This exception should never occur (CoCo implementation error).");
            }
        }
        return Collections.singletonList(new ShapeSymbol.Builder()
                .height(height)
                .width(width)
                .channels(channels)
                .build());
    }
}
