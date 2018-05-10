        ${element.name} = mx.symbol.Activation(data=${element.inputs[0]},
            act_type='sigmoid',
            name="${element.name}")
