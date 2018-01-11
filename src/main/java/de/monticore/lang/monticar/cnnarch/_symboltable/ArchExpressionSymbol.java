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

import de.monticore.symboltable.CommonSymbol;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Symbol;

import java.util.*;

abstract public class ArchExpressionSymbol extends CommonSymbol {

    public static final ArchExpressionKind KIND = new ArchExpressionKind();

    private Set<VariableSymbol> unresolvableVariables = null;

    public ArchExpressionSymbol() {
        super("", KIND);
    }


    public Boolean isResolvable(){
        Set<VariableSymbol> set = getUnresolvableVariables();
        return set != null && set.isEmpty();
    }

    public Set<VariableSymbol> getUnresolvableVariables() {
        if (unresolvableVariables == null){
            checkIfResolvable(new HashSet<>());
        }
        return unresolvableVariables;
    }

    protected void setUnresolvableVariables(Set<VariableSymbol> unresolvableVariables){
        this.unresolvableVariables = unresolvableVariables;
    }

    public void checkIfResolvable(Set<VariableSymbol> seenVariables){
        Set<VariableSymbol> unresolvableVariables = new HashSet<>();
        computeUnresolvableVariables(unresolvableVariables, seenVariables);
        setUnresolvableVariables(unresolvableVariables);
    }


    /**
     * Checks whether the value is a boolean. If true getValue() will return a Boolean if present.
     *
     * @return returns true iff the value of the resolved expression will be a boolean.
     */
    abstract public boolean isBoolean();

    /**
     * Checks whether the value is a number.
     * Note that the return of getValue() can be either a Double or an Integer if present.
     *
     * @return returns true iff the value of the resolved expression will be a number.
     */
    abstract public boolean isNumber();

    /**
     * Checks whether the value is a Tuple.
     * If true getValue() will return (if present) a List of Objects.
     * These Objects can either be Integer, Double or Boolean.
     *
     * @return returns true iff the value of the expression will be a tuple.
     */
    abstract public boolean isTuple();

    public boolean isString(){
        if (getValue().isPresent()){
            return getStringValue().isPresent();
        }
        return false;
    }

    /**
     * Checks whether the value is an integer. This can only be checked if the expression is resolved.
     * If true getRhs() will return an Integer.
     *
     * @return returns true iff the value of the expression is an integer.
     *         The Optional is present if the expression was resolved.
     */
    public Optional<Boolean> isInt(){
        if (getValue().isPresent()){
            return Optional.of(getIntValue().isPresent());
        }
        return Optional.empty();
    }

    public Optional<Boolean> isIntTuple(){
        if (getValue().isPresent()){
            return Optional.of(getIntTupleValues().isPresent());
        }
        return Optional.empty();
    }

    public Optional<Boolean> isNumberTuple(){
        if (getValue().isPresent()){
            return Optional.of(getDoubleTupleValues().isPresent());
        }
        return Optional.empty();
    }

    public Optional<Boolean> isBooleanTuple(){
        if (getValue().isPresent()){
            return Optional.of(getBooleanTupleValues().isPresent());
        }
        return Optional.empty();
    }

    /**
     * Checks whether the value is a parallel Sequence.
     * If true, getValue() will return (if present) a List of Lists of Objects.
     * These Objects can either be Integer, Double or Boolean.
     * If isSerialSequence() returns false, the second List will always have a size smaller than 2.
     * Sequences of size 1 or 0 cannot be parallel sequences.
     *
     * @return returns true iff the value contains a parallel sequence.
     */
    public boolean isParallelSequence(){
        return false;
    }

    /**
     * Checks whether the value is a serial Sequence.
     * If true, getValue() will return (if present) a List(parallel) of Lists(serial) of Objects.
     * If isParallelSequence() is false, the first list will be of size 1.
     * These Objects can either be Integer, Double or Boolean.
     * Sequences of size 1 or 0 are counted as serial sequences.
     * Therefore, this returns always true if isParallelSequence() returns false.
     *
     * @return returns true iff the value contains a serial sequence.
     */
    public boolean isSerialSequence(){
        return false;
    }

    /**
     *
     * @return returns true if this object is instance of ArchRangeExpressionSymbol
     */
    public boolean isRange(){
        return false;
    }

    public boolean isSequence(){
        return false;
    }

    /**
     *
     * @return returns true if this object is instance of ArchSimpleExpressionSymbol
     */
    public boolean isSimpleValue(){
        return false;
    }

    public Optional<Integer> getIntValue(){
        Optional<Object> optValue = getValue();
        if (optValue.isPresent() && (optValue.get() instanceof Integer)){
            return Optional.of((Integer) optValue.get());
        }
        return Optional.empty();
    }

    public Optional<Double> getDoubleValue(){
        Optional<Object> optValue = getValue();
        if (optValue.isPresent()){
            if (optValue.get() instanceof Double){
                return Optional.of((Double) optValue.get());
            }
            if (optValue.get() instanceof Integer){
                return Optional.of(((Integer) optValue.get()).doubleValue());
            }
        }
        return Optional.empty();
    }

