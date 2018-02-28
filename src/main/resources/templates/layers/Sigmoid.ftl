<#if tc.targetLanguage == ".py">
        ${tc.currentName} = mx.symbol.Activation(data=${tc.currentInputs[0]},
            act_type='sigmoid',
            name="${tc.currentName}")
<#elseif tc.targetLanguage == ".cpp">
        auto ${tc.currentName} = Operator("Activation")
            .SetParam("act_type", "sigmoid")
            .SetInput("data", ${tc.currentInputs[0]})
            .CreateSymbol("${tc.currentName}");
</#if>