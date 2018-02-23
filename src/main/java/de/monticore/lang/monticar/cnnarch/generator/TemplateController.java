/**
 *
 *  ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
package de.monticore.lang.monticar.cnnarch.generator;

import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedMethods;
import de.monticore.lang.monticar.cnnarch.predefined.Sigmoid;
import de.monticore.lang.monticar.cnnarch.predefined.Softmax;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.logging.Log;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public class TemplateController {

    public static final String FTL_FILE_ENDING = ".ftl";
    public static final String TEMPLATE_LAYER_DIR_PATH = "layers/";
    public static final String TEMPLATE_CONTROLLER_KEY = "tc";

    private LayerNameCreator nameManager;
    private Configuration freemarkerConfig = TemplateConfiguration.get();

    private ArchitectureSymbol architecture;
    private LayerSymbol currentLayer;
    private Target target;

    public TemplateController(ArchitectureSymbol architecture, Target target) {
        setArchitecture(architecture);
        this.target = target;
    }

    public String getTarget(){
        return target.toString();
    }

    public LayerSymbol getCurrentLayer() {
        return currentLayer;
    }

    public void setCurrentLayer(LayerSymbol currentLayer) {
        this.currentLayer = currentLayer;
    }

    public ArchitectureSymbol getArchitecture() {
        return architecture;
    }

    public void setArchitecture(ArchitectureSymbol architecture) {
        this.architecture = architecture;
        this.nameManager = new LayerNameCreator(architecture);
    }

    public String getCurrentOutputShape(){
        return getOutputShape(getCurrentLayer());
    }

    public String getOutputShape(LayerSymbol layer){
        if (layer.getOutputTypes().size() == 1){
            return shapeToString(layer.getOutputTypes().get(0));
        }
        else {
            List<String> strings = new ArrayList<>();
            for (ArchTypeSymbol shape : layer.getOutputTypes()){
                strings.add(shapeToString(shape));
            }
            return "{" + Joiners.COMMA.join(strings) + "}";
        }
    }

    private String shapeToString(ArchTypeSymbol shape){
        return "[" + Joiners.COMMA.join(shape.getDimensions()) + "]";
    }

    public String getCurrentName(){
        return getName(getCurrentLayer());
    }

    public String getName(LayerSymbol layer){
        return nameManager.getName(layer);
    }

    public List<String> getCurrentInputs(){
        return getInputs(getCurrentLayer());
    }

    public List<String> getInputs(LayerSymbol layer){
        List<String> inputNames = new ArrayList<>();

        if (isSoftmaxOutput(layer) || isLogisticRegressionOutput(layer)){
            inputNames = getInputs(layer.getInputLayer().get());
        }
        else {
            for (LayerSymbol input : layer.getPrevious()) {
                if (input.getOutputTypes().size() == 1) {
                    inputNames.add(getName(input));
                } else {
                    for (int i = 0; i < input.getOutputTypes().size(); i++) {
                        inputNames.add(getName(input) + "[" + i + "]");
                    }
                }
            }
        }
        return inputNames;

    }

    public List<String> getArchitectureInputs(){
        List<String> list = new ArrayList<>();
        for (IOLayerSymbol layer : getArchitecture().getInputs()){
            list.add(nameManager.getName(layer));
        }
        return list;
    }

    public List<String> getArchitectureOutputs(){
        List<String> list = new ArrayList<>();
        for (IOLayerSymbol layer : getArchitecture().getOutputs()){
            list.add(nameManager.getName(layer));
        }
        return list;
    }

    public void include(String relativePath, String templateWithoutFileEnding, Writer stringWriter){
        String templatePath = relativePath + templateWithoutFileEnding + FTL_FILE_ENDING;

        try {
            Template template = freemarkerConfig.getTemplate(templatePath);
            Map<String, Object> ftlContext = new HashMap<>();
            ftlContext.put(TEMPLATE_CONTROLLER_KEY, this);
            template.process(ftlContext, stringWriter);
        }
        catch (IOException e) {
            Log.error("Freemarker could not find template " + templatePath + " :\n" + e.getMessage());
        }
        catch (TemplateException e){
            Log.error("An exception occured in template " + templatePath + " :\n" + e.getMessage());
        }
    }

    public void include(IOLayerSymbol layer, Writer writer){
        LayerSymbol previousLayer = getCurrentLayer();
        setCurrentLayer(layer);

        String result;
        if (layer.isInput()){
            include(TEMPLATE_LAYER_DIR_PATH, "Input", writer);
        }
        else {
            include(TEMPLATE_LAYER_DIR_PATH, "Output", writer);
        }
        setCurrentLayer(previousLayer);
    }

    public void include(MethodLayerSymbol layer, Writer writer){
        LayerSymbol previousLayer = getCurrentLayer();
        setCurrentLayer(layer);
        if (layer.isAtomic()){

            LayerSymbol nextLayer = layer.getOutputLayer().get();
            if (!isSoftmaxOutput(nextLayer) && !isLogisticRegressionOutput(nextLayer)){
                String templateName = layer.getMethod().getName();
                include(TEMPLATE_LAYER_DIR_PATH, templateName, writer);
            }

            setCurrentLayer(previousLayer);
        }
        else {
            include(layer.getResolvedThis().get(), writer);
        }
    }

    public void include(CompositeLayerSymbol compositeLayer, Writer writer){
        LayerSymbol previousLayer = getCurrentLayer();
        setCurrentLayer(compositeLayer);

        for (LayerSymbol layer : compositeLayer.getLayers()){
            include(layer, writer);
        }

        setCurrentLayer(previousLayer);
    }

    public void include(LayerSymbol layer, Writer writer){
        if (layer instanceof CompositeLayerSymbol){
            include((CompositeLayerSymbol) layer, writer);
        }
        else if (layer instanceof MethodLayerSymbol){
            include((MethodLayerSymbol) layer, writer);
        }
        else {
            include((IOLayerSymbol) layer, writer);
        }
    }

    public String include(LayerSymbol layer){
        StringWriter writer = new StringWriter();
        include(layer, writer);
        return writer.toString();
    }

    public void process(Writer writer) throws IOException{
        include("", "Network", writer);
    }

    public String process(){
        StringWriter writer = new StringWriter();
        include("", "Network", writer);
        return writer.toString();
    }

    public String join(Iterable iterable, String separator){
        return join(iterable, separator, "", "");
    }

    public String join(Iterable iterable, String separator, String elementPrefix, String elementPostfix){
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirst = true;
        for (Object element : iterable){
            if (!isFirst){
                stringBuilder.append(",");
            }
            stringBuilder.append(elementPrefix);
            stringBuilder.append(element.toString());
            stringBuilder.append(elementPostfix);
            isFirst = false;
        }
        return stringBuilder.toString();
    }


    public boolean isLogisticRegressionOutput(){
        return isLogisticRegressionOutput(getCurrentLayer());
    }

    public boolean isLogisticRegressionOutput(LayerSymbol layer){
        return isTOutput(Sigmoid.class, layer);
    }

    public boolean isLinearRegressionOutput(){
        boolean result = isLinearRegressionOutput(getCurrentLayer());
        if (result){
            Log.warn("The Output '" + getCurrentLayer().getName() + "' is a linear regression output (squared loss) during training" +
                            " because the previous layer is not a softmax (cross-entropy loss) or sigmoid (logistic regression loss) activation. " +
                            "Other loss functions are currently not supported. "
                    , getCurrentLayer().getSourcePosition());
        }
        return result;
    }

    public boolean isLinearRegressionOutput(LayerSymbol layer){
        return layer.isOutput()
                && !isLogisticRegressionOutput(layer)
                && !isSoftmaxOutput(layer);
    }

    public boolean isSoftmaxOutput(){
        return isSoftmaxOutput(getCurrentLayer());
    }

    public boolean isSoftmaxOutput(LayerSymbol layer){
        return isTOutput(Softmax.class, layer);
    }

    private boolean isTOutput(Class inputLayerMethodClass, LayerSymbol layer){
        if (layer.isOutput()){
            if (layer.getInputLayer().isPresent() && layer.getInputLayer().get() instanceof MethodLayerSymbol){
                MethodLayerSymbol inputLayer = (MethodLayerSymbol) layer.getInputLayer().get();
                if (inputLayerMethodClass.isInstance(inputLayer.getMethod())){
                    return true;
                }
            }
        }
        return false;
    }



    public List<Integer> getKernel(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getIntTupleValue(AllPredefinedMethods.KERNEL_NAME).get();
    }

    public int getChannels(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getIntValue(AllPredefinedMethods.CHANNELS_NAME).get();
    }

    public List<Integer> getStride(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getIntTupleValue(AllPredefinedMethods.STRIDE_NAME).get();
    }

    public int getUnits(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getIntValue(AllPredefinedMethods.UNITS_NAME).get();
    }

    public boolean getNoBias(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getBooleanValue(AllPredefinedMethods.NOBIAS_NAME).get();
    }

    public double getP(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getDoubleValue(AllPredefinedMethods.P_NAME).get();
    }

    public int getIndex(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getIntValue(AllPredefinedMethods.INDEX_NAME).get();
    }

    public int getNumOutputs(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getIntValue(AllPredefinedMethods.NUM_SPLITS_NAME).get();
    }

    public boolean getFixGamma(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getBooleanValue(AllPredefinedMethods.FIX_GAMMA_NAME).get();
    }

    public int getNsize(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getIntValue(AllPredefinedMethods.NSIZE_NAME).get();
    }

    public double getKnorm(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getDoubleValue(AllPredefinedMethods.KNORM_NAME).get();
    }

    public double getAlpha(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getDoubleValue(AllPredefinedMethods.ALPHA_NAME).get();
    }

    public double getBeta(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getDoubleValue(AllPredefinedMethods.BETA_NAME).get();
    }

    @Nullable
    public String getPoolType(){
        return ((MethodLayerSymbol) getCurrentLayer())
                .getStringValue(AllPredefinedMethods.POOL_TYPE_NAME).get();
    }

    @Nullable
    public List<Integer> getPadding(){
        return getPadding((MethodLayerSymbol) getCurrentLayer());
    }

    @Nullable
    public List<Integer> getPadding(MethodLayerSymbol layer){
        List<Integer> kernel = layer.getIntTupleValue(AllPredefinedMethods.KERNEL_NAME).get();
        List<Integer> stride = layer.getIntTupleValue(AllPredefinedMethods.STRIDE_NAME).get();
        ArchTypeSymbol inputType = layer.getInputTypes().get(0);
        ArchTypeSymbol outputType = layer.getOutputTypes().get(0);

        int heightWithPad = kernel.get(0) + stride.get(0)*(outputType.getHeight() - 1);
        int widthWithPad = kernel.get(1) + stride.get(1)*(outputType.getWidth() - 1);
        int heightPad = Math.max(0, heightWithPad - inputType.getHeight());
        int widthPad = Math.max(0, widthWithPad - inputType.getWidth());

        int topPad = (int)Math.ceil(heightPad / 2.0);
        int bottomPad = (int)Math.floor(heightPad / 2.0);
        int leftPad = (int)Math.ceil(widthPad / 2.0);
        int rightPad = (int)Math.floor(widthPad / 2.0);

        if (topPad == 0 && bottomPad == 0 && leftPad == 0 && rightPad == 0){
            return null;
        }

        return Arrays.asList(0,0,0,0,topPad,bottomPad,leftPad,rightPad);
    }

}
