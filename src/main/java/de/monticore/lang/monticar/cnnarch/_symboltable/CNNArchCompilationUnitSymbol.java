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

import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.monticore.lang.monticar.cnnarch.helper.Utils;
import de.se_rwth.commons.logging.Log;
import org.jscience.mathematics.number.Rational;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CNNArchCompilationUnitSymbol extends CNNArchCompilationUnitSymbolTOP{

    private List<VariableSymbol> parameters;
    private List<IODeclarationSymbol> ioDeclarations;
    private ArchitectureSymbol architecture;

    public CNNArchCompilationUnitSymbol(String name) {
        super(name);
    }

    public ArchitectureSymbol getArchitecture() {
        return architecture;
    }

    public void setArchitecture(ArchitectureSymbol architecture) {
        this.architecture = architecture;
    }

    public List<IODeclarationSymbol> getIoDeclarations() {
        return ioDeclarations;
    }

    public void setIoDeclarations(List<IODeclarationSymbol> ioDeclarations) {
        this.ioDeclarations = ioDeclarations;
    }

    public List<VariableSymbol> getParameters() {
        return parameters;
    }

    public void setParameters(List<VariableSymbol> parameters) {
        this.parameters = parameters;
    }

    public ArchitectureSymbol resolve(){
        checkParameters();
        getArchitecture().resolve();
        return getArchitecture();
    }


    public void checkParameters(){
        for (VariableSymbol parameter : getParameters()){
            if (!parameter.hasExpression()){
                Log.error("0" + ErrorCodes.MISSING_VAR_VALUE + " Missing architecture argument. " +
                        "The parameter '" + parameter.getName() + "' has no value.");
            }
        }
    }

    public Optional<VariableSymbol> getParameter(String name){
        for (VariableSymbol parameter : getParameters()){
            if (parameter.getName().equals(name)){
                return Optional.of(parameter);
            }
        }
        return Optional.empty();
    }

    private VariableSymbol getParameterOrError(String name){
        Optional<VariableSymbol> param = getParameter(name);
        if (param.isPresent()){
            return param.get();
        }
        else {
            throw new IllegalArgumentException("architecture parameter with name " + name + " does not exist.");
        }
    }

    public void setParameter(String name, Rational value){
        VariableSymbol parameter = getParameterOrError(name);
        if (value.getDivisor().intValue() == 1){
            parameter.setExpression(ArchSimpleExpressionSymbol.of(value.getDividend().intValue()));
        }
        else {
            parameter.setExpression(ArchSimpleExpressionSymbol.of(value.doubleValue()));
        }
    }

    public void setParameter(String name, boolean value){
        VariableSymbol parameter = getParameterOrError(name);
        parameter.setExpression(ArchSimpleExpressionSymbol.of(value));
    }

    public void setParameter(String name, int value){
        VariableSymbol parameter = getParameterOrError(name);
        parameter.setExpression(ArchSimpleExpressionSymbol.of(value));
    }

    public void setParameter(String name, double value){
        VariableSymbol parameter = getParameterOrError(name);
        parameter.setExpression(ArchSimpleExpressionSymbol.of(value));
    }

    public void setParameter(String name, String value){
        VariableSymbol parameter = getParameterOrError(name);
        parameter.setExpression(ArchSimpleExpressionSymbol.of(value));
    }


    public CNNArchCompilationUnitSymbol preResolveDeepCopy(){
        return preResolveDeepCopy(getName());
    }

    public CNNArchCompilationUnitSymbol preResolveDeepCopy(String newName){
        CNNArchCompilationUnitSymbol copy = new CNNArchCompilationUnitSymbol(newName);
        if (getAstNode().isPresent()){
            copy.setAstNode(getAstNode().get());
        }

        List<VariableSymbol> parameterCopies = new ArrayList<>(getParameters().size());
        for (VariableSymbol param : getParameters()){
            VariableSymbol paramCopy = param.deepCopy();
            parameterCopies.add(paramCopy);
            paramCopy.putInScope(copy.getSpannedScope());
        }
        copy.setParameters(parameterCopies);

        List<IODeclarationSymbol> ioCopies = new ArrayList<>(getIoDeclarations().size());
        for (IODeclarationSymbol ioDeclaration : getIoDeclarations()){
            IODeclarationSymbol ioCopy = ioDeclaration.preResolveDeepCopy();
            ioCopies.add(ioCopy);
            ioCopy.putInScope(copy.getSpannedScope());
        }
        copy.setIoDeclarations(ioCopies);

        ArchitectureSymbol architecture = getArchitecture().preResolveDeepCopy(copy.getSpannedScope());
        copy.setArchitecture(architecture);
        Utils.recursiveSetResolvingFilters(copy.getSpannedScope(), getSpannedScope().getResolvingFilters());
        return copy;
    }
}
