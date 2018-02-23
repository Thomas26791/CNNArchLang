<#if tc.target == ".py">
        ${tc.currentName} = mx.symbol.BatchNorm(data=${tc.currentInputs[0]},
            fix_gamma=${tc.fixGamma?string("True","False")},
            name="${tc.currentName}")
<#elseif tc.target == ".cpp">
        auto ${tc.currentName} = Operator("BatchNorm")
            .SetParam("fix_gamma", ${tc.fixGamma?string("true","false")})
            .SetInput("data", ${tc.currentInputs[0]})
            .CreateSymbol("${tc.currentName}");
</#if>