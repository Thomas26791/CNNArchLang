<#if tc.targetLanguage == ".py">
        ${tc.currentName} = mx.symbol.Flatten(data=${tc.currentInputs[0]},
            name="${tc.currentName}")
<#elseif tc.targetLanguage == ".cpp">
        auto ${tc.currentName} = Operator("flatten")
            .SetInput("data", ${tc.currentInputs[0]})
            .CreateSymbol("${tc.currentName}");
</#if>