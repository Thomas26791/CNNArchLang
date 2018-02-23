<#if tc.target == ".py">
        ${tc.currentName} = mx.symbol.split(data=${tc.currentInputs[0]},
            num_outputs=${tc.numOutputs?c},
            axis=1,
            name="${tc.currentName}")
<#elseif tc.target == ".cpp">
        Symbol[${tc.numOutputs}] ${tc.currentName} = Operator("split")
            .SetParam("num_outputs", ${tc.numOutputs?c})
            .SetParam("axis", 1)
            .SetInput("data", ${tc.currentInputs[0]})
            .CreateSymbol("${tc.currentName}");
</#if>
<#include "OutputShape.ftl">