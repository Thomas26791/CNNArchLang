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
import de.monticore.lang.monticar.cnnarch._symboltable.VariableSymbol;
import org.jscience.mathematics.number.Rational;

import java.util.Arrays;
import java.util.List;

public class PredefinedMethods {

    public static MethodDeclarationSymbol createFullyConnected(){
        return  new MethodDeclarationSymbol.Builder()
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
                .predefined(true)
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
                .predefined(true)
                .build();
    }

    public static MethodDeclarationSymbol createSoftmax(){
        return new MethodDeclarationSymbol.Builder()
                .name("Softmax")
                .predefined(true)
                .build();
    }

    public static MethodDeclarationSymbol createSigmoid(){
        return new MethodDeclarationSymbol.Builder()
                .name("Sigmoid")
                .predefined(true)
                .build();
    }

    public static MethodDeclarationSymbol createTanh(){
        return new MethodDeclarationSymbol.Builder()
                .name("Tanh")
                .predefined(true)
                .build();
    }

    public static MethodDeclarationSymbol createRelu(){
        return new MethodDeclarationSymbol.Builder()
                .name("Relu")
                .predefined(true)
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
                .predefined(true)
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
                .predefined(true)
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
                .predefined(true)
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
                .predefined(true)
                .build();
    }

    public static MethodDeclarationSymbol createBatchNorm(){
        return new MethodDeclarationSymbol.Builder()
                .name("BatchNorm")
                .parameters(
                        //todo
                )
                .predefined(true)
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
                .predefined(true)
                .build();
    }

    public static MethodDeclarationSymbol createGet(){
        return new MethodDeclarationSymbol.Builder()
                .name("Get")
                .parameters(
                        new VariableSymbol.Builder()
                                .name("index")
                                .build()
                )
                .predefined(true)
                .build();
    }

    public static MethodDeclarationSymbol createAdd(){
        return new MethodDeclarationSymbol.Builder()
                .name("Add")
                .parameters(

                )
                .predefined(true)
                .build();
    }

    public static MethodDeclarationSymbol createConcatenate(){
        return new MethodDeclarationSymbol.Builder()
                .name("Concatenate")
                .parameters(

                )
                .predefined(true)
                .build();
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
