        ${element.name} = mx.symbol.LRN(data=${element.inputs[0]},
            alpha=${element.alpha?c},
            beta=${element.beta?c},
            knorm=${element.knorm?c},
            nsize=${element.nsize?c},
            name="${element.name}")
