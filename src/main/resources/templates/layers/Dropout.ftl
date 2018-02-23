<#if tc.target == ".py">
        ${tc.currentName} = mx.symbol.Dropout(data=${tc.currentInputs[0]},
            p=${tc.p?c}<#if false>,
            mode=</#if>,
            name="${tc.currentName}")
<#elseif tc.target == ".cpp">
        auto ${tc.currentName} = Operator("Dropout")
            .SetParam("p", ${tc.p?c})<#if false>
            .SetParam("mode", )</#if>
            .SetInput("data", ${tc.currentInputs[0]})
            .CreateSymbol("${tc.currentName}");
</#if>