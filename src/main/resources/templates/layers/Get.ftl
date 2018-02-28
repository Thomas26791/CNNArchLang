<#if tc.targetLanguage == ".py">
        ${tc.currentName} = ${tc.currentInputs[tc.index]}
<#elseif tc.targetLanguage == ".cpp">
        auto ${tc.currentName} = ${tc.currentInputs[tc.index]};
</#if>