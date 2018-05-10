[![Maintainability](https://api.codeclimate.com/v1/badges/fc45309cb83a31c9586e/maintainability)](https://codeclimate.com/github/EmbeddedMontiArc/CNNArchLang/maintainability)
[![Build Status](https://travis-ci.org/EmbeddedMontiArc/CNNArchLang.svg?branch=master)](https://travis-ci.org/EmbeddedMontiArc/CNNArchLang)
[![Build Status](https://circleci.com/gh/EmbeddedMontiArc/CNNArchLang/tree/master.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/EmbeddedMontiArc/CNNArchLang/tree/master)
[![Coverage Status](https://coveralls.io/repos/github/EmbeddedMontiArc/CNNArchLang/badge.svg?branch=master)](https://coveralls.io/github/EmbeddedMontiArc/CNNArchLang?branch=master)

# CNNArch
## Introduction
CNNArch is a descriptive language to model architectures of feedforward neural networks with a special focus on convolutional neural networks. 
It is being developed for use in the MontiCar language family, along with CNNTrain, which configures the training of the network, and EmbeddedMontiArcDL, which integrates CNNArch into EmbeddedMontiArc.
The inputs and outputs of a network are strongly typed and the validity of a network is checked at model creation.
In the following, we will explain the syntax and all features of CNNArch in combination with code examples to show how these can be used.

## Basic Structure
The syntax of this language has many similarities to python in the way how variables and methods are handled. 
Variables which occur only in form of parameters are seemingly untyped. 
However, the correctness of their values is checked at compile time.
The header of the architecture declares architecture parameters that can be used in all following expressions. 
In this way, different instances of the same architecture can be created.
The top part of the architecture consists of input, output and layer declarations.
The main part is the actual architecture definition in the form of a collection of architecture elements which are connected through the two operators "->" and "|". 
An architecture element can either be a layer, an input or an output. 
The following is a complete example of the original version of Alexnet by A. Krizhevsky. 
There are more compact versions of the same architecture but we will get to that later. 
All predefined methods are listed at the end of this document.
```
architecture Alexnet_alt(img_height=224, img_width=224, img_channels=3, classes=10){
    def input Z(0:255)^{img_channels, img_height, img_width} image
    def output Q(0:1)^{classes} predictions

    image ->
    Convolution(kernel=(11,11), channels=96, stride=(4,4), padding="no_loss") ->
    Lrn(nsize=5, alpha=0.0001, beta=0.75) ->
    Pooling(pool_type="max", kernel=(3,3), stride=(2,2), padding="no_loss") ->
    Relu() ->
    Split(n=2) ->
    (
        [0] ->
        Convolution(kernel=(5,5), channels=128) ->
        Lrn(nsize=5, alpha=0.0001, beta=0.75) ->
        Pooling(pool_type="max", kernel=(3,3), stride=(2,2), padding="no_loss") ->
        Relu()
    |
        [1] ->
        Convolution(kernel=(5,5), channels=128) ->
        Lrn(nsize=5, alpha=0.0001, beta=0.75) ->
        Pooling(pool_type="max", kernel=(3,3), stride=(2,2), padding="no_loss") ->
        Relu()
    ) ->
    Concatenate() ->
    Convolution(kernel=(3,3), channels=384) ->
    Relu() ->
    Split(n=2) ->
    (
        [0] ->
        Convolution(kernel=(3,3), channels=192) ->
        Relu() ->
        Convolution(kernel=(3,3), channels=128) ->
        Pooling(pool_type="max", kernel=(3,3), stride=(2,2), padding="no_loss") ->
        Relu()
    |
        [1] ->
        Convolution(kernel=(3,3), channels=192) ->
        Relu() ->
        Convolution(kernel=(3,3), channels=128) ->
        Pooling(pool_type="max", kernel=(3,3), stride=(2,2), padding="no_loss") ->
        Relu()
    ) ->
    Concatenate() ->
    FullyConnected(units=4096) ->
    Relu() ->
    Dropout() ->
    FullyConnected(units=4096) ->
    Relu() ->
    Dropout() ->
    FullyConnected(units=classes) ->
    Softmax() ->
    predictions
}
```
*Note: The third convolutional and the first two fully connected layers are not divided into two streams like they are in the original Alexnet. 
This is done for the sake of simplicity. However, this change should not affect the actual computation.*

## Data Flow Operators
This language does not use symbols to denote a connections between layers like most deep learning frameworks but instead uses a approach which describes the data flow through the network. 
The first operator is the serial connection "->". The operator simply connects the output of the first element to the input of the second element. 
Despite being sequential in nature, CNNArch is still able to describe complex networks like ResNeXt through the use of the parallelization operator "|". 
This operator splits the network into parallel data streams. 
The serial connection operator has a higher precedence than the parallel connection operator. 
Therefore, it is necessary to use brackets around parallel groups of layers.
Each element in a parallel group has the same input stream as the whole group. 
The output of a parallelization block is a list of streams which can be merged into a single stream through use of the following layers: 
`Concatenate()`, `Add()` or `Get(index)`. 
Note: `Get(index=i)` can be abbreviated by `[i]`. 
The layer `Split(n)` in the example above creates multiple output streams from a single input stream by splitting the channels of the input data into *n* streams.


## Inputs and Outputs
An architecture in CNNArch can have multiple inputs and outputs. 
Multiple inputs (or outputs) of the same form can be combined to an array. 
Assuming `h` and `w` are architecture parameter, the following is a valid example:
```
def input Z(0:255)^{3, h, w} image[2]
def input Q(-oo:+oo)^{10} additionalData
def output Q(0:1)^{3} predictions
```
The first line defines the input *image* as an array of two color images with a resolution of `h` x `w`. 
The part `Z(0:255)`, which corresponds to the type definition in EmbeddedMontiArc, restricts the values to integers between 0 and 255. 
The following line `{3, h, w}` declares the shape of the input. 
The shape denotes the dimensionality in form  of depth (number of channels), height and width. 
Here, the height is initialized as `h`, the width as `w` and the number of channels is 3.  
The second line defines another input with one dimension of size 10 and arbitrary rational values. 
The last line defines an one-dimensional output of size 3 with rational values between 0 and 1 (probabilities of 3 classes).

If an input or output is an array, it can be used in the architecture in two different ways. 
Either a single element is accessed or the array is used as a whole. 
The line `image[0] ->` would access the first image of the array and `image ->` would directly result in 2 output streams. 
In fact, `image ->` is identical to `(image[0] | image[1]) ->`. 
Furthermore, assuming *out* is a output array of size 2, the line `-> out` would be identical to `-> ([0]->out[0] | [1]->out[1])`. 
Inputs and outputs can also be used in the middle of an architecture. 
In general, inputs create new streams and outputs consume existing streams.

## Layer Construction
It is possible to declare and construct new layers. The declaration of a layer is similar to methods in python. 
Each parameter can have a default value that makes it an optional argument. 
A new layer is constructed from other layers including other constructed layers. However, recursion is not allowed. 
The compiler will throw an error if recursion occurs. 
The following is a example of multiple layer declarations.
```
    def conv(kernel, channels, stride=1, act=true){
        Convolution(kernel=(filter,filter), channels=channels, stride=(stride,stride)) ->
        BatchNorm() ->
        Relu(?=act)
    }
    def resLayer(channels, stride=1, addSkipConv=false){
        (
            conv(kernel=3, channels=channels, stride=stride) ->
            conv(kernel=3, channels=channels, act=false)
        |
            conv(kernel=1, channels=channels, stride=stride, act=false, ?=addSkipConv)
        ) ->
        Add() ->
        Relu()
    }
```
The constructed layer `resLayer` in this example corresponds to a building block of a Residual Network. 
The `?` argument is a special argument which is explained in the next section.

## Structural Arguments
Structural arguments are special arguments which can be set for each layer and which do not correspond to a layer parameter. 
The three structural arguments are "?", "->" and "|". The conditional argument "?" is a boolean. 
It does nothing if it is true and it removes the layer completely if it is false. 
This argument is only useful for layer construction. 
The other two structural arguments are non-negative integers which repeat the layer *x* number of times where *x* is equal to their value. 
The layer operator between each repetition has the same symbol as the argument.

Assuming `a` is a method without required arguments, 
then `a(-> = 3)->` is equal to `a()->a()->a()->`, 
`a(| = 3)->` is equal to `(a() | a() | a())->` and 
`a(-> = 3, | = 2)->` is equal to `(a()->a()->a() | a()->a()->a())->`. 

## Argument Sequences
Argument sequences can be used instead of regular arguments to declare that a layer should be repeated with the values of the given sequence. 
The operator between these so stacked layers is also given by the sequence. 
Other arguments that only have a single value are neutral to the repetition 
which means that the single value will be repeated an arbitrary number of times without having an influence on the number of repetitions.

The following are valid sequences: `[1->2->3->4]`, `[true | false]`, `{[1 | 3->2]`, `[ |2->3]` and `[1->..->4]`. 
All values in these examples could also be replaced by variable names or arithmetic or logical expressions. 
The last sequence is defined as a range and equal to the first one. A range in CNNArch is closed which means the start and end value are both in the sequence. 
Moreover, a range has always a step size of +1. Thus, the range `[0|..|-4]` would be empty. 
The data flow operators can be used both in the same argument sequence in which case a single parallelization block is created. 
A parallel group in this block can be empty, which is why `[ |2->3]` is a valid sequence. 
If a method contains multiple argument sequences, the language will try to combine them by expanding the smaller one and will throw an error at model creation if this fails.
Let `m` be a layer with parameters `a`, `b` and `c`, then the expression `m(a=[3->2],b=1)` is equal to `m(a=3,b=1)->m(a=2,b=1)`. 
Furthermore, the line `m(a=[5->3],b=[3|4|2],c=2)->` is equal to:
```
(
    m(a=5, b=3, c=2) ->
    m(a=3, b=3, c=2)
|
    m(a=5, b=4, c=2) ->
    m(a=3, b=4, c=2)
|
    m(a=5, b=2, c=2) ->
    m(a=3, b=2, c=2)
) ->
```
And `m(a=[|5|3->4], b=[|1|2], c=2)` is equal to: 
```
(

|
    m(a=5, b=1, c=2)
|
    m(a=3, b=2, c=2) ->
    m(a=4, b=2, c=2)
) ->
```
However, `m(a=[5->3], b=[2|4->6], c=2)->` and `m(a=[5->3], b=[2->4->6], c=2)->` would fail because it is not possible to expand *a* such that it is the same size as *b*.



## Expressions
This language supports the basic arithmetic operators "+", "-", "\*", "/", the logical operators "&&", "||", the comparison operators "==", "!=", "<", ">", "<=", ">=" 
and the constants `true` and `false`. 
At the moment, it is sometimes necessary to use parentheses around an expression to avoid a parsing error. 
For example, the line `someMethod(booleanArg = (1!=1))` does not parse without the parentheses around `1!=1`.

## Advanced Examples
This version of Alexnet, which uses method construction, argument sequences and special arguments, is identical to the one in the section Basic Structure.
```
architecture Alexnet_alt2(img_height=224, img_width=224, img_channels=3, classes=10){
    def input Z(0:255)^{img_channels, img_height, img_width} image
    def output Q(0:1)^{classes} predictions
    
    def conv(filter, channels, convStride=1, poolStride=1, hasLrn=false, convPadding="same"){
    	Convolution(kernel=(filter,filter), channels=channels, stride=(convStride,convStride), padding=convPadding) ->
        Lrn(nsize=5, alpha=0.0001, beta=0.75, ?=hasLrn) ->
        Pooling(pool_type="max", kernel=(3,3), stride=(poolStride,poolStride), padding="no_loss", ?=(poolStride != 1)) ->
        Relu()
    }
    def split1(i){
        [i] ->
        conv(filter=5, channels=128, poolStride=2, hasLrn=true)
    }
    def split2(i){
        [i] ->
        conv(filter=3, channels=192) ->
        conv(filter=3, channels=128, poolStride=2)
    }
    def fc(){
        FullyConnected(units=4096) ->
        Relu() ->
        Dropout()
    }

    image ->
    conv(filter=11, channels=96, convStride=4, poolStride=2, hasLrn=true, convPadding="no_loss") ->
    Split(n=2) ->
    split1(i=[0|1]) ->
    Concatenate() ->
    conv(filter=3, channels=384) ->
    Split(n=2) ->
    split2(i=[0|1]) ->
    Concatenate() ->
    fc(-> = 2) ->
    FullyConnected(units=classes) ->
    Softmax() ->
    predictions
}
```

The following architecture is the extremely deep ResNet-152.
```
architecture ResNet152(img_height=224, img_width=224, img_channels=3, classes=1000){
    def input Z(0:255)^{img_channels, img_height, img_width} data
    def output Q(0:1)^{classes} predictions

    def conv(kernel, channels, stride=1, act=true){
        Convolution(kernel=(kernel,kernel), channels=channels, stride=(stride,stride)) ->
        BatchNorm() ->
        Relu(?=act)
    }
    def resLayer(channels, stride=1, addSkipConv=false){
        (
            conv(kernel=1, channels=channels, stride=stride) ->
            conv(kernel=3, channels=channels) ->
            conv(kernel=1, channels=4*channels, act=false)
        |
            conv(kernel=1, channels=4*channels, stride=stride, act=false, ? = addSkipConv)
        ) ->
        Add() ->
        Relu()
    }

    data ->
    conv(kernel=7, channels=64, stride=2) ->
    Pooling(pool_type="max", kernel=(3,3), stride=(2,2)) ->
    resLayer(channels=64, addSkipConv=true) ->
    resLayer(channels=64, ->=2) ->
    resLayer(channels=128, stride=2, addSkipConv=true) ->
    resLayer(channels=128, ->=7) ->
    resLayer(channels=256, stride=2, addSkipConv=true) ->
    resLayer(channels=256, ->=35) ->
    resLayer(channels=512, stride=2, addSkipConv=true) ->
    resLayer(channels=512, ->=2) ->
    GlobalPooling(pool_type="avg") ->
    FullyConnected(units=classes) ->
    Softmax() ->
    predictions
}
```

## Predefined Layers
All methods with the exception of *Concatenate*, *Add*, *Get* and *Split* can only handle 1 input stream and have 1 output stream. 
All predefined methods start with a capital letter and all constructed methods have to start with a lowercase letter.

* **FullyConnected(units, no_bias=false)**

  Creates a fully connected layer and applies flatten to the input if necessary.
    
  * **units** (integer > 0, required): number of neural units in the output.
  * **no_bias** (boolean, optional, default=false): Whether to disable the bias parameter.
  
* **Convolution(kernel, channels, stride=(1,1), padding="same", no_bias=false)**

  Creates a convolutional layer. Currently, only 2D convolutions are allowed
    
  * **kernel** (integer tuple > 0, required): convolution kernel size: (height, width).
  * **channels** (integer > 0, required): number of convolution filters and number of output channels.
  * **stride** (integer tuple > 0, optional, default=(1,1)): convolution stride: (height, width).
  * **padding** ({"valid", "same", "no_loss"}, optional, default="same"): One of "valid", "same" or "no_loss". "valid" means no padding. "same"   results in padding the input such that the output has the same length as the original input divided by the stride (rounded up). "no_loss" results in minimal padding such that each input is used by at least one filter (identical to "valid" if *stride* equals 1).
  * **no_bias** (boolean, optional, default=false): Whether to disable the bias parameter.

* **Softmax()**

  Applies softmax activation function to the input.
    
* **Tanh()**

  Applies tanh activation function to the input.
    
* **Sigmoid()**

  Applies sigmoid activation function to the input.
    
* **Relu()**

  Applies relu activation function to the input.
    
* **Flatten()**

  Reshapes the input such that height and width are 1. 
  Usually not necessary because the FullyConnected layer applies *Flatten* automatically.
    
* **Dropout()**

  Applies dropout operation to input array during training.
    
  * **p** (1 >= float >= 0, optional, default=0.5): Fraction of the input that gets dropped out during training time.
  
* **Pooling(pool_type, kernel, stride=(1,1), padding="same")**

  Performs pooling on the input.
  
  * **pool_type** ({"avg", "max"}, required): Pooling type to be applied.
  * **kernel** (integer tuple > 0, required): convolution kernel size: (height, width).
  * **stride** (integer tuple > 0, optional, default=(1,1)): convolution stride: (height, width).
  * **padding** ({"valid", "same", "no_loss"}, optional, default="same"): One of "valid", "same" or "no_loss". "valid" means no padding. "same"   results in padding the input such that the output has the same length as the original input divided by the stride (rounded up). "no_loss" results in minimal padding such that each input is used by at least one filter (identical to "valid" if *stride* equals 1).

* **GlobalPooling(pool_type)**

  Performs global pooling on the input.
  
  * **pool_type** ({"avg", "max"}, required): Pooling type to be applied.

* **Lrn(nsize, knorm=2, alpha=0.0001, beta=0.75)**

  Applies local response normalization to the input.
  See: [mxnet](https://mxnet.incubator.apache.org/api/python/symbol.html#mxnet.symbol.LRN)
    
  * **nsize** (integer > 0, required): normalization window width in elements.
  * **knorm** (float, optional, default=2): The parameter k in the LRN expression.
  * **alpha** (float, optional, default=0.0001): The variance scaling parameter *alpha* in the LRN expression.
  * **beta** (float, optional, default=0.75): The power parameter *beta* in the LRN expression.

* **BatchNorm(fix_gamma=true)**
    
  Batch normalization.
    
  * **fix_gamma** (boolean, optional, default=true): Fix gamma while training.

* **Concatenate()**
    
  Merges multiple input streams into one output stream by concatenation of channels. 
  The height and width of all inputs must be identical. 
  The number of channels in the output shape is the sum of the number of channels in the shape of the input streams.
    
* **Add()**
    
  Merges multiple input streams into one output stream by adding them element-wise together. 
  The height, width and the number of channels of all inputs must be identical. 
  The output shape is identical to each input shape.
    
* **Get(index)**

  `Get(index=i)` can be abbreviated with `[i]`. Selects one out of multiple input streams. 
  The single output stream is identical to the selected input. 
  
  * **index** (integer >= 0, required): The zero-based index of the selected input.

* **Split(n)**

  Opposite of *Concatenate*. Handles a single input stream and splits it into *n* output streams. 
  The output streams have the same height and width as the input stream and a number channels which is in general `input_channels / n`. 
  The last output stream will have a higher number of channels than the other if `input_channels` is not divisible by `n`.
  
  * **n** (integer > 0, required): The number of output streams. Cannot be higher than the number of input channels.
  
  
