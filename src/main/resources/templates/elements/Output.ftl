
<#if element.softmaxOutput>
        ${element.name} = mx.symbol.SoftmaxOutput(data=${element.inputs[0]},
            name="${element.name}")
<#elseif element.logisticRegressionOutput>
        ${element.name} = mx.symbol.LogisticRegressionOutput(data=${element.inputs[0]},
            name="${element.name}")
<#elseif element.linearRegressionOutput>
        ${element.name} = mx.symbol.LinearRegressionOutput(data=${element.inputs[0]},
            name="${element.name}")
</#if>