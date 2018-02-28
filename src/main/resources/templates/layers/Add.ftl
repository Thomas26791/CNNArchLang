<#if tc.targetLanguage == ".py">
        ${tc.currentName} = ${tc.join(tc.currentInputs, " + ")}
<#elseif tc.targetLanguage == ".cpp">
        auto ${tc.currentName} = ${tc.join(tc.currentInputs, " + ")};
</#if>
<#include "OutputShape.ftl">