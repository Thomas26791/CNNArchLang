<#list tc.architecture.outputs as output>
    <#assign shape = output.definition.type.dimensions>
    vector<float> CNN_${tc.getName(output)}(<#list shape as dim>${dim?c}<#if  dim?has_next>*</#if></#list>);
</#list>

    _cnn_.predict(<#list tc.architecture.inputs as input>CNNTranslator::translate(${input.name}<#if input.arrayAccess.isPresent()>[${input.arrayAccess.get().intValue.get()?c}]</#if>),
                </#list><#list tc.architecture.outputs as output>CNN_${tc.getName(output)}<#if output?has_next>,
                </#if></#list>);

<#list tc.architecture.outputs as output>
<#assign shape = output.definition.type.dimensions>
<#if shape?size == 1>
    ${output.name}<#if output.arrayAccess.isPresent()>[${output.arrayAccess.get().intValue.get()?c}]</#if> = CNNTranslator::translateToCol(CNN_${tc.getName(output)}, std::vector<size_t> {${shape[0]?c}});
</#if>
<#if shape?size == 2>
    ${output.name}<#if output.arrayAccess.isPresent()>[${output.arrayAccess.get().intValue.get()?c}]</#if> = CNNTranslator::translateToMat(CNN_${tc.getName(output)}, std::vector<size_t> {${shape[0]?c}, ${shape[1]?c}});
</#if>
<#if shape?size == 3>
    ${output.name}<#if output.arrayAccess.isPresent()>[${output.arrayAccess.get().intValue.get()?c}]</#if> = CNNTranslator::translateToCube(CNN_${tc.getName(output)}, std::vector<size_t> {${shape[0]?c}, ${shape[1]?c}, ${shape[2]?c}});
</#if>
</#list>
