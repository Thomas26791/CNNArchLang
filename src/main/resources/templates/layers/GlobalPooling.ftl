<#if tc.targetLanguage == ".py">
        ${tc.currentName} = mx.symbol.Pooling(data=${tc.currentInputs[0]},
            global_pool=True<#if false>,
            cudnn_off=None</#if>,
            kernel=(1,1),
            pool_type=${tc.poolType},
            name="${tc.currentName}")
<#elseif tc.targetLanguage == ".cpp">
        auto ${tc.currentName} = Operator("Pooling")
            .SetParam("kernel", Shape(1,1))
            .SetParam("pool_type", ${tc.poolType})
            .SetParam("global_pool", true)
            .SetInput("data", ${tc.currentInputs[0]})
            .CreateSymbol("${tc.currentName}");
</#if>
<#include "OutputShape.ftl">