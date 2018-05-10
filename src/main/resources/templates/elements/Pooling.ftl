<#assign input = element.inputs[0]>
<#if element.padding??>
<#assign input = element.name>
        ${element.name} = mx.symbol.pad(data=${element.inputs[0]},
            mode='constant',
            pad_width=(${tc.join(element.padding, ",")}),
            constant_value=0)
</#if>
        ${element.name} = mx.symbol.Pooling(data=${input},
            kernel=(${tc.join(element.kernel, ",")}),
            pool_type=${element.poolType},
            stride=(${tc.join(element.stride, ",")}),
            name="${element.name}")
<#include "OutputShape.ftl">