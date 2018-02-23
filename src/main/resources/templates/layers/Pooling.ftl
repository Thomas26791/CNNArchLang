<#assign input = tc.currentInputs[0]>
<#if tc.target == ".py">
<#if tc.padding??>
<#assign input = tc.currentName>
        ${tc.currentName} = mx.symbol.pad(data=${tc.currentInputs[0]},
            mode='constant',
            pad_width=(${tc.join(tc.padding, ",")}),
            constant_value=0)
</#if>
        ${tc.currentName} = mx.symbol.Pooling(data=${input}<#if false>,
            cudnn_off=None</#if>,
            kernel=(${tc.join(tc.kernel, ",")}),
            pool_type=${tc.poolType},
            stride=(${tc.join(tc.stride, ",")}),
            name="${tc.currentName}")
<#elseif tc.target == ".cpp">
<#if tc.padding??>
<#assign input = tc.currentName>
        auto ${tc.currentName} = Operator("pad")
            .SetParam("mode", "constant")
            .SetParam("pad_width", Shape(${tc.join(tc.padding, ",")}))
            .SetParam("constant_value", 0)
            .SetInput("data", ${tc.currentInputs[0]})
            .CreateSymbol();
</#if>
        <#if !tc.padding??>auto </#if>${tc.currentName} = Operator("Pooling")
            .SetParam("kernel", Shape(${tc.join(tc.kernel, ",")}))
            .SetParam("pool_type", ${tc.poolType})
            .SetParam("stride", Shape(${tc.join(tc.stride, ",")}))
            .SetInput("data", ${input})
            .CreateSymbol("${tc.currentName}");
</#if>
<#include "OutputShape.ftl">