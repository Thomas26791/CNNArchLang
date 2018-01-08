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
package de.monticore.lang.monticar.cnnarch._symboltable;


import de.monticore.lang.math.math._ast.ASTMathExpression;
import de.monticore.lang.math.math._ast.ASTMathFalseExpression;
import de.monticore.lang.math.math._ast.ASTMathTrueExpression;
import de.monticore.lang.math.math._symboltable.MathSymbolTableCreator;
import de.monticore.lang.math.math._symboltable.expression.MathExpressionSymbol;
import de.monticore.lang.math.math._symboltable.expression.MathNameExpressionSymbol;
import de.monticore.lang.math.math._visitor.MathVisitor;
import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.cnnarch._visitor.CNNArchInheritanceVisitor;
import de.monticore.lang.monticar.cnnarch._visitor.CNNArchVisitor;
import de.monticore.lang.monticar.cnnarch._visitor.CommonCNNArchDelegatorVisitor;
import de.monticore.lang.monticar.cnnarch.helper.Constraints;
import de.monticore.lang.monticar.cnnarch.helper.PredefinedMethods;
import de.monticore.lang.monticar.cnnarch.helper.PredefinedVariables;
import de.monticore.lang.monticar.types2._ast.ASTType;
import de.monticore.symboltable.*;
import de.se_rwth.commons.logging.Log;

import java.util.*;

