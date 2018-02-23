<#if tc.target == ".py">
        ${tc.currentName} = ${tc.currentInputs[tc.index]}
<#elseif tc.target == ".cpp">
        auto ${tc.currentName} = ${tc.currentInputs[tc.index]};
</#if>