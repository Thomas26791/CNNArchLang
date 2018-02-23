<#if tc.target == ".py">
        ${tc.currentName} = mx.symbol.concat(${tc.join(tc.currentInputs, ", ")},
            dim=1,
            name="${tc.currentName}")
<#elseif tc.target == ".cpp">
        auto ${tc.currentName} = Operator("concat")
            .SetParam("dim", 1)
            .SetInput("data", {${tc.join(tc.currentInputs, ",")}})
            .CreateSymbol("${tc.currentName}");
</#if>
<#include "OutputShape.ftl">