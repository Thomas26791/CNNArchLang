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

import de.monticore.lang.monticar.cnnarch._symboltable.MethodDeclarationSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.MethodLayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ShapeSymbol;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedMethods;
import de.se_rwth.commons.logging.Log;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

abstract public class PredefinedMethodDeclaration extends MethodDeclarationSymbol {

    protected PredefinedMethodDeclaration(String name) {
        super(name);
    }

    @Override
    public boolean isPredefined() {
        return true;
    }

    abstract public List<ShapeSymbol> computeOutputShapes(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer);

    abstract public void checkInput(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer);

    //check if inputShapes is of size 1
    protected void checkOneInput(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer){
        if (inputShapes.size() != 1){
            Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT + " Invalid layer input. " +
                            getName() + " layer can only handle one input stream. " +
                            "Current number of input streams " + inputShapes.size() + "."
                    , layer.getSourcePosition());
        }
    }

    protected void checkNotEmptyInput(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer){
        if (inputShapes.size() == 0){
            Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT + " Invalid layer input. Number of input streams is 0"
                    , layer.getSourcePosition());
        }
    }





    //output shape function for convolution and pooling
    protected static List<ShapeSymbol> convAndPoolOutputShapeFunction(ShapeSymbol inputShape, MethodLayerSymbol method, int channels) {
        Optional<Boolean> optGlobal = method.getBooleanValue(AllPredefinedMethods.GLOBAL_NAME);
        if (optGlobal.isPresent() && optGlobal.get()){
            //argument global is true which means the pooling is applied to the whole input and is flattened. kernel, stride and border_mode is ignored.
            return Collections.singletonList(new ShapeSymbol.Builder()
                    .height(1)
                    .width(1)
                    .channels(channels)
                    .build());
        }
        else{
            int strideHeight = method.getIntTupleValue(AllPredefinedMethods.STRIDE_NAME).get().get(0);
            int strideWidth = method.getIntTupleValue(AllPredefinedMethods.STRIDE_NAME).get().get(1);
            int kernelHeight = method.getIntTupleValue(AllPredefinedMethods.KERNEL_NAME).get().get(0);
            int kernelWidth = method.getIntTupleValue(AllPredefinedMethods.KERNEL_NAME).get().get(1);
            int inputHeight = inputShape.getHeight().get();
            int inputWidth = inputShape.getWidth().get();
            int outputWidth;
            int outputHeight;

            String borderModeSetting = method.getStringValue(AllPredefinedMethods.PADDING_NAME).get();
            if (borderModeSetting.equals(AllPredefinedMethods.PADDING_SAME)){
                //padding with border_mode='same'
                outputWidth = (inputWidth + strideWidth - 1) / strideWidth;
                outputHeight = (inputHeight + strideWidth - 1) / strideHeight;
            }
            else if (borderModeSetting.equals(AllPredefinedMethods.PADDING_VALID)){
                //padding with border_mode=valid, no padding
                if (inputWidth < kernelWidth || inputHeight < kernelHeight){
                    outputWidth = 0;
                    outputHeight = 0;
                }
                else {
                    outputWidth = 1 + (inputWidth - kernelWidth) / strideWidth;
                    outputHeight = 1 + (inputHeight - kernelHeight) / strideHeight;
                }
            }
            else if (borderModeSetting.equals(AllPredefinedMethods.PADDING_NO_LOSS)){
                //padding until no data gets discarded, same as valid with a stride of 1
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

    //check input for convolution and pooling
    protected static void checkConvAndPoolInput(List<ShapeSymbol> inputShapes, MethodLayerSymbol layer){
        if (!inputShapes.isEmpty()) {
            int inputHeight = inputShapes.get(0).getHeight().get();
            int inputWidth = inputShapes.get(0).getWidth().get();
            int kernelHeight;
            int kernelWidth;
            Optional<Boolean> optGlobal = layer.getBooleanValue(AllPredefinedMethods.GLOBAL_NAME);
            if (optGlobal.isPresent() && optGlobal.get()){
                kernelHeight = inputHeight;
                kernelWidth = inputWidth;
            } else{
                kernelHeight = layer.getIntTupleValue(AllPredefinedMethods.KERNEL_NAME).get().get(0);
                kernelWidth = layer.getIntTupleValue(AllPredefinedMethods.KERNEL_NAME).get().get(1);
            }

            if (kernelHeight > inputHeight || kernelWidth > inputWidth){
                if (layer.getStringValue(AllPredefinedMethods.PADDING_NAME).equals(AllPredefinedMethods.PADDING_VALID)){
                    Log.error("0" + ErrorCodes.INVALID_LAYER_INPUT + " Invalid layer input. " +
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
}
