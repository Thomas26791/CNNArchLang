<#if tc.targetLanguage == ".py">
        # ${tc.currentName}, output shape: {<#list tc.currentLayer.outputTypes as type>[${tc.join(type.dimensions, ",")}]</#list>}

<#elseif tc.targetLanguage == ".cpp">
        /* ${tc.currentName}, output shape: {<#list tc.currentLayer.outputTypes as type>[${tc.join(type.dimensions, ",")}]</#list>} */

</#if>