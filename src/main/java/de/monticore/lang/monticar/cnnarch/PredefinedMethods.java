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

    public static final MethodDeclarationSymbol FULLY_CONNECTED = new MethodDeclarationSymbol.Builder()
            .name("FullyConnected")
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

    public static final MethodDeclarationSymbol CONVOLUTION = new MethodDeclarationSymbol.Builder()
            .name("Convolution")
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
                            .defaultValue(1, 1)
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

    public static final MethodDeclarationSymbol SOFTMAX = new MethodDeclarationSymbol.Builder()
            .name("Softmax")
            .shapeFunction((inputShapes, method) -> inputShapes)
            .build();

    public static final MethodDeclarationSymbol SIGMOID = new MethodDeclarationSymbol.Builder()
            .name("Sigmoid")
            .shapeFunction((inputShapes, method) -> inputShapes)
            .build();

    public static final MethodDeclarationSymbol TANH = new MethodDeclarationSymbol.Builder()
            .name("Tanh")
            .shapeFunction((inputShapes, method) -> inputShapes)
            .build();

    public static final MethodDeclarationSymbol RELU = new MethodDeclarationSymbol.Builder()
            .name("Relu")
            .shapeFunction((inputShapes, method) -> inputShapes)
            .build();

    public static final MethodDeclarationSymbol DROPOUT = new MethodDeclarationSymbol.Builder()
            .name("Dropout")
            .parameters(
                    new VariableSymbol.Builder()
                            .name("p")
                            .constraints(Constraint.NUMBER, Constraint.BETWEEN_ZERO_AND_ONE)
                            .defaultValue(Rational.valueOf(1, 2))//0.5
                            .build()
            )
            .shapeFunction((inputShapes, method) -> inputShapes)
            .build();

    public static final MethodDeclarationSymbol MAX_POOLING = new MethodDeclarationSymbol.Builder()
            .name("MaxPooling")
            .parameters(
                    new VariableSymbol.Builder()
                            .name("kernel")
                            .constraints(Constraint.INTEGER_TUPLE, Constraint.POSITIVE)
                            .build(),
                    new VariableSymbol.Builder()
                            .name("stride")
                            .constraints(Constraint.INTEGER_TUPLE, Constraint.POSITIVE)
                            .defaultValue(1, 1)
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

    public static final MethodDeclarationSymbol AVERAGE_POOLING = new MethodDeclarationSymbol.Builder()
            .name("AveragePooling")
            .parameters(
                    new VariableSymbol.Builder()
                            .name("kernel")
                            .constraints(Constraint.INTEGER_TUPLE, Constraint.POSITIVE)
                            .build(),
                    new VariableSymbol.Builder()
                            .name("stride")
                            .constraints(Constraint.INTEGER_TUPLE, Constraint.POSITIVE)
                            .defaultValue(1, 1)
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

    public static final MethodDeclarationSymbol LRN = new MethodDeclarationSymbol.Builder()
            .name("Lrn")
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
                            .defaultValue(Rational.valueOf(1, 10000))//0.0001
                            .build(),
                    new VariableSymbol.Builder()
                            .name("beta")
                            .constraints(Constraint.NUMBER)
                            .defaultValue(Rational.valueOf(3, 4))//0.75
                            .build()
            )
            .shapeFunction((inputShapes, method) -> inputShapes)
            .build();

    public static final MethodDeclarationSymbol BATCHNORM = new MethodDeclarationSymbol.Builder()
            .name("BatchNorm")
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

    public static final MethodDeclarationSymbol SPLIT = new MethodDeclarationSymbol.Builder()
            .name("Split")
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

    public static final MethodDeclarationSymbol GET = new MethodDeclarationSymbol.Builder()
            .name("Get")
            .parameters(
                    new VariableSymbol.Builder()
                            .name("index")
                            .constraints(Constraint.INTEGER, Constraint.NON_NEGATIVE)
                            .build()
            )
            .shapeFunction((inputShapes, method) ->
                    Collections.singletonList(inputShapes.get(method.getIntValue("index").get())))
            .build();

    public static final MethodDeclarationSymbol ADD = new MethodDeclarationSymbol.Builder()
            .name("Add")
            .shapeFunction((inputShapes, method) -> Collections.singletonList(inputShapes.get(0)))
            .build();

    public static final MethodDeclarationSymbol CONCATENATE = new MethodDeclarationSymbol.Builder()
            .name("Concatenate")
            .shapeFunction(PredefinedMethods::concatenateShapeFunction)
            .build();

    public static final List<MethodDeclarationSymbol> LIST = Arrays.asList(
            FULLY_CONNECTED,
            CONVOLUTION,
            SOFTMAX,
            SIGMOID,
            TANH,
            RELU,
            DROPOUT,
            MAX_POOLING,
            AVERAGE_POOLING,
            LRN,
            BATCHNORM,
            SPLIT,
            GET,
            ADD,
            CONCATENATE);

    public static final Map<String, MethodDeclarationSymbol> MAP = createPredefinedMap();




    private static Map<String, MethodDeclarationSymbol> createPredefinedMap() {
        Map<String, MethodDeclarationSymbol> map = new HashMap<>();
        for (MethodDeclarationSymbol method : LIST) {
            map.put(method.getName(), method);
        }
        return map;
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
