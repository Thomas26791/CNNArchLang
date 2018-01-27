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


import de.monticore.symboltable.resolving.CommonResolvingFilter;

public class CNNArchLanguage extends CNNArchLanguageTOP {

    public static final String FILE_ENDING = "cnna";

    public CNNArchLanguage() {
        super("CNNArch Language", FILE_ENDING);
    }

    @Override
    protected CNNArchModelLoader provideModelLoader() {
        return new CNNArchModelLoader(this);
    }

    @Override
    protected void initResolvingFilters() {
        super.initResolvingFilters();
        //addResolvingFilter(CommonResolvingFilter.create(MathExpressionSymbol.KIND));
        addResolvingFilter(new ArchitectureResolvingFilter());
        addResolvingFilter(CommonResolvingFilter.create(MethodDeclarationSymbol.KIND));
        addResolvingFilter(CommonResolvingFilter.create(LayerSymbol.KIND));
        addResolvingFilter(CommonResolvingFilter.create(VariableSymbol.KIND));
        addResolvingFilter(CommonResolvingFilter.create(IODeclarationSymbol.KIND));
        addResolvingFilter(CommonResolvingFilter.create(ArgumentSymbol.KIND));
        //addResolvingFilter(CommonResolvingFilter.create(ArchExpressionSymbol.KIND));
        setModelNameCalculator(new CNNArchModelNameCalculator());
    }

}
