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
import de.monticore.lang.monticar.cnnarch.predefined.Sigmoid;
import de.monticore.lang.monticar.cnnarch.predefined.Softmax;
import de.se_rwth.commons.logging.Log;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public class CNNArchTemplateController {

    public static final String FTL_FILE_ENDING = ".ftl";
    public static final String TEMPLATE_ELEMENTS_DIR_PATH = "elements/";
    public static final String TEMPLATE_CONTROLLER_KEY = "tc";
    public static final String ELEMENT_DATA_KEY = "element";

    private LayerNameCreator nameManager;
    private Configuration freemarkerConfig = TemplateConfiguration.get();
    private ArchitectureSymbol architecture;

    private Writer writer;
    private String mainTemplateNameWithoutEnding;
    private Target targetLanguage;
    private ArchitectureElementData dataElement;

    public CNNArchTemplateController(ArchitectureSymbol architecture) {
        setArchitecture(architecture);
    }

    public String getFileNameWithoutEnding() {
        return mainTemplateNameWithoutEnding + "_" + getFullArchitectureName();
    }

    public Target getTargetLanguage(){
        return targetLanguage;
    }

    public void setTargetLanguage(Target targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public ArchitectureElementData getCurrentElement() {
        return dataElement;
    }

    public void setCurrentElement(ArchitectureElementSymbol layer) {
        this.dataElement = new ArchitectureElementData(getName(layer), layer, this);
    }

    public void setCurrentElement(ArchitectureElementData dataElement) {
        this.dataElement = dataElement;
    }

    public ArchitectureSymbol getArchitecture() {
        return architecture;
    }

    public void setArchitecture(ArchitectureSymbol architecture) {
        this.architecture = architecture;
        this.nameManager = new LayerNameCreator(architecture);
    }

    public String getName(ArchitectureElementSymbol layer){
        return nameManager.getName(layer);
    }

    public String getArchitectureName(){
        return getArchitecture().getEnclosingScope().getSpanningSymbol().get().getName().replaceAll("\\.","_");
    }

    public String getFullArchitectureName(){
        return getArchitecture().getEnclosingScope().getSpanningSymbol().get().getFullName().replaceAll("\\.","_");
    }

    public List<String> getLayerInputs(ArchitectureElementSymbol layer){
        List<String> inputNames = new ArrayList<>();

        if (isSoftmaxOutput(layer) || isLogisticRegressionOutput(layer)){
            inputNames = getLayerInputs(layer.getInputElement().get());
        }
        else {
            for (ArchitectureElementSymbol input : layer.getPrevious()) {
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
        for (IOSymbol ioElement : getArchitecture().getInputs()){
            list.add(nameManager.getName(ioElement));
        }
        return list;
    }

    public List<String> getArchitectureOutputs(){
        List<String> list = new ArrayList<>();
        for (IOSymbol ioElement : getArchitecture().getOutputs()){
            list.add(nameManager.getName(ioElement));
        }
        return list;
    }

    public void include(String relativePath, String templateWithoutFileEnding, Writer writer){
        String templatePath = relativePath + templateWithoutFileEnding + FTL_FILE_ENDING;

        try {
            Template template = freemarkerConfig.getTemplate(templatePath);
            Map<String, Object> ftlContext = new HashMap<>();
            ftlContext.put(TEMPLATE_CONTROLLER_KEY, this);
            ftlContext.put(ELEMENT_DATA_KEY, getCurrentElement());

            this.writer = writer;
            template.process(ftlContext, writer);
            this.writer = null;
        }
        catch (IOException e) {
            Log.error("Freemarker could not find template " + templatePath + " :\n" + e.getMessage());
            System.exit(1);
        }
        catch (TemplateException e){
            Log.error("An exception occured in template " + templatePath + " :\n" + e.getMessage());
            System.exit(1);
        }
    }

    public void include(IOSymbol ioElement, Writer writer){
        ArchitectureElementData previousElement = getCurrentElement();
        setCurrentElement(ioElement);

        if (ioElement.isAtomic()){
            if (ioElement.isInput()){
                include(TEMPLATE_ELEMENTS_DIR_PATH, "Input", writer);
            }
            else {
                include(TEMPLATE_ELEMENTS_DIR_PATH, "Output", writer);
            }
        }
        else {
            include(ioElement.getResolvedThis().get(), writer);
        }

        setCurrentElement(previousElement);
    }

    public void include(LayerSymbol layer, Writer writer){
        ArchitectureElementData previousElement = getCurrentElement();
        setCurrentElement(layer);

        if (layer.isAtomic()){
            ArchitectureElementSymbol nextElement = layer.getOutputElement().get();
            if (!isSoftmaxOutput(nextElement) && !isLogisticRegressionOutput(nextElement)){
                String templateName = layer.getDeclaration().getName();
                include(TEMPLATE_ELEMENTS_DIR_PATH, templateName, writer);
            }
        }
        else {
            include(layer.getResolvedThis().get(), writer);
        }

        setCurrentElement(previousElement);
    }

    public void include(CompositeElementSymbol compositeElement, Writer writer){
        ArchitectureElementData previousElement = getCurrentElement();
        setCurrentElement(compositeElement);

        for (ArchitectureElementSymbol element : compositeElement.getElements()){
            include(element, writer);
        }

        setCurrentElement(previousElement);
    }

    public void include(ArchitectureElementSymbol architectureElement, Writer writer){
        if (architectureElement instanceof CompositeElementSymbol){
            include((CompositeElementSymbol) architectureElement, writer);
        }
        else if (architectureElement instanceof LayerSymbol){
            include((LayerSymbol) architectureElement, writer);
        }
        else {
            include((IOSymbol) architectureElement, writer);
        }
    }

    public void include(ArchitectureElementSymbol architectureElement){
        if (writer == null){
            throw new IllegalStateException("missing writer");
        }
        include(architectureElement, writer);
    }

    public Map.Entry<String,String> process(String templateNameWithoutEnding, Target targetLanguage){
        StringWriter writer = new StringWriter();
        this.mainTemplateNameWithoutEnding = templateNameWithoutEnding;
        this.targetLanguage = targetLanguage;
        include("", templateNameWithoutEnding, writer);

        String fileEnding = targetLanguage.toString();
        if (targetLanguage == Target.CPP){
            fileEnding = ".h";
        }
        String fileName = getFileNameWithoutEnding() + fileEnding;

        Map.Entry<String,String> fileContent = new AbstractMap.SimpleEntry<>(fileName, writer.toString());

        this.mainTemplateNameWithoutEnding = null;
        this.targetLanguage = null;
        return fileContent;
    }

    public String join(Iterable iterable, String separator){
        return join(iterable, separator, "", "");
    }

    public String join(Iterable iterable, String separator, String elementPrefix, String elementPostfix){
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirst = true;
        for (Object element : iterable){
            if (!isFirst){
                stringBuilder.append(separator);
            }
            stringBuilder.append(elementPrefix);
            stringBuilder.append(element.toString());
            stringBuilder.append(elementPostfix);
            isFirst = false;
        }
        return stringBuilder.toString();
    }


    public boolean isLogisticRegressionOutput(ArchitectureElementSymbol architectureElement){
        return isTOutput(Sigmoid.class, architectureElement);
    }

    public boolean isLinearRegressionOutput(ArchitectureElementSymbol architectureElement){
        return architectureElement.isOutput()
                && !isLogisticRegressionOutput(architectureElement)
                && !isSoftmaxOutput(architectureElement);
    }


    public boolean isSoftmaxOutput(ArchitectureElementSymbol architectureElement){
        return isTOutput(Softmax.class, architectureElement);
    }

    private boolean isTOutput(Class inputPredefinedLayerClass, ArchitectureElementSymbol architectureElement){
        if (architectureElement.isOutput()){
            if (architectureElement.getInputElement().isPresent() && architectureElement.getInputElement().get() instanceof LayerSymbol){
                LayerSymbol inputLayer = (LayerSymbol) architectureElement.getInputElement().get();
                if (inputPredefinedLayerClass.isInstance(inputLayer.getDeclaration())){
                    return true;
                }
            }
        }
        return false;
    }
}
