<#assign flatten = element.element.inputTypes[0].height != 1 || element.element.inputTypes[0].width != 1>
<#assign input = element.inputs[0]>
<#if flatten>
        ${element.name} = mx.symbol.flatten(data=${input})
<#assign input = element.name>
</#if>
        ${element.name} = mx.symbol.FullyConnected(data=${input},
            num_hidden=${element.units?c},
            no_bias=${element.noBias?string("True","False")},
            name="${element.name}")
