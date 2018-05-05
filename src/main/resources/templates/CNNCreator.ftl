import mxnet as mx
import logging
import os
import errno
import shutil
import h5py
import sys
import numpy as np

@mx.init.register
class MyConstant(mx.init.Initializer):
    def __init__(self, value):
        super(MyConstant, self).__init__(value=value)
        self.value = value
    def _init_weight(self, _, arr):
        arr[:] = mx.nd.array(self.value)

class ${tc.fileNameWithoutEnding}:

    module = None
    _data_dir_ = "data/${tc.fullArchitectureName}/"
    _model_dir_ = "model/${tc.fullArchitectureName}/"
    _model_prefix_ = "${tc.architectureName}"
    _input_names_ = [${tc.join(tc.architectureInputs, ",", "'", "'")}]
    _input_shapes_ = [<#list tc.architecture.inputs as input>(${tc.join(input.definition.type.dimensions, ",")})</#list>]
    _output_names_ = [${tc.join(tc.architectureOutputs, ",", "'", "_label'")}]


    def load(self, context):
        lastEpoch = 0
        param_file = None

        try:
            os.remove(self._model_dir_ + self._model_prefix_ + "_newest-0000.params")
        except OSError:
            pass
        try:
            os.remove(self._model_dir_ + self._model_prefix_ + "_newest-symbol.json")
        except OSError:
            pass

        if os.path.isdir(self._model_dir_):
            for file in os.listdir(self._model_dir_):
                if ".params" in file and self._model_prefix_ in file:
                    epochStr = file.replace(".params","").replace(self._model_prefix_ + "-","")
                    epoch = int(epochStr)
                    if epoch > lastEpoch:
                        lastEpoch = epoch
                        param_file = file
        if param_file is None:
            return 0
        else:
            logging.info("Loading checkpoint: " + param_file)
            self.module.load(prefix=self._model_dir_ + self._model_prefix_,
                              epoch=lastEpoch,
                              data_names=self._input_names_,
                              label_names=self._output_names_,
                              context=context)
            return lastEpoch


    def load_data(self, batch_size):
        train_h5, test_h5 = self.load_h5_files()

        data_mean = train_h5[self._input_names_[0]][:].mean(axis=0)
        data_std = train_h5[self._input_names_[0]][:].std(axis=0) + 1e-5

        train_iter = mx.io.NDArrayIter(train_h5[self._input_names_[0]],
                                       train_h5[self._output_names_[0]],
                                       batch_size=batch_size,
                                       data_name=self._input_names_[0],
                                       label_name=self._output_names_[0])
        test_iter = None
        if test_h5 != None:
            test_iter = mx.io.NDArrayIter(test_h5[self._input_names_[0]],
                                          test_h5[self._output_names_[0]],
                                          batch_size=batch_size,
                                          data_name=self._input_names_[0],
                                          label_name=self._output_names_[0])
        return train_iter, test_iter, data_mean, data_std

    def load_h5_files(self):
        train_h5 = None
        test_h5 = None
        train_path = self._data_dir_ + "train.h5"
        test_path = self._data_dir_ + "test.h5"
        if os.path.isfile(train_path):
            train_h5 = h5py.File(train_path, 'r')
            if not (self._input_names_[0] in train_h5 and self._output_names_[0] in train_h5):
                logging.error("The HDF5 file '" + os.path.abspath(train_path) + "' has to contain the datasets: "
                              + "'" + self._input_names_[0] + "', '" + self._output_names_[0] + "'")
                sys.exit(1)
            test_iter = None
            if os.path.isfile(test_path):
                test_h5 = h5py.File(test_path, 'r')
                if not (self._input_names_[0] in test_h5 and self._output_names_[0] in test_h5):
                    logging.error("The HDF5 file '" + os.path.abspath(test_path) + "' has to contain the datasets: "
                                  + "'" + self._input_names_[0] + "', '" + self._output_names_[0] + "'")
                    sys.exit(1)
            else:
                logging.warning("Couldn't load test set. File '" + os.path.abspath(test_path) + "' does not exist.")
            return train_h5, test_h5
        else:
            logging.error("Data loading failure. File '" + os.path.abspath(train_path) + "' does not exist.")
            sys.exit(1)


    def train(self, batch_size,
              num_epoch=10,
              optimizer='adam',
              optimizer_params=(('learning_rate', 0.001),),
              load_checkpoint=True,
              context=mx.gpu(),
              checkpoint_period=5,
              normalize=True):

        if 'weight_decay' in optimizer_params:
            optimizer_params['wd'] = optimizer_params['weight_decay']
            del optimizer_params['weight_decay']
        if 'learning_rate_decay' in optimizer_params:
            min_learning_rate = 1e-08
            if 'learning_rate_minimum' in optimizer_params:
                min_learning_rate = optimizer_params['learning_rate_minimum']
                del optimizer_params['learning_rate_minimum']
            optimizer_params['lr_scheduler'] = mx.lr_scheduler.FactorScheduler(
                                                   optimizer_params['step_size'],
                                                   factor=optimizer_params['learning_rate_decay'],
                                                   stop_factor_lr=min_learning_rate)
            del optimizer_params['step_size']
            del optimizer_params['learning_rate_decay']


        train_iter, test_iter, data_mean, data_std = self.load_data(batch_size)
        if self.module == None:
            if normalize:
                self.construct(context, data_mean, data_std)
            else:
                self.construct(context)

        begin_epoch = 0
        if load_checkpoint:
            begin_epoch = self.load(context)
        else:
            if os.path.isdir(self._model_dir_):
                shutil.rmtree(self._model_dir_)

        try:
            os.makedirs(self._model_dir_)
        except OSError:
            if not os.path.isdir(self._model_dir_):
                raise

        self.module.fit(
            train_data=train_iter,
            eval_data=test_iter,
            optimizer=optimizer,
            optimizer_params=optimizer_params,
            batch_end_callback=mx.callback.Speedometer(batch_size),
            epoch_end_callback=mx.callback.do_checkpoint(prefix=self._model_dir_ + self._model_prefix_, period=checkpoint_period),
            begin_epoch=begin_epoch,
            num_epoch=num_epoch + begin_epoch)
        self.module.save_checkpoint(self._model_dir_ + self._model_prefix_, num_epoch + begin_epoch)
        self.module.save_checkpoint(self._model_dir_ + self._model_prefix_ + '_newest', 0)


    def construct(self, context, data_mean=None, data_std=None):
${tc.include(tc.architecture.body)}
        self.module = mx.mod.Module(symbol=mx.symbol.Group([${tc.join(tc.architectureOutputs, ",")}]),
                                         data_names=self._input_names_,
                                         label_names=self._output_names_,
                                         context=context)