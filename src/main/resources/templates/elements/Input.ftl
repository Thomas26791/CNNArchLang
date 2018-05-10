<#assign channelIndex = element.element.outputTypes[0].channelIndex + 1>
<#assign heightIndex = element.element.outputTypes[0].heightIndex + 1>
<#assign widthIndex = element.element.outputTypes[0].widthIndex + 1>
<#assign indexList = []>
<#if channelIndex != 0><#assign indexList = indexList + [channelIndex]></#if>
<#if heightIndex != 0><#assign indexList = indexList + [heightIndex]></#if>
<#if widthIndex != 0><#assign indexList = indexList + [widthIndex]></#if>
<#assign dimensions = element.element.outputTypes[0].dimensions>
        ${element.name} = mx.sym.var("${element.name}",
            shape=(0,${tc.join(dimensions, ",")}))
<#include "OutputShape.ftl">
<#if heightIndex != channelIndex + 1 || widthIndex != heightIndex + 1>
        ${element.name} = mx.symbol.transpose(data=${element.name},
            axes=(0,${tc.join(indexList, ",")}))

</#if>
<#if indexList?size != 3>
        ${element.name} = mx.symbol.reshape(data=${element.name},
            shape=(0,${element.element.outputTypes[0].channels?c},${element.element.outputTypes[0].height?c},${element.element.outputTypes[0].width?c}))
</#if>
        if not data_mean is None:
            assert(not data_std is None)
            _data_mean_ = mx.sym.Variable("_data_mean_", shape=(${tc.join(dimensions, ",")}), init=MyConstant(value=data_mean.tolist()))
            _data_mean_ = mx.sym.BlockGrad(_data_mean_)
            _data_std_ = mx.sym.Variable("_data_std_", shape=(${tc.join(dimensions, ",")}), init=MyConstant(value=data_mean.tolist()))
            _data_std_ = mx.sym.BlockGrad(_data_std_)
            ${element.name} = mx.symbol.broadcast_sub(${element.name}, _data_mean_)
            ${element.name} = mx.symbol.broadcast_div(${element.name}, _data_std_)
