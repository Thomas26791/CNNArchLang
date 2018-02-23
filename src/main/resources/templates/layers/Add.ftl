<#if tc.target == ".py">
        ${tc.currentName} = ${tc.join(tc.currentInputs, " + ")}
<#elseif tc.target == ".cpp">
        auto ${tc.currentName} = ${tc.join(tc.currentInputs, " + ")};
</#if>
<#include "OutputShape.ftl">