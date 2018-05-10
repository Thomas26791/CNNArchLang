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

import de.monticore.lang.monticar.cnnarch.predefined.AllPredefinedVariables;
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
        if (parameter == null){
            if (getLayer().getDeclaration() != null){
                Optional<VariableSymbol> optParam = getLayer().getDeclaration().getParameter(getName());
                optParam.ifPresent(this::setParameter);
            }
        }
        return parameter;
    }

    protected void setParameter(VariableSymbol parameter) {
        this.parameter = parameter;
    }

    public LayerSymbol getLayer() {
        return (LayerSymbol) getEnclosingScope().getSpanningSymbol().get();
    }

    public ArchExpressionSymbol getRhs() {
        return rhs;
    }

    protected void setRhs(ArchExpressionSymbol rhs) {
        if (getName().equals(AllPredefinedVariables.SERIAL_ARG_NAME)
                && rhs instanceof ArchSimpleExpressionSymbol
                && (!rhs.getValue().isPresent() || !rhs.getValue().get().equals(1))){
            this.rhs = ArchRangeExpressionSymbol.of(
                    ArchSimpleExpressionSymbol.of(1),
                    (ArchSimpleExpressionSymbol) rhs,
                    false);
        }
        else if (getName().equals(AllPredefinedVariables.PARALLEL_ARG_NAME)
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
        if (getRhs().isResolved() && getRhs().isSimpleValue()){
            getParameter().setExpression((ArchSimpleExpressionSymbol) getRhs());
        }
        else {
            throw new IllegalStateException("The value of the parameter is set to a sequence or the expression is not resolved. This should never happen.");
        }
    }

    public void resolveExpression() throws ArchResolveException {
        getRhs().resolveOrError();
        boolean valid = Constraints.check(this);
        if (!valid){
            throw new ArchResolveException();
        }
    }

    public void checkConstraints(){
        Constraints.check(this);
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
                if (getName().equals(AllPredefinedVariables.SERIAL_ARG_NAME) || getName().equals(AllPredefinedVariables.PARALLEL_ARG_NAME)){
                    value = ArchSimpleExpressionSymbol.of(1);
                }

                ArgumentSymbol argument = new Builder().parameter(getParameter()).value(value).build();
                if (getAstNode().isPresent()){
                    argument.setAstNode(getAstNode().get());
                }
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

    protected ArgumentSymbol preResolveDeepCopy(){
        ArgumentSymbol copy = new ArgumentSymbol(getName());
        if (getAstNode().isPresent()){
            copy.setAstNode(getAstNode().get());
        }

        copy.setRhs(getRhs().preResolveDeepCopy());
        return copy;
    }


    public static class Builder{
        private String name;
        private VariableSymbol parameter;
        private ArchExpressionSymbol value;

        public Builder parameter(VariableSymbol parameter) {
            this.parameter = parameter;
            return this;
        }

        public Builder parameter(String name) {
            this.name = name;
            return this;
        }

        public Builder value(ArchExpressionSymbol value) {
            this.value = value;
            return this;
        }

        public ArgumentSymbol build(){
            if (parameter == null && name == null){
                throw new IllegalStateException("Missing parameter for ArgumentSymbol");
            }
            if (parameter == null){
                ArgumentSymbol sym = new ArgumentSymbol(name);
                sym.setRhs(value);
                return sym;
            }
            else {
                ArgumentSymbol sym = new ArgumentSymbol(parameter.getName());
                sym.setParameter(parameter);
                sym.setRhs(value);
                return sym;
            }
        }
    }
}
