<#assign channelIndex = tc.currentLayer.outputTypes[0].channelIndex + 1>
<#assign heightIndex = tc.currentLayer.outputTypes[0].heightIndex + 1>
<#assign widthIndex = tc.currentLayer.outputTypes[0].widthIndex + 1>
<#assign indexList = []>
<#if channelIndex != 0><#assign indexList = indexList + [channelIndex]></#if>
<#if heightIndex != 0><#assign indexList = indexList + [heightIndex]></#if>
<#if widthIndex != 0><#assign indexList = indexList + [widthIndex]></#if>
<#assign dimensions = tc.currentLayer.outputTypes[0].dimensions>
<#if tc.targetLanguage == ".py">
        self.${tc.currentName} = mx.sym.var("${tc.currentName}",
            shape=(0,${tc.join(dimensions, ",")}))
        ${tc.currentName} = self.${tc.currentName}
<#include "OutputShape.ftl">
<#if heightIndex != channelIndex + 1 || widthIndex != heightIndex + 1>
        ${tc.currentName} = mx.symbol.transpose(data=${tc.currentName},
            axes=(0,${tc.join(indexList, ",")}))

</#if>
<#if indexList?size != 3>
        ${tc.currentName} = mx.symbol.reshape(data=${tc.currentName},
            shape=(0,${tc.currentLayer.outputTypes[0].channels?c},${tc.currentLayer.outputTypes[0].height?c},${tc.currentLayer.outputTypes[0].width?c}))
</#if>
<#elseif tc.targetLanguage == ".cpp">
        m_${tc.currentName} = Symbol::Variable("${tc.currentName}");
        m_${tc.currentName}.SetParam("shape", Shape(0,${tc.join(dimensions, ",")}));
        auto ${tc.currentName} = m_${tc.currentName};
<#include "OutputShape.ftl">
<#if heightIndex != channelIndex + 1 || widthIndex != heightIndex + 1>
        ${tc.currentName} = Operator("transpose")
            .SetParam("axes", Shape(0,${tc.join(indexList, ",")}))
            .SetInput("data", ${tc.currentName})
            .CreateSymbol();

</#if>
<#if indexList?size != 3>
        ${tc.currentName} = Operator("reshape")
            .SetParam("shape", Shape(0,${tc.currentLayer.outputTypes[0].channels?c},${tc.currentLayer.outputTypes[0].height?c},${tc.currentLayer.outputTypes[0].width?c}))
            .SetInput("data", ${tc.currentName})
            .CreateSymbol();
</#if>
</#if>