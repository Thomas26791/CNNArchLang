<#assign flatten = tc.currentLayer.inputTypes[0].height != 1 || tc.currentLayer.inputTypes[0].width != 1>
<#assign input = tc.currentInputs[0]>
<#if tc.target == ".py">
<#if flatten>
        ${tc.currentName} = mx.symbol.flatten(data=${input})
<#assign input = tc.currentName>
</#if>
        ${tc.currentName} = mx.symbol.FullyConnected(data=${input},
            num_hidden=${tc.units?c},
            no_bias=${tc.noBias?string("True","False")},
            name="${tc.currentName}")
<#elseif tc.target == ".cpp">
<#if flatten>
        auto ${tc.currentName} = Operator("flatten")
            .SetInput("data", ${input})
            .CreateSymbol();
<#assign input = tc.currentName>
</#if>
        <#if !flatten>auto </#if>${tc.currentName} = Operator("FullyConnected")
            .SetParam("num_hidden", ${tc.units?c})
            .SetParam("no_bias", ${tc.noBias?string("true","false")})
            .SetInput("data", ${input})
            .CreateSymbol("${tc.currentName}");
</#if>