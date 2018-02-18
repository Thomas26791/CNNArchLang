[![Maintainability](https://api.codeclimate.com/v1/badges/fc45309cb83a31c9586e/maintainability)](https://codeclimate.com/github/EmbeddedMontiArc/CNNArchLang/maintainability)
[![Build Status](https://travis-ci.org/EmbeddedMontiArc/CNNArchLang.svg?branch=master)](https://travis-ci.org/EmbeddedMontiArc/CNNArchLang)
[![Build Status](https://circleci.com/gh/EmbeddedMontiArc/CNNArchLang/tree/master.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/EmbeddedMontiArc/CNNArchLang/tree/master)
[![Coverage Status](https://coveralls.io/repos/github/EmbeddedMontiArc/CNNArchLang/badge.svg?branch=master)](https://coveralls.io/github/EmbeddedMontiArc/CNNArchLang?branch=master)

# CNNArch
**work in progress**
## Introduction
CNNArch is a declarative language to build architectures of feedforward neural networks with a special focus on convolutional neural networks. 
It is being developed for use in the MontiCar language family, along with CNNTrain which configures the training of the network and EmbeddedMontiArcDL 
which combines the languages into a EmbeddedMontiArc component.
The inputs and outputs of a network are strongly typed and the validity of a network is checked at compile time.
In the following, we will explain the syntax and all features of CNNArch in combination with code examples to show how these can be used.

## Basic Structure
The syntax of this language has many similarities to python in the way how variables and methods are handled. 
Variables which occur only in form of parameters are seemingly untyped. 
However, the correctness of their values is checked at compile time.
The header of the architecture declares architecture parameters which are usually used to define the Dimensions of inputs and outputs.
The top part of the architecture consists of input, output or method declarations.
The main part is the actual definition of the architecture in the form of a collection of layers which are connected through the two operators "->" and "|". 
A layer can either be a method, an input or an output. 
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

## Layer Operators
This language does not use symbols to denote a connections between layers like most deep learning frameworks but instead uses a approach which describes the data flow through the network. 
The first operator is the serial connection "->". The operator simply connects the output of the first layer to the input of the second layer. 
Despite being sequential in nature, CNNArch is still able to describe complex networks like ResNeXt through the use of the parallelization operator "|". 
This operator splits the network into parallel data streams. 
The serial connection operator has a higher precedence than the parallel connection operator. 
Therefore it is necessary to use brackets around each parallel group of layers.
Each element in a parallel group has the same input stream as the whole group. 
The output of a parallel group is a list of streams which can be merged into a single stream through use of the following methods: 
`Convolution()`, `Add()` or `Get(index)`. 
Note: `Get(index=i)` can be abbreviated by `[i]`. 
The method `Split(n)` in the example above creates multiple output streams from a single input stream by splitting the data itself into *n* streams which can then handled separately.


## Inputs and Outputs
An architecture in CNNArch can have multiple inputs and outputs. 
Multiple inputs (or outputs) of the same form can be combined to an array. 
Assuming `h` and `w` are architecture parameter, the following is a valid example:
```
def input Z(0:255)^{3, h, w} image[2]
def input Q(-oo:+oo)^{10} additionalData
def output Q(0:1)^{3} predictions
```
The first line defines the input *image* as an array of two rgb (or bgr) images with a resolution of `h` x `w`. 
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

## Methods
It is possible to declare and construct new methods. The method declaration is similar to python. 
Each parameter can have a default value that makes it an optional argument. 
The method call is also similar to python but, in contrast to python, it is necessary to specify the name of each argument. 
The body of a new method is constructed from other layers including other user-defined methods. However, recursion is not allowed. 
The compiler will throw an error if recursion occurs. 
The following is a example of multiple method declarations.
```
    def conv(filter, channels, stride=1, act=true){
        Convolution(kernel=(filter,filter), channels=channels, stride=(stride,stride)) ->
        BatchNorm() ->
        Relu(?=act)
    }
    def skip(channels, stride){
        Convolution(kernel=(1,1), channels=channels, stride=(stride,stride)) ->
        BatchNorm()
    }
    def resLayer(channels, stride=1){
        (
            conv(filter=3, channels=channels, stride=stride) ->
            conv(filter=3, channels=channels, act=false)
        |
            skip(channels=channels, stride=stride, ?=(stride!=1))
        ) ->
        Add() ->
        Relu()
    }
```
The method `resLayer` in this example corresponds to a building block of a Residual Network. 
The `?` argument is a special argument which is explained in the next section.

## Special Arguments
There exists special structural arguments which can be used in each method. 
These are `->`, `|` and `?`. `->` and `|` can only be positive integers and `?` can only be a boolean. 
The argument `?` does nothing if it is true and removes the layer completely if it is false. 
The other two arguments create an iteration of the method. 
We will show their effect with examples. 
Assuming `a` is a method without required arguments, 
then `a(-> = 3)->` is equal to `a()->a()->a()->`, 
`a(| = 3)->` is equal to `(a() | a() | a())->` and 
`a(-> = 3, | = 2)->` is equal to `(a()->a()->a() | a()->a()->a())->`. 

## Argument Sequences
It is also possible to iterate a method through the use of argument sequences. 
The following are valid sequences: `[2->5->3]`, `[true|false|false]`, `[2->1|4->4->6]`, `[ |2->3]`, `1->..->5` and `3|..|-2`. 
All values in these examples could also be replaced by variable names or expressions. 
The first three are standard sequences and the last two are intervals. 
An interval can be translated to a standard sequence. 
The interval `3|..|-2` is equal to `[3|2|1|0|-1|-2]` and `1->..->5` is equal to `[1->2->3->4->5]`. 

If a argument is set to a sequence, the method will be repeated for each value in the sequence and the connection between the layers will be the same as it is between the values of the sequence. 
An argument which has a single value is neutral to the repetition which means that it will be repeated an arbitrary number of times without interfering with the repetition. 
If a method contains multiple argument sequences, CNNArch will try to combine the sequences. 
The language will throw an error at compile time if this fails. 
Assuming the method `m(a, b, c)` exists, the line `m(a=[5->3], b=[3|4|2], c=2)->` is equal to:
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
And `m(a=[|5|3->4], b=[1|1|2], c=2)` is equal to: 
```
(

|
    m(a=5, b=1, c=2) ->
    m(a=5, b=1, c=2)
|
    m(a=3, b=2, c=2) ->
    m(a=4, b=2, c=2)
) ->
```
However, the line `m(a=[5->3], b=[2|4->6], c=2)->` would throw an error because it is not possible to expand *a* such that it is the same size as *b*.



## Expressions
This language supports the basic arithmetic operators "+", "-", "\*", "/", the logical operators "&&", "||" and the comparison operators "==", "!=", "<", ">", "<=", ">=". 
At the moment, it is sometimes necessary to use parentheses around an expression to avoid a parsing error. 
For example, the line `someMethod(booleanArg = (1!=1))` does not parse without the parentheses around `1!=1`.

## Advanced Example
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
  
  
