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

import de.monticore.lang.monticar.cnnarch.Constraint;
import de.monticore.lang.monticar.cnnarch.PredefinedVariables;
import de.monticore.symboltable.CommonSymbol;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Symbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ArgumentSymbol extends CommonSymbol {

    public static final ArgumentKind KIND = new ArgumentKind();

    private VariableSymbol parameter;
    private ArchExpressionSymbol rhs;

    protected ArgumentSymbol(String name) {
        super(name, KIND);
    }

    public VariableSymbol getParameter() {
        return parameter;
    }

    protected void setParameter(VariableSymbol parameter) {
        this.parameter = parameter;
    }

    public ArchExpressionSymbol getRhs() {
        return rhs;
    }

    public Optional<Object> getValue(){
        return getRhs().getValue();
    }

    protected void setRhs(ArchExpressionSymbol rhs) {
        if (getName().equals(PredefinedVariables.FOR_NAME)
                && rhs instanceof ArchSimpleExpressionSymbol
                && (!rhs.getValue().isPresent() || !rhs.getValue().get().equals(1))){
            this.rhs = ArchRangeExpressionSymbol.of(
                    ArchSimpleExpressionSymbol.of(1),
                    (ArchSimpleExpressionSymbol) rhs,
                    false);
        }
        else if (getName().equals(PredefinedVariables.CARDINALITY_NAME)
                && rhs instanceof ArchSimpleExpressionSymbol
                && (!rhs.getValue().isPresent() || !rhs.getValue().get().equals(1))) {
            this.rhs = ArchRangeExpressionSymbol.of(
                    ArchSimpleExpressionSymbol.of(1),
                    (ArchSimpleExpressionSymbol) rhs,
                    true);
        }
        else {
            this.rhs = rhs;
        }
    }

    //do not call if value is a sequence
    public void set(){
        if (getRhs().isSimpleValue()){
            getRhs().resolveOrError();
            Constraint.check(this);
            getParameter().setExpression((ArchSimpleExpressionSymbol) getRhs());
        }
        else {
            throw new IllegalStateException("The value of the parameter is set to a sequence. This should never happen.");
        }
    }

    public List<List<ArgumentSymbol>> split(){
        if (getRhs().isRange()){
            getRhs().resolveOrError();
        }
        List<List<ArchSimpleExpressionSymbol>> elements = getRhs().getElements().get();
        List<List<ArgumentSymbol>> arguments = new ArrayList<>(elements.size());

        for (List<ArchSimpleExpressionSymbol> serialElementList : elements){
            List<ArgumentSymbol> serialArgumentList = new ArrayList<>(serialElementList.size());
            for (ArchSimpleExpressionSymbol element : serialElementList){
                ArchSimpleExpressionSymbol value = element;
                if (getName().equals(PredefinedVariables.FOR_NAME) || getName().equals(PredefinedVariables.CARDINALITY_NAME)){
                    value = ArchSimpleExpressionSymbol.of(1);
                }

                ArgumentSymbol argument = new Builder().parameter(getParameter()).value(value).build();
                serialArgumentList.add(argument);
            }
            arguments.add(serialArgumentList);
        }
        return arguments;
    }

    public Optional<List<List<ArgumentSymbol>>> expandedSplit(int parallelLength, List<Integer> serialLengths){
        List<List<ArgumentSymbol>> splitArguments = split();

        boolean valid = splitArguments.size() == parallelLength || splitArguments.size() == 1;
        int k = 0;
        for (List<ArgumentSymbol> serialArgumentList : splitArguments){
            if (serialArgumentList.size() != serialLengths.get(k) && serialArgumentList.size() != 1){
                valid = false;
            }
            k++;
        }

        if (valid){
            List<List<ArgumentSymbol>> expandedArguments = new ArrayList<>(parallelLength);

            for (int i = 0; i < parallelLength; i++){
                List<ArgumentSymbol> expandedSerialArgumentList = new ArrayList<>(serialLengths.get(i));
                List<ArgumentSymbol> serialArgumentList = splitArguments.size() != 1
                        ? splitArguments.get(i)
                        : splitArguments.get(0);

                for (int j = 0; j < serialLengths.get(i); j++){
                    ArgumentSymbol argument = serialArgumentList.size() != 1
                            ? serialArgumentList.get(j)
                            : serialArgumentList.get(0);
                    expandedSerialArgumentList.add(argument);
                }
                expandedArguments.add(expandedSerialArgumentList);
            }
            return Optional.of(expandedArguments);
        }
        else {
            return Optional.empty();
        }
    }

    protected void putInScope(MutableScope scope){
        Collection<Symbol> symbolsInScope = scope.getLocalSymbols().get(getName());
        if (symbolsInScope == null || !symbolsInScope.contains(this)) {
            scope.add(this);
            getRhs().putInScope(scope);
        }
    }

    public ArgumentSymbol copy(){
        ArgumentSymbol copy = new Builder()
                .parameter(getParameter())
                .value(getRhs().copy())
                .build();
        if (getAstNode().isPresent()){
            copy.setAstNode(getAstNode().get());
        }
        return copy;
    }


    public static class Builder{
        private VariableSymbol parameter;
        private ArchExpressionSymbol value;

        public Builder parameter(VariableSymbol parameter) {
            this.parameter = parameter;
            return this;
        }

        public Builder value(ArchExpressionSymbol value) {
            this.value = value;
            return this;
        }

        public ArgumentSymbol build(){
            if (parameter == null){
                throw new IllegalStateException("Missing parameter for ArgumentSymbol");
            }
            ArgumentSymbol sym = new ArgumentSymbol(parameter.getName());
            sym.setParameter(parameter);
            sym.setRhs(value);
            return sym;
        }
    }
}
