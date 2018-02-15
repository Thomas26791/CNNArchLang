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

import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Symbol;
import de.monticore.symboltable.resolving.ResolvingFilter;

import java.util.Collection;
import java.util.Optional;

public class LayerScope extends de.monticore.symboltable.CommonScope {

    public LayerScope() {
        super(true);
    }

    public LayerScope(Optional<MutableScope> enclosingScope) {
        super(enclosingScope, true);
    }

    @Override
    public void setResolvingFilters(Collection<ResolvingFilter<? extends Symbol>> resolvingFilters) {
        super.setResolvingFilters(resolvingFilters);
    }

    @Override
    public void add(Symbol symbol) {
        super.add(symbol);
        if (symbol instanceof LayerSymbol){
            LayerScope subScope = ((LayerSymbol) symbol).getSpannedScope();
            addSubScope(subScope);
            subScope.setResolvingFilters(getResolvingFilters());
        }
    }
}