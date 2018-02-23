<#-- This template is not used if the followiing layer is an output. See Output.ftl -->
<#if tc.target == ".py">
        ${tc.currentName} = mx.symbol.softmax(data=${tc.currentInputs[0]},
            axis=1,
            name="${tc.currentName}")
<#elseif tc.target == ".cpp">
        auto ${tc.currentName} = Operator("softmax")
            .SetParam("axis", 1)
            .SetInput("data", ${tc.currentInputs[0]})
            .CreateSymbol("${tc.currentName}");
</#if>