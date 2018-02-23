<#if tc.target == ".py">
        # ${tc.currentName}, output shape: ${tc.currentOutputShape}

<#elseif tc.target == ".cpp">
        /* ${tc.currentName}, output shape: ${tc.currentOutputShape} */

</#if>