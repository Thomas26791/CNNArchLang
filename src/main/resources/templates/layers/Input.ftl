<#assign channelIndex = tc.currentLayer.outputTypes[0].channelIndex + 1>
<#assign heightIndex = tc.currentLayer.outputTypes[0].heightIndex + 1>
<#assign widthIndex = tc.currentLayer.outputTypes[0].widthIndex + 1>
<#assign indexList = []>
<#if channelIndex != 0><#assign indexList = indexList + [channelIndex]></#if>
<#if heightIndex != 0><#assign indexList = indexList + [heightIndex]></#if>
<#if widthIndex != 0><#assign indexList = indexList + [widthIndex]></#if>
<#assign dimensions = tc.currentLayer.outputTypes[0].dimensions>
<#if tc.targetLanguage == ".py">
        ${tc.currentName} = mx.sym.var("${tc.currentName}",
            shape=(0,${tc.join(dimensions, ",")}))
<#include "OutputShape.ftl">
<#if heightIndex != channelIndex + 1 || widthIndex != heightIndex + 1>
        ${tc.currentName} = mx.symbol.transpose(data=${tc.currentName},
            axes=(0,${tc.join(indexList, ",")}))

</#if>
<#if indexList?size != 3>
        ${tc.currentName} = mx.symbol.reshape(data=${tc.currentName},
            shape=(0,${tc.currentLayer.outputTypes[0].channels?c},${tc.currentLayer.outputTypes[0].height?c},${tc.currentLayer.outputTypes[0].width?c}))
</#if>
        if not data_mean is None:
            assert(not data_std is None)
            _data_mean_ = mx.sym.Variable("_data_mean_", shape=(${tc.join(dimensions, ",")}), init=MyConstant(value=data_mean.tolist()))
            _data_mean_ = mx.sym.BlockGrad(_data_mean_)
            _data_std_ = mx.sym.Variable("_data_std_", shape=(${tc.join(dimensions, ",")}), init=MyConstant(value=data_mean.tolist()))
            _data_std_ = mx.sym.BlockGrad(_data_std_)
            ${tc.currentName} = mx.symbol.broadcast_sub(${tc.currentName}, _data_mean_)
            ${tc.currentName} = mx.symbol.broadcast_div(${tc.currentName}, _data_std_)
</#if>