public class CNNArchSymbolTableCreator extends de.monticore.symboltable.CommonSymbolTableCreator
        implements CNNArchInheritanceVisitor {

    private String compilationUnitPackage = "";

    private MathSymbolTableCreator mathSTC;
    private List<IODeclarationSymbol> inputs = new ArrayList<>();
    private List<IODeclarationSymbol> outputs = new ArrayList<>();


    public CNNArchSymbolTableCreator(final ResolvingConfiguration resolvingConfig,
                                     final MutableScope enclosingScope) {
        super(resolvingConfig, enclosingScope);
        initSuperSTC(resolvingConfig);
    }

    public CNNArchSymbolTableCreator(final ResolvingConfiguration resolvingConfig,
                                     final Deque<MutableScope> scopeStack) {
        super(resolvingConfig, scopeStack);
        initSuperSTC(resolvingConfig);
    }

    private void initSuperSTC(final ResolvingConfiguration resolvingConfig) {
        this.mathSTC = new ModifiedMathSymbolTableCreator(resolvingConfig, scopeStack);
        CommonCNNArchDelegatorVisitor visitor = new CommonCNNArchDelegatorVisitor();
        visitor.set_de_monticore_lang_monticar_cnnarch__visitor_CNNArchVisitor(this);
        visitor.set_de_monticore_lang_math_math__visitor_MathVisitor(mathSTC);

        setRealThis(visitor);
    }

    /**
     * Creates the symbol table starting from the <code>rootNode</code> and
     * returns the first scope that was created.
     *
     * @param rootNode the root node
     * @return the first scope that was created
     */
    public Scope createFromAST(de.monticore.lang.monticar.cnnarch._ast.ASTCNNArchNode rootNode) {
        Log.errorIfNull(rootNode, "0xA7004_650 Error by creating of the CNNArchSymbolTableCreatorTOP symbol table: top ast node is null");
        rootNode.accept(realThis);
        return getFirstCreatedScope();
    }

    private CNNArchVisitor realThis = this;

    @Override
    public CNNArchVisitor getRealThis() {
        return realThis;
    }

    @Override
    public void setRealThis(CNNArchVisitor realThis) {
        if (this.realThis != realThis) {
            this.realThis = realThis;
        }
    }


    @Override
    public void visit(final ASTCNNArchCompilationUnit compilationUnit) {
        Log.debug("Building Symboltable for Script: " + compilationUnit.getArchitecture().getName(),
                CNNArchSymbolTableCreator.class.getSimpleName());

        List<ImportStatement> imports = new ArrayList<>();

        ArtifactScope artifactScope = new ArtifactScope(
                Optional.empty(),
                compilationUnitPackage,
                imports);

        putOnStack(artifactScope);

    }

    @Override
    public void endVisit(ASTCNNArchCompilationUnit ast) {
        setEnclosingScopeOfNodes(ast);
    }

    public void visit(final ASTArchitecture node) {
        ArchitectureSymbol architecture = new ArchitectureSymbol(node.getName());

        addToScopeAndLinkWithNode(architecture, node);

        createPredefinedConstants();
        createPredefinedMethods();
    }

    public void endVisit(final ASTArchitecture node) {
        ArchitectureSymbol architecture = (ArchitectureSymbol) node.getSymbol().get();
        architecture.setBody((LayerSymbol) node.getBody().getSymbol().get());
        architecture.setInputs(inputs);
        architecture.setOutputs(outputs);

        removeCurrentScope();
    }

    private void createPredefinedConstants(){
        addToScope(PredefinedVariables.createTrueConstant());
        addToScope(PredefinedVariables.createFalseConstant());
    }

    private void createPredefinedMethods(){
        for (MethodDeclarationSymbol sym : PredefinedMethods.createList()){
            addToScope(sym);
        }
    }

    @Override
    public void visit(ASTIODeclaration ast) {
        IODeclarationSymbol iODeclaration = new IODeclarationSymbol(ast.getName());
        addToScopeAndLinkWithNode(iODeclaration, ast);
    }

    @Override
    public void endVisit(ASTIODeclaration ast) {
        IODeclarationSymbol iODeclaration = (IODeclarationSymbol) ast.getSymbol().get();
        if (ast.getArrayDeclaration().isPresent()){
            iODeclaration.setArrayLength(ast.getArrayDeclaration().get().getIntLiteral().getNumber().get().getDividend().intValue());
        }
        iODeclaration.setShape((ShapeSymbol) ast.getType().getShape().getSymbol().get());
        iODeclaration.setInput(ast.getIn().isPresent());
        iODeclaration.setType(ast.getType().getElementType());
        if (iODeclaration.isInput()){
            inputs.add(iODeclaration);
        }
        else {
            outputs.add(iODeclaration);
        }
    }

    @Override
    public void endVisit(ASTType node) {
        //todo
    }

    @Override
    public void visit(ASTShape ast) {
        ShapeSymbol sym = new ShapeSymbol();
        addToScopeAndLinkWithNode(sym, ast);
    }

    @Override
    public void endVisit(ASTShape node) {
        ShapeSymbol sym = (ShapeSymbol) node.getSymbol().get();
        if (node.getDimensions().size() == 1){
            sym.setChannels((ArchSimpleExpressionSymbol) node.getDimensions().get(0).getSymbol().get());
        }
        else if (node.getDimensions().size() == 3){
            sym.setHeight((ArchSimpleExpressionSymbol) node.getDimensions().get(ShapeSymbol.HEIGHT_INDEX - 1).getSymbol().get());
            sym.setWidth((ArchSimpleExpressionSymbol) node.getDimensions().get(ShapeSymbol.WIDTH_INDEX - 1).getSymbol().get());
            sym.setChannels((ArchSimpleExpressionSymbol) node.getDimensions().get(ShapeSymbol.CHANNEL_INDEX - 1).getSymbol().get());
        }
        else {
            //todo
            throw new IllegalStateException("todo: incorrect shape");
        }
        addToScopeAndLinkWithNode(sym, node);
    }

    @Override
    public void endVisit(ASTDimension node) {
        ArchSimpleExpressionSymbol sym;
        if (node.getIntLiteral().isPresent()){
            sym = ArchSimpleExpressionSymbol.of(node.getIntLiteral().get().getNumber().get().getDividend().intValue());
        }
        else {
            sym = ArchSimpleExpressionSymbol.of((VariableSymbol) node.getIOVariable().get().getSymbol().get());
        }
        addToScopeAndLinkWithNode(sym, node);
    }

    @Override
    public void endVisit(ASTIOVariable node) {
        ArchSimpleExpressionSymbol defaultValue = null;
        if (node.getIntRhs().isPresent()){
            defaultValue = ArchSimpleExpressionSymbol.of(node.getIntRhs().get().getNumber().get().getDividend().intValue());
        }
        VariableSymbol variable = new VariableSymbol.Builder()
                .name(node.getName())
                .type(VariableType.IOVARIABLE)
                .defaultValue(defaultValue)
                .constraints(Constraints.INTEGER, Constraints.POSITIVE)
                .build();
        //addToScope(ArchSimpleExpressionSymbol.of(variable));
        addToScopeAndLinkWithNode(variable, node);
    }

    @Override
    public void visit(ASTMethodDeclaration ast) {
        MethodDeclarationSymbol methodDeclaration = new MethodDeclarationSymbol(ast.getName());
        addToScopeAndLinkWithNode(methodDeclaration, ast);
    }

    @Override
    public void endVisit(ASTMethodDeclaration ast) {
        MethodDeclarationSymbol methodDeclaration = (MethodDeclarationSymbol) ast.getSymbol().get();
        methodDeclaration.setBody((CompositeLayerSymbol) ast.getBody().getSymbol().get());

        List<VariableSymbol> parameters = new ArrayList<>(4);
        for (ASTParameter astParam : ast.getParameters()){
            VariableSymbol parameter = (VariableSymbol) astParam.getSymbol().get();
            parameters.add(parameter);
        }
        methodDeclaration.setParameters(parameters);

        removeCurrentScope();
    }

    @Override
    public void visit(ASTParameter ast) {
        VariableSymbol variable = new VariableSymbol(ast.getName());
        variable.setType(VariableType.PARAMETER);
        addToScopeAndLinkWithNode(variable, ast);
    }

    @Override
    public void endVisit(ASTParameter ast) {
        VariableSymbol variable = (VariableSymbol) ast.getSymbol().get();
        if (ast.getDefault().isPresent()){
            variable.setDefaultExpression((ArchSimpleExpressionSymbol) ast.getDefault().get().getSymbol().get());
        }
    }

    @Override
    public void endVisit(ASTVariableAssignment node) {
        VariableSymbol variable = new VariableSymbol(node.getName());
        variable.setType(VariableType.CONSTANT);
        variable.setDefaultExpression((ArchSimpleExpressionSymbol) node.getRhs().getSymbol().get());
        addToScopeAndLinkWithNode(variable, node);
    }

    @Override
    public void endVisit(ASTMathExpression node) {
        boolean t = true;
        boolean b = t;
    }

    @Override
    public void visit(ASTArchSimpleExpression ast) {
        ArchSimpleExpressionSymbol sym = new ArchSimpleExpressionSymbol();
        addToScopeAndLinkWithNode(sym, ast);
    }

    @Override
    public void endVisit(ASTArchSimpleExpression ast) {
        MathExpressionSymbol mathExp;
        if (ast.getArithmeticExpression().isPresent()) {
            mathExp = (MathExpressionSymbol) ast.getArithmeticExpression().get().getSymbol().get();
        }
        else if (ast.getBooleanExpression().isPresent()) {
            mathExp = (MathExpressionSymbol) ast.getBooleanExpression().get().getSymbol().get();
        }
        else {
            mathExp = (MathExpressionSymbol) ast.getTupleExpression().get().getSymbol().get();
        }
        ArchSimpleExpressionSymbol sym = new ArchSimpleExpressionSymbol();
        sym.setMathExpression(mathExp);
        addToScopeAndLinkWithNode(sym, ast);
    }

    @Override
    public void endVisit(ASTArchExpression node) {
        if (node.getExpression().isPresent()){
            addToScopeAndLinkWithNode(node.getExpression().get().getSymbol().get(), node);
        }
        else {
            addToScopeAndLinkWithNode(node.getSequence().get().getSymbol().get(), node);
        }
    }

    @Override
    public void visit(ASTArchValueRange node) {
        ArchRangeExpressionSymbol sym = new ArchRangeExpressionSymbol();
        addToScopeAndLinkWithNode(sym, node);
    }

    @Override
    public void endVisit(ASTArchValueRange node) {
        ArchRangeExpressionSymbol sym = (ArchRangeExpressionSymbol) node.getSymbol().get();
        sym.setParallel(node.getParallel().isPresent());
        sym.setStartSymbol((ArchSimpleExpressionSymbol) node.getStart().getSymbol().get());
        sym.setEndSymbol((ArchSimpleExpressionSymbol) node.getEnd().getSymbol().get());
    }

    @Override
    public void visit(ASTArchParallelSequence node) {
        ArchSequenceExpressionSymbol sym = new ArchSequenceExpressionSymbol();
        addToScopeAndLinkWithNode(sym, node);
    }

    @Override
    public void endVisit(ASTArchParallelSequence node) {
        ArchSequenceExpressionSymbol sym = (ArchSequenceExpressionSymbol) node.getSymbol().get();

        List<List<ArchSimpleExpressionSymbol>> elements = new ArrayList<>();
        for (ASTArchSerialSequence serialSequenceAST : node.getParallelValues()) {
            List<ArchSimpleExpressionSymbol> serialElements = new ArrayList<>();
            for (ASTArchSimpleExpression astExpression : serialSequenceAST.getSerialValues()) {
                serialElements.add((ArchSimpleExpressionSymbol) astExpression.getSymbol().get());
            }
            elements.add(serialElements);
        }
        sym.setElements(elements);
    }

    @Override
    public void visit(ASTParallelLayer node) {
        CompositeLayerSymbol compositeLayer = new CompositeLayerSymbol();
        compositeLayer.setParallel(true);
        addToScopeAndLinkWithNode(compositeLayer, node);
    }

    @Override
    public void endVisit(ASTParallelLayer node) {
        CompositeLayerSymbol compositeLayer = (CompositeLayerSymbol) node.getSymbol().get();

        List<LayerSymbol> layers = new ArrayList<>();
        for (ASTArchBody astBody : node.getGroups()){
            layers.add((CompositeLayerSymbol) astBody.getSymbol().get());
        }
        compositeLayer.setLayers(layers);

        removeCurrentScope();
    }

    @Override
    public void visit(ASTArchBody ast) {
        CompositeLayerSymbol compositeLayer = new CompositeLayerSymbol();
        compositeLayer.setParallel(false);
        addToScopeAndLinkWithNode(compositeLayer, ast);
    }

    @Override
    public void endVisit(ASTArchBody ast) {
        CompositeLayerSymbol compositeLayer = (CompositeLayerSymbol) ast.getSymbol().get();

        List<LayerSymbol> layers = new ArrayList<>();
        for (ASTArchitectureElement astElement : ast.getElements()){
            layers.add((LayerSymbol) astElement.getSymbol().get());
        }
        compositeLayer.setLayers(layers);

        removeCurrentScope();
    }

    @Override
    public void visit(ASTMethodLayer ast) {
        MethodLayerSymbol methodLayer = new MethodLayerSymbol(ast.getName());
        addToScopeAndLinkWithNode(methodLayer, ast);
    }

    @Override
    public void endVisit(ASTMethodLayer ast) {
        MethodLayerSymbol methodLayer = (MethodLayerSymbol) ast.getSymbol().get();

        List<ArgumentSymbol> arguments = new ArrayList<>(6);
        for (ASTArgument astArgument : ast.getArguments()){
            Optional<ArgumentSymbol> optArgument = astArgument.getSymbol().map(e -> (ArgumentSymbol)e);
            optArgument.ifPresent(arguments::add);
        }
        methodLayer.setArguments(arguments);

        removeCurrentScope();
    }

    @Override
    public void endVisit(ASTArgument node) {
        ArchExpressionSymbol value;
        value = (ArchExpressionSymbol) node.getRhs().getSymbol().get();

        ArgumentSymbol argument = new ArgumentSymbol(node.getName());
        argument.setRhs(value);
        addToScopeAndLinkWithNode(argument, node);
    }

    @Override
    public void visit(ASTIOLayer node) {
        Optional<IODeclarationSymbol> optIODef = currentScope().get().resolve(node.getName(), IODeclarationSymbol.KIND);
        int arrayLength = 1;
        if (optIODef.isPresent()){
            arrayLength = optIODef.get().getArrayLength();
        }

        if (!node.getIndex().isPresent() && arrayLength > 1){
            List<LayerSymbol> ioLayers = new ArrayList<>(arrayLength);
            IOLayerSymbol ioLayer;
            for (int i = 0; i < arrayLength; i++){
                ioLayer = new IOLayerSymbol(node.getName());
                ioLayer.setArrayAccess(i);
                ioLayers.add(ioLayer);
            }

            CompositeLayerSymbol composite = new CompositeLayerSymbol.Builder()
                    .parallel(true)
                    .layers(ioLayers)
                    .build();

            addToScopeAndLinkWithNode(composite, node);

            for (LayerSymbol layer : ioLayers){
                addToScope(layer);
                layer.setAstNode(node);
            }
        }
        else {
            IOLayerSymbol ioLayer = new IOLayerSymbol(node.getName());
            addToScopeAndLinkWithNode(ioLayer, node);
        }
    }

    @Override
    public void endVisit(ASTIOLayer node) {
        if (node.getIndex().isPresent()){
            IOLayerSymbol sym = (IOLayerSymbol) node.getSymbol().get();
            sym.setArrayAccess((ArchSimpleExpressionSymbol) node.getIndex().get().getSymbol().get());
        }
        removeCurrentScope();
    }

    @Override
    public void visit(ASTArrayAccessLayer node) {
        MethodLayerSymbol methodLayer = new MethodLayerSymbol(PredefinedMethods.GET_NAME);
        addToScopeAndLinkWithNode(methodLayer, node);
    }

    @Override
    public void endVisit(ASTArrayAccessLayer node) {
        MethodLayerSymbol methodLayer = (MethodLayerSymbol) node.getSymbol().get();
        ArgumentSymbol indexArgument = new ArgumentSymbol.Builder()
                .parameter(methodLayer.getMethod().getParameter("index").get())
                .value((ArchSimpleExpressionSymbol) node.getIndex().getSymbol().get())
                .build();
        addToScope(indexArgument);
        methodLayer.setArguments(Collections.singletonList(indexArgument));

        removeCurrentScope();
    }

    @Override
    public void endVisit(ASTTupleExpression node) {
        TupleExpressionSymbol symbol = new TupleExpressionSymbol();

        for (ASTMathExpression expression : node.getExpressions()){
            symbol.add((MathExpressionSymbol)expression.getSymbol().get());
        }

        addToScopeAndLinkWithNode(symbol, node);
    }
}