    public Optional<Boolean> getBooleanValue(){
        Optional<Object> optValue = getValue();
        if (optValue.isPresent() && (optValue.get() instanceof Boolean)){
            return Optional.of((Boolean) optValue.get());
        }
        return Optional.empty();
    }

    public Optional<String> getStringValue(){
        Optional<Object> optValue = getValue();
        if (optValue.isPresent() && (optValue.get() instanceof String)){
            return Optional.of((String) optValue.get());
        }
        return Optional.empty();
    }

    public Optional<List<Integer>> getIntTupleValues(){
        Optional<List<Object>> optValue = getTupleValues();
        if (optValue.isPresent()){
            List<Integer> list = new ArrayList<>();
            for (Object value : optValue.get()) {
                if (value instanceof Integer){
                    list.add((Integer) value);
                }
                else {
                    return Optional.empty();
                }
            }
            return Optional.of(list);
        }
        return Optional.empty();
    }

    public Optional<List<Double>> getDoubleTupleValues() {
        Optional<List<Object>> optValue = getTupleValues();
        if (optValue.isPresent()){
            List<Double> list = new ArrayList<>();
            for (Object value : optValue.get()) {
                if (value instanceof Double) {
                    list.add((Double) value);
                }
                else if (value instanceof Integer){
                    list.add(((Integer) value).doubleValue());
                }
                else {
                    return Optional.empty();
                }
            }
            return Optional.of(list);
        }
        return Optional.empty();
    }

    public Optional<List<Boolean>> getBooleanTupleValues() {
        Optional<List<Object>> optValue = getTupleValues();
        if (optValue.isPresent()){
            List<Boolean> list = new ArrayList<>();
            for (Object value : optValue.get()) {
                if (value instanceof Boolean) {
                    list.add((Boolean) value);
                }
                else {
                    return Optional.empty();
                }
            }
            return Optional.of(list);
        }
        return Optional.empty();
    }

    public Optional<List<Object>> getTupleValues(){
        if (getValue().isPresent()){
            Optional<Object> optValue = getValue();
            if (optValue.isPresent() && (optValue.get() instanceof List)){
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) getValue().get();
                return Optional.of(list);
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> getParallelLength(){
        Optional<List<List<ArchSimpleExpressionSymbol>>> elements = getElements();
        return elements.map(e -> e.isEmpty() ? 1 : e.size());
    }

    public Optional<List<Integer>> getSerialLengths(){
        Optional<List<List<ArchSimpleExpressionSymbol>>> elements = getElements();
        if (elements.isPresent()){
            List<Integer> serialLengths = new ArrayList<>();
            for (List<ArchSimpleExpressionSymbol> serialList : getElements().get()){
                serialLengths.add(serialList.size());
            }
            return Optional.of(serialLengths);
        }
        else {
            return Optional.empty();
        }
    }

    public Optional<Integer> getMaxSerialLength(){
        int max = 0;
        Optional<List<Integer>> optLens = getSerialLengths();
        if (optLens.isPresent()){
            for (int len : optLens.get()){
                if (len > max){
                    max = len;
                }
            }
        }
        else {
            return Optional.empty();
        }
        return Optional.of(max);
    }

    /**
     * Same as resolve() but throws an error if it was not successful.
     */
    public void resolveOrError(){
        resolve();
        if (!isResolved()){
            throw new IllegalStateException("The following expression could not be resolved: " + getTextualRepresentation() +
                    ". The following names are unresolvable: " + getUnresolvableVariables());
        }
    }

    abstract public String getTextualRepresentation();

    /**
     * This method returns the result of the expression if it is already resolved.
     * This can be a primitive object (Integer, Double or Boolean)
     * or a list of primitive objects if it is a tuple
     * or a list of lists of primitive objects if it is a sequence. (See other methods for more information)
     *
     * @return returns the value as Object or Optional.empty if the expression is not resolved.
     *
     */
    abstract public Optional<Object> getValue();

    abstract public void reset();

    /**
     * Replaces all variable names in this values expression if possible.
     * The values of the variables depend on the current scope. The replacement is irreversible if successful.
     *
     * @return returns a set of all names which could not be resolved.
     */
    abstract public Set<VariableSymbol> resolve();

    /**
     * @return returns a optional of a list(parallel) of lists(serial) of simple expressions in this sequence.
     *         These lists will only contain one element if this is not a sequence.
     *         If the optional is not present that means this expression is a range which is not resolved.
     */
    abstract public Optional<List<List<ArchSimpleExpressionSymbol>>> getElements();

    abstract protected void computeUnresolvableVariables(Set<VariableSymbol> unresolvableVariables, Set<VariableSymbol> allVariables);

    /**
     * @return returns true if the expression is resolved.
     */
    abstract public boolean isResolved();

    abstract public ArchExpressionSymbol copy();

    protected void putInScope(MutableScope scope){
        Collection<Symbol> symbolsInScope = scope.getLocalSymbols().get(getName());
        if (symbolsInScope == null || !symbolsInScope.contains(this)) {
            scope.add(this);
        }
    }

}
