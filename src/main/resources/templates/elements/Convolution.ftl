<#assign input = element.inputs[0]>
<#if element.padding??>
<#assign input = element.name>
        ${element.name} = mx.symbol.pad(data=${element.inputs[0]},
            mode='constant',
            pad_width=(${tc.join(element.padding, ",")}),
            constant_value=0)
</#if>
        ${element.name} = mx.symbol.Convolution(data=${input},
            kernel=(${tc.join(element.kernel, ",")}),
            stride=(${tc.join(element.stride, ",")}),
            num_filter=${element.channels?c},
            no_bias=${element.noBias?string("True","False")},
            name="${element.name}")
<#include "OutputShape.ftl">