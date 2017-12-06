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


import de.monticore.lang.monticar.cnnarch._ast.ASTArchitecture;
import de.monticore.lang.monticar.cnnarch._ast.ASTCNNArchCompilationUnit;
import de.monticore.lang.monticar.cnnarch._ast.ASTOutputDef;
import de.monticore.lang.monticar.cnnarch._ast.ASTOutputStructure;
import de.monticore.lang.monticar.cnnarch._cocos.ArchitectureCheck;
import de.monticore.symboltable.*;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class CNNArchSymbolTableCreator extends CNNArchSymbolTableCreatorTOP {

    private String compilationUnitPackage = "";


    public CNNArchSymbolTableCreator(final ResolvingConfiguration resolvingConfig,
                                     final MutableScope enclosingScope) {
        super(resolvingConfig, enclosingScope);
    }

    public CNNArchSymbolTableCreator(final ResolvingConfiguration resolvingConfig,
                                     final Deque<MutableScope> scopeStack) {
        super(resolvingConfig, scopeStack);
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

        CNNArchCompilationUnitSymbol compilationUnitSymbol = new CNNArchCompilationUnitSymbol(
                compilationUnit.getName()
        );

        addToScopeAndLinkWithNode(compilationUnitSymbol, compilationUnit);

    }

    /*@Override
    public void visit(ASTOutputDef ast) {
        Log.debug("Building Symboltable for Script: " + ast.getName(),
                CNNArchSymbolTableCreator.class.getSimpleName());

        OutputSymbol outputSymbol = new OutputSymbol(ast.getName());
        addToScope(outputSymbol);
    }

    @Override
    public void visit(ASTOutputStructure ast) {
        Log.debug("Linking symbol " + ast.getOutput() + " to output: ",
                CNNArchSymbolTableCreator.class.getSimpleName());

        Optional<Symbol> optSymbol = compilationUnitScope.resolveDown(ast.getOutput(), OutputKind.KIND);

        if (optSymbol.isPresent()){
            OutputSymbol outSym = (OutputSymbol) optSymbol.get();
            addToScopeAndLinkWithNode(outSym, ast);
        }
        else {
            Log.error("0"+OUTPUT_UNDEFINED_CODE+" Output symbol with name "+ ast.getOutput() +" does not exist."
                    , ast.get_SourcePositionEnd());
        }
    }*/

    public void endVisit(final ASTArchitecture architecture) {
        removeCurrentScope();
    }
    
}
