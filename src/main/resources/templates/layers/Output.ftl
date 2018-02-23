<#if tc.target == ".py">
<#if tc.softmaxOutput>
        self.${tc.currentName} = mx.symbol.SoftmaxOutput(data=${tc.currentInputs[0]},
            name="${tc.currentName}")
<#elseif tc.logisticRegressionOutput>
        self.${tc.currentName} = mx.symbol.LogisticRegressionOutput(data=${tc.currentInputs[0]},
            name="${tc.currentName}")
<#elseif tc.linearRegressionOutput>
        self.${tc.currentName} = mx.symbol.LinearRegressionOutput(data=${tc.currentInputs[0]},
            name="${tc.currentName}")
</#if>
<#elseif tc.target == ".cpp">
<#if tc.softmaxOutput>
        m_${tc.currentName} = Operator("SoftmaxOutput")
            .SetParam("data", ${tc.currentInputs[0]})
            .CreateSymbol("${tc.currentName}");
<#elseif tc.logisticRegressionOutput>
        m_${tc.currentName} = Operator("LogisticRegressionOutput")
            .SetParam("data", ${tc.currentInputs[0]})
            .CreateSymbol("${tc.currentName}");
<#elseif tc.linearRegressionOutput>
        m_${tc.currentName} = Operator("LinearRegressionOutput")
            .SetParam("data", ${tc.currentInputs[0]})
            .CreateSymbol("${tc.currentName}");
</#if>
</#if>