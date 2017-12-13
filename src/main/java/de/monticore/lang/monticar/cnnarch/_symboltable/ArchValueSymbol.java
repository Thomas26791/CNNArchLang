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
import de.monticore.symboltable.SymbolKind;

import java.util.Optional;

abstract public class ArchValueSymbol extends CommonSymbol {

    public static final ArchValueKind KIND = new ArchValueKind();

    private boolean fullyResolved = false;


    public ArchValueSymbol() {
        super("", KIND);
    }

    /**
     * Getter for the fullyResolved attribute.
     * If it is still false for the return of resolve()
     * then the value contains a dimension variable for input and output which has to be set.
     *
     * @return returns true if the value contains no variables.
     */
    public boolean isFullyResolved() {
        return fullyResolved;
    }

    public void setFullyResolved(boolean fullyResolved) {
        this.fullyResolved = fullyResolved;
    }

    /**
     * Checks whether the value is a boolean. If true getValue() will return a Boolean if present.
     *
     * @return returns true iff the value of the resolved expression will be a boolean.
     */
    public boolean isBoolean(){
        return false;
    }

    /**
     * Checks whether the value is a number.
     * Note that the return of getValue() can be either a Double or an Integer if present.
     *
     * @return returns true iff the value of the resolved expression will be a number.
     */
    public boolean isNumber(){
        return false;
    }

    /**
     * Checks whether the value is a Tuple.
     * If true getValue() will return (if present) a List of Objects.
     * These Objects can either be Integer, Double or Boolean.
     *
     * @return returns true iff the value of the expression will be a tuple.
     */
    public boolean isTuple(){
        return false;
    }

    /**
     * Checks whether the value is an integer. This can only be checked if the expression is resolvable.
     * If true getValue() will return an Integer.
     *
     * @return returns Optional.of(true) iff the value of the expression is an integer.
     *         The Optional is present if the expression can be resolved.
     */
    public Optional<Boolean> isInt(){
        return Optional.of(false);
    }

    /**
     * Checks whether the value is a parallel Sequence.
     * If true, getValue() will return (if present) a List of Lists of Objects.
     * These Objects can either be Integer, Double or Boolean.
     * If isSerialSequence() returns false, the second List will always have a size smaller than 2.
     *
     * @return returns true iff the value contains a parallel sequence.
     */
    public boolean isParallelSequence(){
        return false;
    }

    /**
     * Checks whether the value is a serial Sequence.
     * If true, getValue() will either return (if present) a List of Objects
     * or a List(parallel) of Lists(serial) of Objects if isParallelSequence() is also true.
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
     * @return returns true if this object is instance of ArchRangeValueSymbol
     */
    public boolean isRange(){
        return false;
    }

    /**
     *
     * @return returns true if this object is instance of ArchSimpleValueSymbol
     */
    public boolean isSimpleValue(){
        return false;
    }



    /**
     * This method returns the result of the expression.
     * This can be a primitive object (Integer, Double or Boolean)
     * or List of primitive objects
     * or a list of lists of primitive objects. (See other methods for more information)
     *
     * @return returns the value as Object or Optional.empty if the expression cannot be completely resolved yet.
     *
     */
    abstract public Optional<Object> getValue();

    /**
     * Creates a copy of this symbol where all Variables are replaced by expressions without variables.
     * If the expression contains an IOVariable which has not yet been set
     * then the expression is resolved as much as possible and the attribute fullyResolved of the return object remains false.
     *
     * @return returns a copy of this object where the expression is resolved as much as possible
     *         or itself if attribute fullyResolved is true.
     */
    abstract public ArchValueSymbol resolve();

    //abstract public ArchValueSymbol resolveCopy()
}
