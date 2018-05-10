        ${element.name} = mx.symbol.Activation(data=${element.inputs[0]},
            act_type='relu',
            name="${element.name}")

