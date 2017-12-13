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
/* generated by template symboltable.ScopeSpanningSymbol*/



package de.monticore.lang.monticar.cnnarch._symboltable;

import static de.monticore.symboltable.Symbols.sortSymbolsByPosition;

import java.util.Collection;

public class ArchitectureSymbol extends de.monticore.symboltable.CommonScopeSpanningSymbol {

  /* generated by template symboltable.symbols.KindConstantDeclaration*/


  public static final ArchitectureKind KIND = new ArchitectureKind();


  public ArchitectureSymbol(String name) {
    super(name, KIND);
  }


  @Override
  protected ArchitectureScope createSpannedScope() {
    return new ArchitectureScope();
  }

  /* Possible methods for containinig symbols
  */
}
