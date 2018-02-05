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

import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedMethods;
import de.monticore.lang.monticar.ranges._ast.ASTRange;
import de.se_rwth.commons.logging.Log;
import org.jscience.mathematics.number.Rational;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

abstract public class PredefinedMethodDeclaration extends MethodDeclarationSymbol {

    protected PredefinedMethodDeclaration(String name) {
        super(name);
    }

    @Override
    protected void setParameters(List<VariableSymbol> parameters) {
        super.setParameters(parameters);
        for (VariableSymbol param : parameters){
            param.putInScope(getSpannedScope());
        }
    }

    @Override
    public boolean isPredefined() {
        return true;
    }

    abstract public List<ArchTypeSymbol> computeOutputTypes(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer);

    abstract public void checkInput(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer);


    //the following methods are only here to avoid duplication. They are used by multiple subclasses.

    //check if inputTypes is of size 1
    protected void errorIfInputSizeIsNotOne(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer){
        if (inputTypes.size() != 1){
            Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT_SHAPE + " Invalid layer input. " +
                            getName() + " layer can only handle one input stream. " +
                            "Current number of input streams " + inputTypes.size() + "."
                    , layer.getSourcePosition());
        }
    }

    protected void errorIfInputIsEmpty(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer){
        if (inputTypes.size() == 0){
            Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT_SHAPE + " Invalid layer input. Number of input streams is 0"
                    , layer.getSourcePosition());
        }
    }

    //check input for convolution and pooling
    protected static void errorIfInputSmallerThanKernel(List<ArchTypeSymbol> inputTypes, MethodLayerSymbol layer){
        if (!inputTypes.isEmpty()) {
            int inputHeight = inputTypes.get(0).getHeight().get();
            int inputWidth = inputTypes.get(0).getWidth().get();
            int kernelHeight = layer.getIntTupleValue(AllPredefinedMethods.KERNEL_NAME).get().get(0);
            int kernelWidth = layer.getIntTupleValue(AllPredefinedMethods.KERNEL_NAME).get().get(1);

            if (kernelHeight > inputHeight || kernelWidth > inputWidth){
                if (layer.getStringValue(AllPredefinedMethods.PADDING_NAME).equals(AllPredefinedMethods.PADDING_VALID)){
                    Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT_SHAPE + " Invalid layer input. " +
                                    "The input resolution is smaller than the kernel and the padding mode is 'valid'." +
                                    "This would result in an output resolution of 0x0."
                            , layer.getSourcePosition());
                }
                else {
                    Log.warn("The input resolution is smaller than the kernel. " +
                                    "This results in an output resolution of 1x1. " +
                                    "If this warning appears multiple times, consider changing your architecture"
                            , layer.getSourcePosition());
                }
            }
        }
    }

    //output type function for convolution and pooling
    protected static List<ArchTypeSymbol> computeConvAndPoolOutputShape(ArchTypeSymbol inputType, MethodLayerSymbol method, int channels) {
        String borderModeSetting = method.getStringValue(AllPredefinedMethods.PADDING_NAME).get();
        if (borderModeSetting.equals(AllPredefinedMethods.PADDING_SAME)){
            return computeOutputShapeWithSamePadding(inputType, method, channels);
        }
        else if (borderModeSetting.equals(AllPredefinedMethods.PADDING_VALID)){
            return computeOutputShapeWithValidPadding(inputType, method, channels);
        }
        else if (borderModeSetting.equals(AllPredefinedMethods.PADDING_NO_LOSS)){
            return computeOutputShapeWithNoLossPadding(inputType, method, channels);
        }
        else{
            throw new IllegalStateException("border_mode is " + borderModeSetting + ". This should never happen.");
        }
    }

    //padding with border_mode=valid, no padding
    private static List<ArchTypeSymbol> computeOutputShapeWithValidPadding(ArchTypeSymbol inputType, MethodLayerSymbol method, int channels){
        int strideHeight = method.getIntTupleValue(AllPredefinedMethods.STRIDE_NAME).get().get(0);
        int strideWidth = method.getIntTupleValue(AllPredefinedMethods.STRIDE_NAME).get().get(1);
        int kernelHeight = method.getIntTupleValue(AllPredefinedMethods.KERNEL_NAME).get().get(0);
        int kernelWidth = method.getIntTupleValue(AllPredefinedMethods.KERNEL_NAME).get().get(1);
        int inputHeight = inputType.getHeight().get();
        int inputWidth = inputType.getWidth().get();

        int outputWidth;
        int outputHeight;
        if (inputWidth < kernelWidth || inputHeight < kernelHeight){
            outputWidth = 0;
            outputHeight = 0;
        }
        else {
            outputWidth = 1 + (inputWidth - kernelWidth) / strideWidth;
            outputHeight = 1 + (inputHeight - kernelHeight) / strideHeight;
        }

        return Collections.singletonList(new ArchTypeSymbol.Builder()
                .height(outputHeight)
                .width(outputWidth)
                .channels(channels)
                .elementType("-oo", "oo")
                .build());
    }

    //padding until no data gets discarded, same as valid with a stride of 1
    private static List<ArchTypeSymbol> computeOutputShapeWithNoLossPadding(ArchTypeSymbol inputType, MethodLayerSymbol method, int channels){
        int strideHeight = method.getIntTupleValue(AllPredefinedMethods.STRIDE_NAME).get().get(0);
        int strideWidth = method.getIntTupleValue(AllPredefinedMethods.STRIDE_NAME).get().get(1);
        int kernelHeight = method.getIntTupleValue(AllPredefinedMethods.KERNEL_NAME).get().get(0);
        int kernelWidth = method.getIntTupleValue(AllPredefinedMethods.KERNEL_NAME).get().get(1);
        int inputHeight = inputType.getHeight().get();
        int inputWidth = inputType.getWidth().get();

        int outputWidth = 1 + Math.max(0, ((inputWidth - kernelWidth + strideWidth - 1) / strideWidth));
        int outputHeight = 1 + Math.max(0, ((inputHeight - kernelHeight + strideHeight - 1) / strideHeight));

        return Collections.singletonList(new ArchTypeSymbol.Builder()
                .height(outputHeight)
                .width(outputWidth)
                .channels(channels)
                .elementType("-oo", "oo")
                .build());
    }

    //padding with border_mode='same'
    private static List<ArchTypeSymbol> computeOutputShapeWithSamePadding(ArchTypeSymbol inputType, MethodLayerSymbol method, int channels){
        int strideHeight = method.getIntTupleValue(AllPredefinedMethods.STRIDE_NAME).get().get(0);
        int strideWidth = method.getIntTupleValue(AllPredefinedMethods.STRIDE_NAME).get().get(1);
        int inputHeight = inputType.getHeight().get();
        int inputWidth = inputType.getWidth().get();

        int outputWidth = (inputWidth + strideWidth - 1) / strideWidth;
        int outputHeight = (inputHeight + strideWidth - 1) / strideHeight;

        return Collections.singletonList(new ArchTypeSymbol.Builder()
                .height(outputHeight)
                .width(outputWidth)
                .channels(channels)
                .elementType("-oo", "oo")
                .build());
    }

    protected List<String> computeStartAndEndValue(List<ArchTypeSymbol> inputTypes, BinaryOperator<Rational> startValAccumulator, BinaryOperator<Rational> endValAccumulator){
        Stream.Builder<Rational> startValues = Stream.builder();
        Stream.Builder<Rational> endValues = Stream.builder();
        String start = null;
        String end = null;
        for (ArchTypeSymbol inputType : inputTypes){
            ASTRange range = inputType.getElementType().getRange().get();
            if (range.getStartInf().isPresent()){
                start = "-oo";
            }
            else {
                startValues.add(range.getStartValue());
            }
            if (range.getEndInf().isPresent()){
                end = "oo";
            }
            else {
                endValues.add(range.getEndValue());
            }
        }
        if (start == null){
            start = "" + startValues.build().reduce(startValAccumulator).get().doubleValue();
        }
        if (end == null){
            end = "" + endValues.build().reduce(endValAccumulator).get().doubleValue();
        }

        return Arrays.asList(start, end);
    }
}
