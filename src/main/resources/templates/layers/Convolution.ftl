<#assign input = tc.currentInputs[0]>
<#if tc.targetLanguage == ".py">
<#if tc.padding??>
<#assign input = tc.currentName>
        ${tc.currentName} = mx.symbol.pad(data=${tc.currentInputs[0]},
            mode='constant',
            pad_width=(${tc.join(tc.padding, ",")}),
            constant_value=0)
</#if>
        ${tc.currentName} = mx.symbol.Convolution(data=${input},
            kernel=(${tc.join(tc.kernel, ",")}),
            stride=(${tc.join(tc.stride, ",")})<#if false>,
            dilate=None</#if>,
            num_filter=${tc.channels?c}<#if false>,
            num_group=None</#if><#if false>,
            workspace=None</#if>,
            no_bias=${tc.noBias?string("True","False")}<#if false>,
            cudnn_tune=None</#if><#if false>,
            cudnn_off=None</#if><#if false>,
            layout=None</#if>,
            name="${tc.currentName}")
<#elseif tc.targetLanguage == ".cpp">
<#if tc.padding??>
<#assign input = tc.currentName>
        auto ${tc.currentName} = Operator("pad")
            .SetParam("mode", "constant")
            .SetParam("pad_width", Shape(${tc.join(tc.padding, ",")}))
            .SetParam("constant_value", 0)
            .SetInput("data", ${tc.currentInputs[0]})
            .CreateSymbol();
</#if>
        <#if !tc.padding??>auto </#if>${tc.currentName} = Operator("Convolution")
            .SetParam("kernel", Shape(${tc.join(tc.kernel, ",")}))
            .SetParam("num_filter", ${tc.channels?c})
            .SetParam("stride", Shape(${tc.join(tc.stride, ",")}))<#if false>
            .SetParam("dilate", Shape(1, 1))</#if><#if false>
            .SetParam("num_group", 1)</#if><#if false>
            .SetParam("workspace", 512)</#if>
            .SetParam("no_bias", ${tc.noBias?string("true","false")})
            .SetInput("data", ${input})
            .CreateSymbol("${tc.currentName}");
</#if>
<#include "OutputShape.ftl">