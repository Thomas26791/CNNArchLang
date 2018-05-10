<#-- This template is not used if the followiing architecture element is an output. See Output.ftl -->
        ${element.name} = mx.symbol.softmax(data=${element.inputs[0]},
            axis=1,
            name="${element.name}")
