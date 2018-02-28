<#if tc.targetLanguage == ".py">
        ${tc.currentName} = mx.symbol.Activation(data=${tc.currentInputs[0]},
            act_type='relu',
            name="${tc.currentName}")
<#elseif tc.targetLanguage == ".cpp">
        auto ${tc.currentName} = Operator("Activation")
            .SetParam("act_type", "relu")
            .SetInput("data", ${tc.currentInputs[0]})
            .CreateSymbol("${tc.currentName}");
</#if>