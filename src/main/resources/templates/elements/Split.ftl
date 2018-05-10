        ${element.name} = mx.symbol.split(data=${element.inputs[0]},
            num_outputs=${element.numOutputs?c},
            axis=1,
            name="${element.name}")
<#include "OutputShape.ftl">