<#if tc.targetLanguage == ".py">
        ${tc.currentName} = mx.symbol.LRN(data=${tc.currentInputs[0]},
            alpha=${tc.alpha?c},
            beta=${tc.beta?c},
            knorm=${tc.knorm?c},
            nsize=${tc.nsize?c},
            name="${tc.currentName}")
<#elseif tc.targetLanguage == ".cpp">
        auto ${tc.currentName} = Operator("LRN")
            .SetParam("nsize", ${tc.nsize?c})
            .SetParam("alpha", ${tc.alpha?c})
            .SetParam("beta", ${tc.beta?c})
            .SetParam("knorm", ${tc.knorm?c})
            .SetInput("data", ${tc.currentInputs[0]})
            .CreateSymbol("${tc.currentName}");
</#if>