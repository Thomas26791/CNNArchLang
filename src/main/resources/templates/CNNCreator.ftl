import mxnet as mx
import logging
import os
import shutil
import h5py
import sys

class ${tc.fileNameWithoutEnding}:
<#list tc.architectureInputs as input>
    ${input} = None
</#list>
<#list tc.architectureOutputs as output>
    ${output} = None
</#list>

    outputGroup_ = None
    module_ = None
    begin_epoch_ = 0
    train_iter_ = None
    test_iter_ = None
    context_ = None
    checkpoint_period_ = 1

    _data_dir_ = "data/${tc.fullArchitectureName}/"
    _model_dir_ = "model/${tc.fullArchitectureName}/"
    _model_prefix_ = "${tc.architectureName}"
    _input_names_ = [${tc.join(tc.architectureInputs, ",", "'", "'")}]
    _input_shapes_ = [<#list tc.architecture.inputs as input>(${tc.join(input.definition.type.dimensions, ",")})</#list>]
    _output_names_ = [${tc.join(tc.architectureOutputs, ",", "'", "_label'")}]

    def __init__(self, context=mx.gpu()):
        self.context_ = context
        self.construct()
        self.outputGroup_ = mx.symbol.Group([${tc.join(tc.architectureOutputs, ",", "self.", "")}])
        self.module_ = mx.mod.Module(symbol=self.outputGroup_,
                                      data_names=self._input_names_,
                                      label_names=self._output_names_,
                                      context=self.context_)

    def load(self):
        lastEpoch = 0
        param_file = None
        if os.path.isdir(self._model_dir_):
            for file in os.listdir(self._model_dir_):
                if ".params" in file and self._model_prefix_ in file:
                    epochStr = file.replace(".params","").replace(self._model_prefix_ + "-","")
                    epoch = int(epochStr)
                    if epoch > lastEpoch:
                        lastEpoch = epoch
                        param_file = file
        if param_file != None:
            logging.info("Loading checkpoint: " + param_file)
            self.begin_epoch_ = lastEpoch
            self.module_.load(prefix=self._model_dir_ + self._model_prefix_,
                              epoch=lastEpoch,
                              data_names=self._input_names_,
                              label_names=self._output_names_,
                              context=self.context_)


    def getH5ArrayIter(self, batch_size):
        train_path = self._data_dir_ + "train.h5"
        test_path = self._data_dir_ + "test.h5"
        if os.path.isfile(train_path):
            train_file = h5py.File(train_path, 'r')
            if self._input_names_[0] in train_file and self._output_names_[0] in train_file:
                train_iter = mx.io.NDArrayIter(train_file[self._input_names_[0]],
                                              train_file[self._output_names_[0]],
                                              batch_size=batch_size,
                                              data_name=self._input_names_[0],
                                              label_name=self._output_names_[0])
            else:
                logging.error("The HDF5 file '" + os.path.abspath(train_path) + "' has to contain the datasets: "
                              + "'" + self._input_names_[0] + "', '" + self._output_names_[0] + "'")
                sys.exit(1)
            test_iter = None
            if os.path.isfile(test_path):
                test_file = h5py.File(test_path, 'r')
                if self._input_names_[0] in test_file and self._output_names_[0] in test_file:
                    test_iter = mx.io.NDArrayIter(test_file[self._input_names_[0]],
                                                  test_file[self._output_names_[0]],
                                                  batch_size=batch_size,
                                                  data_name=self._input_names_[0],
                                                  label_name=self._output_names_[0])
                else:
                    logging.error("The HDF5 file '" + os.path.abspath(test_path) + "' has to contain the datasets: "
                                  + "'" + self._input_names_[0] + "', '" + self._output_names_[0] + "'")
                    sys.exit(1)
            else:
                logging.warning("Couldn't load test set. File '" + os.path.abspath(test_path) + "' does not exist.")
            return train_iter, test_iter
        else:
            logging.error("Data loading failure. File '" + os.path.abspath(train_path) + "' does not exist.")
            sys.exit(1)


    def train(self, batch_size,
              train_iter=None,
              test_iter=None,
              num_epoch=10,
              optimizer='adam',
              optimizer_params=(('learning_rate', 0.001),),
              load_checkpoint=True):

        if train_iter == None:
            train_iter, test_iter = self.getH5ArrayIter(batch_size)

        if load_checkpoint:
            self.load()
        else:
            if os.path.isdir(self._model_dir_):
                shutil.rmtree(self._model_dir_)

        try:
            os.makedirs(self._model_dir_)
        except OSError:
            if not os.path.isdir(self._model_dir_):
                raise

        self.module_.fit(
            train_data=train_iter,
            eval_data=test_iter,
            optimizer=optimizer,
            optimizer_params=optimizer_params,
            batch_end_callback=mx.callback.Speedometer(batch_size),
            epoch_end_callback=mx.callback.do_checkpoint(prefix=self._model_dir_ + self._model_prefix_, period=self.checkpoint_period_),
            begin_epoch=self.begin_epoch_,
            num_epoch=num_epoch + self.begin_epoch_)


    def construct(self):
${tc.include(tc.architecture.body)}