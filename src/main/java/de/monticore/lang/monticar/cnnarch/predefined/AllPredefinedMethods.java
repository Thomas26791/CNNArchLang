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
package de.monticore.lang.monticar.cnnarch.predefined;

import de.monticore.lang.monticar.cnnarch._symboltable.MethodDeclarationSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.MethodLayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ShapeSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.VariableSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.Constraints;

import java.util.*;

public class AllPredefinedMethods {

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
    public static final String FIX_GAMMA_NAME = "fix_gamma";
    public static final String NSIZE_NAME = "nsize";
    public static final String KNORM_NAME = "knorm";
    public static final String ALPHA_NAME = "alpha";
    public static final String BETA_NAME = "beta";
    public static final String PADDING_NAME = "padding";

    //possible String values
    public static final String PADDING_VALID = "\"valid\"";
    public static final String PADDING_SAME = "\"same\"";
    public static final String PADDING_NO_LOSS = "\"no_loss\"";


    //list with all atomic methods
    public static List<MethodDeclarationSymbol> createList(){
        return Arrays.asList(
                FullyConnected.create(),
                Convolution.create(),
                Softmax.create(),
                Sigmoid.create(),
                Tanh.create(),
                Relu.create(),
                Dropout.create(),
                Flatten.create(),
                MaxPooling.create(),
                AveragePooling.create(),
                Lrn.create(),
                BatchNorm.create(),
                Split.create(),
                Get.create(),
                Add.create(),
                Concatenate.create());
    }

}
