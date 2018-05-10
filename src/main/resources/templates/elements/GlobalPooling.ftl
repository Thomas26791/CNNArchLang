        ${element.name} = mx.symbol.Pooling(data=${element.inputs[0]},
            global_pool=True,
            kernel=(1,1),
            pool_type=${element.poolType},
            name="${element.name}")
<#include "OutputShape.ftl">