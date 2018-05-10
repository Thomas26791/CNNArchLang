        ${element.name} = mx.symbol.concat(${tc.join(element.inputs, ", ")},
            dim=1,
            name="${element.name}")
<#include "OutputShape.ftl">