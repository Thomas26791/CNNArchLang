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
package de.monticore.lang.monticar.cnnarch._cocos;

import de.monticore.symboltable.Scope;
import de.monticore.symboltable.ScopeSpanningSymbol;
import de.monticore.symboltable.Symbol;

import java.util.*;

public class CNNArchSymbolCoCoChecker {

    private List<CNNArchSymbolCoCo> cocos =  new ArrayList<>();

    public List<CNNArchSymbolCoCo> getCocos() {
        return cocos;
    }

    public CNNArchSymbolCoCoChecker addCoCo(CNNArchSymbolCoCo coco) {
        getCocos().add(coco);
        return this;
    }

    public void checkAll(Symbol sym) {
        handle(sym, new HashSet<>());
    }

    public void check(Symbol sym){
        for (CNNArchSymbolCoCo coco : getCocos()){
            coco.check(sym);
        }
    }

    public void handle(Symbol sym, Set<Symbol> checkedSymbols){
        if (!checkedSymbols.contains(sym)) {
            check(sym);
            checkedSymbols.add(sym);
            if (sym instanceof ScopeSpanningSymbol) {
                traverse(((ScopeSpanningSymbol) sym).getSpannedScope(), checkedSymbols);
            }
        }
    }

    public void traverse(Scope scope, Set<Symbol> checkedSymbols){
        for (Collection<Symbol> collection : scope.getLocalSymbols().values()){
            for (Symbol sym : collection){
                handle(sym, checkedSymbols);
            }
        }
        for (Scope subScope : scope.getSubScopes()){
            if (!subScope.isSpannedBySymbol()){
                traverse(subScope, checkedSymbols);
            }
        }
    }

}
