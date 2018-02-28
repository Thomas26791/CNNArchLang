<#if tc.targetLanguage == ".py">
import mxnet as mx
import logging
import os
import errno
import shutil
import numpy as np
from collections import namedtuple
Batch = namedtuple('Batch', ['data'])

logging.basicConfig(level=logging.DEBUG)

class ${tc.fileNameWithoutEnding}:
<#list tc.architectureInputs as input>
    ${input} = None
</#list>
<#list tc.architectureOutputs as output>
    ${output} = None
</#list>
    Module = None
    _checkpoint_dir = 'checkpoints/'

    def load(self):
        self.Module.load(prefix=self._checkpoint_dir)
        self.Module.bind(for_training=False,
                 data_shapes=[('data', (1,3,224,224))],
                 label_shapes=self.Module._label_shapes)


    def predict(self, image):
        # compute the predict probabilities
        self.Module.forward(Batch([mx.nd.array(image)]))
        prob = self.Module.get_outputs()[0].asnumpy()
        # top-5
        prob = np.squeeze(prob)
        return np.argsort(prob)[::-1]

    def train(self, train_iter, test_iter, batch_size, optimizer, num_epoch, checkpoint_period):
        shutil.rmtree(self._checkpoint_dir)
        try:
            os.makedirs(self._checkpoint_dir)
        except OSError:
            if not os.path.isdir(self._checkpoint_dir):
                raise

        self.Module.fit(
            train_data=train_iter,
            eval_data=test_iter,
            optimizer=optimizer,
            batch_end_callback=mx.callback.Speedometer(batch_size, 50),
            epoch_end_callback=mx.callback.do_checkpoint(prefix=self._checkpoint_dir+'${tc.architecture.name}', period=checkpoint_period),
            num_epoch=num_epoch)

    def __init__(self, context=mx.gpu()):
${tc.include(tc.architecture.body)}
        self.Module = mx.mod.Module(symbol=mx.symbol.Group([${tc.join(tc.architectureOutputs, ",", "self.", "")}]),
                             data_names=[${tc.join(tc.architectureInputs, ",", "'", "'")}],
                             label_names=[${tc.join(tc.architectureOutputs, ",", "'", "_label'")}],
                             context=context)

<#elseif tc.targetLanguage == ".cpp">
#ifndef ${tc.fileNameWithoutEnding?upper_case}
#define ${tc.fileNameWithoutEnding?upper_case}
#include "mxnet-cpp/MxNetCpp.h"

using namespace std;
using namespace mxnet::cpp;


class ${tc.fileNameWithoutEnding}{
    bool m_isTrained;
<#list tc.architectureInputs as input>
    Symbol m_${input}; //change to ${input}
</#list>
<#list tc.architectureOutputs as output>
    Symbol m_${output};
</#list>
    Module m_module;

    public:
    ${tc.fileNameWithoutEnding}(Context context = Context::gpu());
    void predict();
    void train();
<#list tc.architectureInputs as input>
    Symbol get${input?capitalize}();
</#list>
<#list tc.architectureOutputs as output>
    Symbol get${output?capitalize}();
</#list>
    Module getModule();
};

<#list tc.architectureInputs as input>
Symbol ${tc.fileNameWithoutEnding}::get${input?capitalize}(){
    return m_${input};
}
</#list>
<#list tc.architectureOutputs as output>
Symbol ${tc.fileNameWithoutEnding}::get${output?capitalize}(){
    return m_${output};
}
</#list>
Module ${tc.fileNameWithoutEnding}::getModule(){
    return m_module;
}

${tc.fileNameWithoutEnding}::${tc.fileNameWithoutEnding}(){
${tc.include(tc.architecture.body)}
        auto _group = Operator("Group")
            .SetInput("data", {${tc.join(tc.architectureOutputs, ",", "m_", "")}});
            .CreateSymbol();
        m_module = Module(symbol=group),
                          data_names=[${tc.join(tc.architectureInputs, ",", "'", "'")}],
                          label_names=[${tc.join(tc.architectureOutputs, ",", "'", "_label'")}],
                          context=context);
}

#endif
</#if>