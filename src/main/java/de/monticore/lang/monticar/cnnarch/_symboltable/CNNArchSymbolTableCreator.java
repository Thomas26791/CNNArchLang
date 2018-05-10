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
import de.monticore.lang.math.math._symboltable.MathSymbolTableCreator;
import de.monticore.lang.math.math._symboltable.expression.MathExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.cnnarch._visitor.CNNArchInheritanceVisitor;
import de.monticore.lang.monticar.cnnarch._visitor.CNNArchVisitor;
import de.monticore.lang.monticar.cnnarch._visitor.CommonCNNArchDelegatorVisitor;
import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedLayers;
import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedVariables;
import de.monticore.symboltable.*;
import de.se_rwth.commons.logging.Log;

import java.util.*;

public class CNNArchSymbolTableCreator extends de.monticore.symboltable.CommonSymbolTableCreator
        implements CNNArchInheritanceVisitor {

    private String compilationUnitPackage = "";

    private MathSymbolTableCreator mathSTC;
    private ArchitectureSymbol architecture;


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

    public MathSymbolTableCreator getMathSTC() {
        return mathSTC;
    }

    @Override
    public void visit(final ASTCNNArchCompilationUnit compilationUnit) {
        Log.debug("Building Symboltable for Script: " + compilationUnit.getName(),
                CNNArchSymbolTableCreator.class.getSimpleName());

        List<ImportStatement> imports = new ArrayList<>();

        ArtifactScope artifactScope = new ArtifactScope(
                Optional.empty(),
                compilationUnitPackage,
                imports);

        putOnStack(artifactScope);

        CNNArchCompilationUnitSymbol compilationUnitSymbol = new CNNArchCompilationUnitSymbol(compilationUnit.getName());
        addToScopeAndLinkWithNode(compilationUnitSymbol, compilationUnit);
    }

    @Override
    public void endVisit(ASTCNNArchCompilationUnit ast) {
        CNNArchCompilationUnitSymbol compilationUnitSymbol = (CNNArchCompilationUnitSymbol) ast.getSymbol().get();
        compilationUnitSymbol.setArchitecture((ArchitectureSymbol) ast.getArchitecture().getSymbol().get());

        List<VariableSymbol> parameters = new ArrayList<>(ast.getArchitectureParameters().size());
        for (ASTArchitectureParameter astParameter : ast.getArchitectureParameters()){
            parameters.add((VariableSymbol) astParameter.getSymbol().get());
        }
        compilationUnitSymbol.setParameters(parameters);

        List<IODeclarationSymbol> ioDeclarations = new ArrayList<>();
        for (ASTIODeclaration astIODeclaration : ast.getIoDeclarations()){
            ioDeclarations.add((IODeclarationSymbol) astIODeclaration.getSymbol().get());
        }
        compilationUnitSymbol.setIoDeclarations(ioDeclarations);

        setEnclosingScopeOfNodes(ast);
    }

    public void visit(final ASTArchitecture node) {
        architecture = new ArchitectureSymbol();

        addToScopeAndLinkWithNode(architecture, node);

        createPredefinedConstants();
        createPredefinedLayers();
    }

    public void endVisit(final ASTArchitecture node) {
        //ArchitectureSymbol architecture = (ArchitectureSymbol) node.getSymbol().get();
        architecture.setBody((ArchitectureElementSymbol) node.getBody().getSymbol().get());

        removeCurrentScope();
    }

    private void createPredefinedConstants(){
        addToScope(AllPredefinedVariables.createTrueConstant());
        addToScope(AllPredefinedVariables.createFalseConstant());
    }

    private void createPredefinedLayers(){
        for (LayerDeclarationSymbol sym : AllPredefinedLayers.createList()){
            addToScope(sym);
        }
    }

    @Override
    public void endVisit(ASTArchitectureParameter node) {
        VariableSymbol variable = new VariableSymbol(node.getName());
        variable.setType(VariableType.ARCHITECTURE_PARAMETER);
        if (node.getDefault().isPresent()){
            variable.setDefaultExpression((ArchSimpleExpressionSymbol) node.getDefault().get().getSymbol().get());
        }

        addToScopeAndLinkWithNode(variable, node);
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
        iODeclaration.setInput(ast.getIn().isPresent());
        iODeclaration.setType((ArchTypeSymbol) ast.getType().getSymbol().get());
    }

    @Override
    public void visit(ASTArchType ast) {
        ArchTypeSymbol sym = new ArchTypeSymbol();
        addToScopeAndLinkWithNode(sym, ast);
    }

    @Override
    public void endVisit(ASTArchType node) {
        ArchTypeSymbol sym = (ArchTypeSymbol) node.getSymbol().get();
        List<ASTArchSimpleExpression> astDimensions = node.getShape().getDimensions();

        if (astDimensions.size() >= 1){
            sym.setChannelIndex(0);
        }
        if (astDimensions.size() >= 2){
            sym.setHeightIndex(1);
        }
        if (astDimensions.size() >= 3){
            sym.setWidthIndex(2);
        }
        List<ArchSimpleExpressionSymbol> dimensionList = new ArrayList<>(3);
        for (ASTArchSimpleExpression astExp : astDimensions){
            dimensionList.add((ArchSimpleExpressionSymbol) astExp.getSymbol().get());
        }
        sym.setDimensionSymbols(dimensionList);
        sym.setDomain(node.getElementType());
    }

    @Override
    public void visit(ASTLayerDeclaration ast) {
        LayerDeclarationSymbol layerDeclaration = new LayerDeclarationSymbol(ast.getName());
        addToScopeAndLinkWithNode(layerDeclaration, ast);
    }

    @Override
    public void endVisit(ASTLayerDeclaration ast) {
        LayerDeclarationSymbol layerDeclaration = (LayerDeclarationSymbol) ast.getSymbol().get();
        layerDeclaration.setBody((CompositeElementSymbol) ast.getBody().getSymbol().get());

        List<VariableSymbol> parameters = new ArrayList<>(4);
        for (ASTLayerParameter astParam : ast.getParameters()){
            VariableSymbol parameter = (VariableSymbol) astParam.getSymbol().get();
            parameters.add(parameter);
        }
        layerDeclaration.setParameters(parameters);

        removeCurrentScope();
    }

    @Override
    public void visit(ASTLayerParameter ast) {
        VariableSymbol variable = new VariableSymbol(ast.getName());
        variable.setType(VariableType.LAYER_PARAMETER);
        addToScopeAndLinkWithNode(variable, ast);
    }

    @Override
    public void endVisit(ASTLayerParameter ast) {
        VariableSymbol variable = (VariableSymbol) ast.getSymbol().get();
        if (ast.getDefault().isPresent()){
            variable.setDefaultExpression((ArchSimpleExpressionSymbol) ast.getDefault().get().getSymbol().get());
        }
    }

    @Override
    public void endVisit(ASTArchSimpleExpression ast) {
        ArchSimpleExpressionSymbol sym = new ArchSimpleExpressionSymbol();
        MathExpressionSymbol mathExp = null;
        if (ast.getArithmeticExpression().isPresent()) {
            mathExp = (MathExpressionSymbol) ast.getArithmeticExpression().get().getSymbol().get();
        }
        else if (ast.getBooleanExpression().isPresent()) {
            mathExp = (MathExpressionSymbol) ast.getBooleanExpression().get().getSymbol().get();
        }
        else if (ast.getTupleExpression().isPresent()){
            mathExp = (MathExpressionSymbol) ast.getTupleExpression().get().getSymbol().get();
        }
        else{
            sym.setValue(ast.getString().get().getValue());
        }
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
    public void visit(ASTParallelBlock node) {
        CompositeElementSymbol compositeElement = new CompositeElementSymbol();
        compositeElement.setParallel(true);
        addToScopeAndLinkWithNode(compositeElement, node);
    }

    @Override
    public void endVisit(ASTParallelBlock node) {
        CompositeElementSymbol compositeElement = (CompositeElementSymbol) node.getSymbol().get();

        List<ArchitectureElementSymbol> elements = new ArrayList<>();
        for (ASTArchBody astBody : node.getGroups()){
            elements.add((CompositeElementSymbol) astBody.getSymbol().get());
        }
        compositeElement.setElements(elements);

        removeCurrentScope();
    }

    @Override
    public void visit(ASTArchBody ast) {
        CompositeElementSymbol compositeElement = new CompositeElementSymbol();
        compositeElement.setParallel(false);
        addToScopeAndLinkWithNode(compositeElement, ast);
    }

    @Override
    public void endVisit(ASTArchBody ast) {
        CompositeElementSymbol compositeElement = (CompositeElementSymbol) ast.getSymbol().get();

        List<ArchitectureElementSymbol> elements = new ArrayList<>();
        for (ASTArchitectureElement astElement : ast.getElements()){
            elements.add((ArchitectureElementSymbol) astElement.getSymbol().get());
        }
        compositeElement.setElements(elements);

        removeCurrentScope();
    }

    @Override
    public void visit(ASTLayer ast) {
        LayerSymbol layer = new LayerSymbol(ast.getName());
        addToScopeAndLinkWithNode(layer, ast);
    }

    @Override
    public void endVisit(ASTLayer ast) {
        LayerSymbol layer = (LayerSymbol) ast.getSymbol().get();

        List<ArgumentSymbol> arguments = new ArrayList<>(6);
        for (ASTArchArgument astArgument : ast.getArguments()){
            Optional<ArgumentSymbol> optArgument = astArgument.getSymbol().map(e -> (ArgumentSymbol)e);
            optArgument.ifPresent(arguments::add);
        }
        layer.setArguments(arguments);

        removeCurrentScope();
    }

    @Override
    public void endVisit(ASTArchArgument node) {
        ArchExpressionSymbol value;
        value = (ArchExpressionSymbol) node.getRhs().getSymbol().get();

        ArgumentSymbol argument = new ArgumentSymbol(node.getName());
        argument.setRhs(value);
        addToScopeAndLinkWithNode(argument, node);
    }

    public void visit(ASTIOElement node) {
        IOSymbol ioElement = new IOSymbol(node.getName());
        addToScopeAndLinkWithNode(ioElement, node);
    }

    @Override
    public void endVisit(ASTIOElement node) {
        IOSymbol sym = (IOSymbol) node.getSymbol().get();
        if (node.getIndex().isPresent()){
            sym.setArrayAccess((ArchSimpleExpressionSymbol) node.getIndex().get().getSymbol().get());
        }
        removeCurrentScope();
    }

    @Override
    public void visit(ASTArrayAccessLayer node) {
        LayerSymbol layer = new LayerSymbol(AllPredefinedLayers.GET_NAME);
        addToScopeAndLinkWithNode(layer, node);
    }

    @Override
    public void endVisit(ASTArrayAccessLayer node) {
        LayerSymbol layer = (LayerSymbol) node.getSymbol().get();
        ArgumentSymbol indexArgument = new ArgumentSymbol.Builder()
                .parameter(layer.getDeclaration().getParameter(AllPredefinedLayers.INDEX_NAME).get())
                .value((ArchSimpleExpressionSymbol) node.getIndex().getSymbol().get())
                .build();
        indexArgument.setAstNode(node.getIndex());
        addToScope(indexArgument);
        layer.setArguments(Collections.singletonList(indexArgument));

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