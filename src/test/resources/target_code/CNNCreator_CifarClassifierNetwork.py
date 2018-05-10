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

class CNNCreator_CifarClassifierNetwork:

    module = None
    _data_dir_ = "data/CifarClassifierNetwork/"
    _model_dir_ = "model/CifarClassifierNetwork/"
    _model_prefix_ = "CifarClassifierNetwork"
    _input_names_ = ['data']
    _input_shapes_ = [(3,32,32)]
    _output_names_ = ['softmax_label']


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
        data = mx.sym.var("data",
            shape=(0,3,32,32))
        # data, output shape: {[3,32,32]}

        if not data_mean is None:
            assert(not data_std is None)
            _data_mean_ = mx.sym.Variable("_data_mean_", shape=(3,32,32), init=MyConstant(value=data_mean.tolist()))
            _data_mean_ = mx.sym.BlockGrad(_data_mean_)
            _data_std_ = mx.sym.Variable("_data_std_", shape=(3,32,32), init=MyConstant(value=data_mean.tolist()))
            _data_std_ = mx.sym.BlockGrad(_data_std_)
            data = mx.symbol.broadcast_sub(data, _data_mean_)
            data = mx.symbol.broadcast_div(data, _data_std_)
        conv2_1_ = mx.symbol.pad(data=data,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv2_1_ = mx.symbol.Convolution(data=conv2_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=8,
            no_bias=False,
            name="conv2_1_")
        # conv2_1_, output shape: {[8,32,32]}

        batchnorm2_1_ = mx.symbol.BatchNorm(data=conv2_1_,
            fix_gamma=True,
            name="batchnorm2_1_")
        relu2_1_ = mx.symbol.Activation(data=batchnorm2_1_,
            act_type='relu',
            name="relu2_1_")

        conv3_1_ = mx.symbol.pad(data=relu2_1_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv3_1_ = mx.symbol.Convolution(data=conv3_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=8,
            no_bias=False,
            name="conv3_1_")
        # conv3_1_, output shape: {[8,32,32]}

        batchnorm3_1_ = mx.symbol.BatchNorm(data=conv3_1_,
            fix_gamma=True,
            name="batchnorm3_1_")
        conv2_2_ = mx.symbol.Convolution(data=data,
            kernel=(1,1),
            stride=(1,1),
            num_filter=8,
            no_bias=False,
            name="conv2_2_")
        # conv2_2_, output shape: {[8,32,32]}

        batchnorm2_2_ = mx.symbol.BatchNorm(data=conv2_2_,
            fix_gamma=True,
            name="batchnorm2_2_")
        add4_ = batchnorm3_1_ + batchnorm2_2_
        # add4_, output shape: {[8,32,32]}

        relu4_ = mx.symbol.Activation(data=add4_,
            act_type='relu',
            name="relu4_")

        conv5_1_ = mx.symbol.pad(data=relu4_,
            mode='constant',
            pad_width=(0,0,0,0,1,0,1,0),
            constant_value=0)
        conv5_1_ = mx.symbol.Convolution(data=conv5_1_,
            kernel=(3,3),
            stride=(2,2),
            num_filter=16,
            no_bias=False,
            name="conv5_1_")
        # conv5_1_, output shape: {[16,16,16]}

        batchnorm5_1_ = mx.symbol.BatchNorm(data=conv5_1_,
            fix_gamma=True,
            name="batchnorm5_1_")
        relu5_1_ = mx.symbol.Activation(data=batchnorm5_1_,
            act_type='relu',
            name="relu5_1_")

        conv6_1_ = mx.symbol.pad(data=relu5_1_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv6_1_ = mx.symbol.Convolution(data=conv6_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=16,
            no_bias=False,
            name="conv6_1_")
        # conv6_1_, output shape: {[16,16,16]}

        batchnorm6_1_ = mx.symbol.BatchNorm(data=conv6_1_,
            fix_gamma=True,
            name="batchnorm6_1_")
        conv5_2_ = mx.symbol.Convolution(data=relu4_,
            kernel=(1,1),
            stride=(2,2),
            num_filter=16,
            no_bias=False,
            name="conv5_2_")
        # conv5_2_, output shape: {[16,16,16]}

        batchnorm5_2_ = mx.symbol.BatchNorm(data=conv5_2_,
            fix_gamma=True,
            name="batchnorm5_2_")
        add7_ = batchnorm6_1_ + batchnorm5_2_
        # add7_, output shape: {[16,16,16]}

        relu7_ = mx.symbol.Activation(data=add7_,
            act_type='relu',
            name="relu7_")

        conv8_1_ = mx.symbol.pad(data=relu7_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv8_1_ = mx.symbol.Convolution(data=conv8_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=16,
            no_bias=False,
            name="conv8_1_")
        # conv8_1_, output shape: {[16,16,16]}

        batchnorm8_1_ = mx.symbol.BatchNorm(data=conv8_1_,
            fix_gamma=True,
            name="batchnorm8_1_")
        relu8_1_ = mx.symbol.Activation(data=batchnorm8_1_,
            act_type='relu',
            name="relu8_1_")

        conv9_1_ = mx.symbol.pad(data=relu8_1_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv9_1_ = mx.symbol.Convolution(data=conv9_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=16,
            no_bias=False,
            name="conv9_1_")
        # conv9_1_, output shape: {[16,16,16]}

        batchnorm9_1_ = mx.symbol.BatchNorm(data=conv9_1_,
            fix_gamma=True,
            name="batchnorm9_1_")
        add10_ = batchnorm9_1_ + relu7_
        # add10_, output shape: {[16,16,16]}

        relu10_ = mx.symbol.Activation(data=add10_,
            act_type='relu',
            name="relu10_")

        conv11_1_ = mx.symbol.pad(data=relu10_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv11_1_ = mx.symbol.Convolution(data=conv11_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=16,
            no_bias=False,
            name="conv11_1_")
        # conv11_1_, output shape: {[16,16,16]}

        batchnorm11_1_ = mx.symbol.BatchNorm(data=conv11_1_,
            fix_gamma=True,
            name="batchnorm11_1_")
        relu11_1_ = mx.symbol.Activation(data=batchnorm11_1_,
            act_type='relu',
            name="relu11_1_")

        conv12_1_ = mx.symbol.pad(data=relu11_1_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv12_1_ = mx.symbol.Convolution(data=conv12_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=16,
            no_bias=False,
            name="conv12_1_")
        # conv12_1_, output shape: {[16,16,16]}

        batchnorm12_1_ = mx.symbol.BatchNorm(data=conv12_1_,
            fix_gamma=True,
            name="batchnorm12_1_")
        add13_ = batchnorm12_1_ + relu10_
        # add13_, output shape: {[16,16,16]}

        relu13_ = mx.symbol.Activation(data=add13_,
            act_type='relu',
            name="relu13_")

        conv14_1_ = mx.symbol.pad(data=relu13_,
            mode='constant',
            pad_width=(0,0,0,0,1,0,1,0),
            constant_value=0)
        conv14_1_ = mx.symbol.Convolution(data=conv14_1_,
            kernel=(3,3),
            stride=(2,2),
            num_filter=32,
            no_bias=False,
            name="conv14_1_")
        # conv14_1_, output shape: {[32,8,8]}

        batchnorm14_1_ = mx.symbol.BatchNorm(data=conv14_1_,
            fix_gamma=True,
            name="batchnorm14_1_")
        relu14_1_ = mx.symbol.Activation(data=batchnorm14_1_,
            act_type='relu',
            name="relu14_1_")

        conv15_1_ = mx.symbol.pad(data=relu14_1_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv15_1_ = mx.symbol.Convolution(data=conv15_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=32,
            no_bias=False,
            name="conv15_1_")
        # conv15_1_, output shape: {[32,8,8]}

        batchnorm15_1_ = mx.symbol.BatchNorm(data=conv15_1_,
            fix_gamma=True,
            name="batchnorm15_1_")
        conv14_2_ = mx.symbol.Convolution(data=relu13_,
            kernel=(1,1),
            stride=(2,2),
            num_filter=32,
            no_bias=False,
            name="conv14_2_")
        # conv14_2_, output shape: {[32,8,8]}

        batchnorm14_2_ = mx.symbol.BatchNorm(data=conv14_2_,
            fix_gamma=True,
            name="batchnorm14_2_")
        add16_ = batchnorm15_1_ + batchnorm14_2_
        # add16_, output shape: {[32,8,8]}

        relu16_ = mx.symbol.Activation(data=add16_,
            act_type='relu',
            name="relu16_")

        conv17_1_ = mx.symbol.pad(data=relu16_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv17_1_ = mx.symbol.Convolution(data=conv17_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=32,
            no_bias=False,
            name="conv17_1_")
        # conv17_1_, output shape: {[32,8,8]}

        batchnorm17_1_ = mx.symbol.BatchNorm(data=conv17_1_,
            fix_gamma=True,
            name="batchnorm17_1_")
        relu17_1_ = mx.symbol.Activation(data=batchnorm17_1_,
            act_type='relu',
            name="relu17_1_")

        conv18_1_ = mx.symbol.pad(data=relu17_1_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv18_1_ = mx.symbol.Convolution(data=conv18_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=32,
            no_bias=False,
            name="conv18_1_")
        # conv18_1_, output shape: {[32,8,8]}

        batchnorm18_1_ = mx.symbol.BatchNorm(data=conv18_1_,
            fix_gamma=True,
            name="batchnorm18_1_")
        add19_ = batchnorm18_1_ + relu16_
        # add19_, output shape: {[32,8,8]}

        relu19_ = mx.symbol.Activation(data=add19_,
            act_type='relu',
            name="relu19_")

        conv20_1_ = mx.symbol.pad(data=relu19_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv20_1_ = mx.symbol.Convolution(data=conv20_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=32,
            no_bias=False,
            name="conv20_1_")
        # conv20_1_, output shape: {[32,8,8]}

        batchnorm20_1_ = mx.symbol.BatchNorm(data=conv20_1_,
            fix_gamma=True,
            name="batchnorm20_1_")
        relu20_1_ = mx.symbol.Activation(data=batchnorm20_1_,
            act_type='relu',
            name="relu20_1_")

        conv21_1_ = mx.symbol.pad(data=relu20_1_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv21_1_ = mx.symbol.Convolution(data=conv21_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=32,
            no_bias=False,
            name="conv21_1_")
        # conv21_1_, output shape: {[32,8,8]}

        batchnorm21_1_ = mx.symbol.BatchNorm(data=conv21_1_,
            fix_gamma=True,
            name="batchnorm21_1_")
        add22_ = batchnorm21_1_ + relu19_
        # add22_, output shape: {[32,8,8]}

        relu22_ = mx.symbol.Activation(data=add22_,
            act_type='relu',
            name="relu22_")

        conv23_1_ = mx.symbol.pad(data=relu22_,
            mode='constant',
            pad_width=(0,0,0,0,1,0,1,0),
            constant_value=0)
        conv23_1_ = mx.symbol.Convolution(data=conv23_1_,
            kernel=(3,3),
            stride=(2,2),
            num_filter=64,
            no_bias=False,
            name="conv23_1_")
        # conv23_1_, output shape: {[64,4,4]}

        batchnorm23_1_ = mx.symbol.BatchNorm(data=conv23_1_,
            fix_gamma=True,
            name="batchnorm23_1_")
        relu23_1_ = mx.symbol.Activation(data=batchnorm23_1_,
            act_type='relu',
            name="relu23_1_")

        conv24_1_ = mx.symbol.pad(data=relu23_1_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv24_1_ = mx.symbol.Convolution(data=conv24_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=64,
            no_bias=False,
            name="conv24_1_")
        # conv24_1_, output shape: {[64,4,4]}

        batchnorm24_1_ = mx.symbol.BatchNorm(data=conv24_1_,
            fix_gamma=True,
            name="batchnorm24_1_")
        conv23_2_ = mx.symbol.Convolution(data=relu22_,
            kernel=(1,1),
            stride=(2,2),
            num_filter=64,
            no_bias=False,
            name="conv23_2_")
        # conv23_2_, output shape: {[64,4,4]}

        batchnorm23_2_ = mx.symbol.BatchNorm(data=conv23_2_,
            fix_gamma=True,
            name="batchnorm23_2_")
        add25_ = batchnorm24_1_ + batchnorm23_2_
        # add25_, output shape: {[64,4,4]}

        relu25_ = mx.symbol.Activation(data=add25_,
            act_type='relu',
            name="relu25_")

        conv26_1_ = mx.symbol.pad(data=relu25_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv26_1_ = mx.symbol.Convolution(data=conv26_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=64,
            no_bias=False,
            name="conv26_1_")
        # conv26_1_, output shape: {[64,4,4]}

        batchnorm26_1_ = mx.symbol.BatchNorm(data=conv26_1_,
            fix_gamma=True,
            name="batchnorm26_1_")
        relu26_1_ = mx.symbol.Activation(data=batchnorm26_1_,
            act_type='relu',
            name="relu26_1_")

        conv27_1_ = mx.symbol.pad(data=relu26_1_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv27_1_ = mx.symbol.Convolution(data=conv27_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=64,
            no_bias=False,
            name="conv27_1_")
        # conv27_1_, output shape: {[64,4,4]}

        batchnorm27_1_ = mx.symbol.BatchNorm(data=conv27_1_,
            fix_gamma=True,
            name="batchnorm27_1_")
        add28_ = batchnorm27_1_ + relu25_
        # add28_, output shape: {[64,4,4]}

        relu28_ = mx.symbol.Activation(data=add28_,
            act_type='relu',
            name="relu28_")

        conv29_1_ = mx.symbol.pad(data=relu28_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv29_1_ = mx.symbol.Convolution(data=conv29_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=64,
            no_bias=False,
            name="conv29_1_")
        # conv29_1_, output shape: {[64,4,4]}

        batchnorm29_1_ = mx.symbol.BatchNorm(data=conv29_1_,
            fix_gamma=True,
            name="batchnorm29_1_")
        relu29_1_ = mx.symbol.Activation(data=batchnorm29_1_,
            act_type='relu',
            name="relu29_1_")

        conv30_1_ = mx.symbol.pad(data=relu29_1_,
            mode='constant',
            pad_width=(0,0,0,0,1,1,1,1),
            constant_value=0)
        conv30_1_ = mx.symbol.Convolution(data=conv30_1_,
            kernel=(3,3),
            stride=(1,1),
            num_filter=64,
            no_bias=False,
            name="conv30_1_")
        # conv30_1_, output shape: {[64,4,4]}

        batchnorm30_1_ = mx.symbol.BatchNorm(data=conv30_1_,
            fix_gamma=True,
            name="batchnorm30_1_")
        add31_ = batchnorm30_1_ + relu28_
        # add31_, output shape: {[64,4,4]}

        relu31_ = mx.symbol.Activation(data=add31_,
            act_type='relu',
            name="relu31_")

        globalpooling31_ = mx.symbol.Pooling(data=relu31_,
            global_pool=True,
            kernel=(1,1),
            pool_type="avg",
            name="globalpooling31_")
        # globalpooling31_, output shape: {[64,1,1]}

        fc31_ = mx.symbol.FullyConnected(data=globalpooling31_,
            num_hidden=128,
            no_bias=False,
            name="fc31_")
        dropout31_ = mx.symbol.Dropout(data=fc31_,
            p=0.5,
            name="dropout31_")
        fc32_ = mx.symbol.FullyConnected(data=dropout31_,
            num_hidden=10,
            no_bias=False,
            name="fc32_")

        softmax = mx.symbol.SoftmaxOutput(data=fc32_,
            name="softmax")

        self.module = mx.mod.Module(symbol=mx.symbol.Group([softmax]),
                                         data_names=self._input_names_,
                                         label_names=self._output_names_,
                                         context=context)