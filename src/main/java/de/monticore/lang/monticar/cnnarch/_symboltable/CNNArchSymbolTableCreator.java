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
import de.monticore.lang.monticar.cnnarch.PredefinedMethods;
import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.cnnarch._visitor.CNNArchVisitor;
import de.monticore.lang.monticar.cnnarch._visitor.CommonCNNArchDelegatorVisitor;
import de.monticore.symboltable.*;
import de.se_rwth.commons.logging.Log;

import java.util.*;

public class CNNArchSymbolTableCreator extends de.monticore.symboltable.CommonSymbolTableCreator
        implements CNNArchVisitor {

    private String compilationUnitPackage = "";

    private MathSymbolTableCreator mathSTC;


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
        this.mathSTC = new MathSymbolTableCreator(resolvingConfig, scopeStack);
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

    public void visit(final ASTArchitecture architecture) {
        ArchitectureSymbol architectureSymbol = new ArchitectureSymbol(
                architecture.getName()
        );

        addToScopeAndLinkWithNode(architectureSymbol, architecture);

        createPredefinedConstants();
        createPredefinedMethods();
    }

    public void endVisit(final ASTArchitecture architecture) {
        removeCurrentScope();
    }

    private void createPredefinedConstants(){
        VariableSymbol trueConstant = new VariableSymbol.Builder()
                .name("true")
                .type(VariableType.CONSTANT)
                .defaultValue(true)
                .build();
        VariableSymbol falseConstant = new VariableSymbol.Builder()
                .name("false")
                .type(VariableType.CONSTANT)
                .defaultValue(false)
                .build();

        addToScope(trueConstant);
        addToScope(falseConstant);
    }

    private void createPredefinedMethods(){
        for (MethodDeclarationSymbol method : PredefinedMethods.createList()){
            addToScope(method);
            putSpannedScopeOnStack(method);
            for (VariableSymbol parameter : method.getParameters()){
                addToScope(parameter);
            }
            removeCurrentScope();
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
        //todo set
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
            variable.setDefaultValueSymbol((ArchSimpleExpressionSymbol) ast.getDefault().get().getSymbol().get());
        }
    }

    @Override
    public void visit(ASTConstant node) {
        VariableSymbol constant = new VariableSymbol(node.getName());
        constant.setType(VariableType.CONSTANT);
        addToScopeAndLinkWithNode(constant, node);
    }

    @Override
    public void endVisit(ASTConstant node) {
        VariableSymbol constant = (VariableSymbol) node.getSymbol().get();
        constant.setDefaultValueSymbol((ArchSimpleExpressionSymbol) node.getRhs().getSymbol().get());
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
        addToScopeAndLinkWithNode(sym, ast);
        sym.setExpression(mathExp);
        sym.checkIfResolved();
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
        sym.checkIfResolved();
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
        sym.checkIfResolved();
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
            if (astArgument.getName().equals("_if")){
                methodLayer.setIfArgument((ArchExpressionSymbol) astArgument.getRhs().getSymbol().get());
            }
            else if (astArgument.getName().equals("_for")){
                methodLayer.setForArgument((ArchExpressionSymbol) astArgument.getRhs().getSymbol().get());
            }
            else {
                arguments.add((ArgumentSymbol) astArgument.getSymbol().get());
            }
        }
        methodLayer.setArguments(arguments);
        methodLayer.getMethod();

        removeCurrentScope();
    }

    @Override
    public void visit(ASTArgument node) {
        if (!node.getName().equals("_if") && !node.getName().equals("_for")){
            ArgumentSymbol argument = new ArgumentSymbol(node.getName());
            addToScopeAndLinkWithNode(argument, node);
        }
    }

    @Override
    public void endVisit(ASTArgument node) {
        if (!node.getName().equals("_if") && !node.getName().equals("_for")){
            MethodLayerSymbol methodLayer = (MethodLayerSymbol) currentScope().get().getSpanningSymbol().get();
            ArchExpressionSymbol value = (ArchExpressionSymbol) node.getRhs().getSymbol().get();
            ArgumentSymbol argument = (ArgumentSymbol) node.getSymbol().get();
            argument.setRhs(value);

            VariableSymbol parameter = (VariableSymbol) methodLayer.getMethod().getSpannedScope()
                    .resolveLocally(argument.getName(), VariableSymbol.KIND).get();

            argument.setParameter(parameter);
        }
    }

    @Override
    public void visit(ASTIOLayer node) {
        IOLayerSymbol sym = new IOLayerSymbol(node.getName());
        addToScopeAndLinkWithNode(sym, node);
    }

    @Override
    public void endVisit(ASTIOLayer node) {
        IOLayerSymbol sym = (IOLayerSymbol) node.getSymbol().get();
        if (node.getIndex().isPresent()){
            sym.setArrayAccess((ArchSimpleExpressionSymbol) node.getIndex().get().getSymbol().get());
        }
    }

    @Override
    public void visit(ASTArrayAccessLayer node) {
        MethodLayerSymbol methodLayer = new MethodLayerSymbol(PredefinedMethods.createGet().getName());
        addToScopeAndLinkWithNode(methodLayer, node);
    }

    @Override
    public void endVisit(ASTArrayAccessLayer node) {
        MethodLayerSymbol methodLayer = (MethodLayerSymbol) currentScope().get().getSpanningSymbol().get();
        methodLayer.setArguments(Arrays.asList(new ArgumentSymbol.Builder()
                .name("index")
                .value((ArchSimpleExpressionSymbol) node.getIndex().getSymbol().get())
                .build()));
        methodLayer.getMethod();
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