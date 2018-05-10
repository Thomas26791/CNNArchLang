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

import de.monticore.lang.math.math._symboltable.expression.MathExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.symboltable.Symbol;

public class CNNArchSymbolCoCo {

    public void check(Symbol sym){
        if (sym instanceof ArchitectureSymbol){
            check((ArchitectureSymbol) sym);
        }
        else if (sym instanceof LayerDeclarationSymbol){
            check((LayerDeclarationSymbol) sym);
        }
        else if (sym instanceof ArchitectureElementSymbol){
            check((ArchitectureElementSymbol) sym);
        }
        else if (sym instanceof ArchExpressionSymbol){
            check((ArchExpressionSymbol) sym);
        }
        else if (sym instanceof ArchTypeSymbol){
            check((ArchTypeSymbol) sym);
        }
        else if (sym instanceof VariableSymbol){
            check((VariableSymbol) sym);
        }
        else if (sym instanceof ArgumentSymbol){
            check((ArgumentSymbol) sym);
        }
        else if (sym instanceof CNNArchCompilationUnitSymbol){
            check((CNNArchCompilationUnitSymbol) sym);
        }
        else if (sym instanceof IODeclarationSymbol){
            check((IODeclarationSymbol) sym);
        }
        else if (sym instanceof MathExpressionSymbol){
            check((MathExpressionSymbol) sym);
        }
        else{
            throw new IllegalStateException("Symbol class is unknown in CNNArchSymbolCoCo: "
                    + sym.getClass().getSimpleName());
        }
    }


    public void check(ArchitectureSymbol sym){
        //Override if needed
    }

    public void check(LayerDeclarationSymbol sym){
        //Override if needed
    }

    public void check(ArchitectureElementSymbol sym){
        //Override if needed
    }

    public void check(ArchExpressionSymbol sym){
        //Override if needed
    }

    public void check(ArchTypeSymbol sym){
        //Override if needed
    }

    public void check(VariableSymbol sym){
        //Override if needed
    }

    public void check(ArgumentSymbol sym){
        //Override if needed
    }

    public void check(CNNArchCompilationUnitSymbol sym){
        //Override if needed
    }

    public void check(IODeclarationSymbol sym){
        //Override if needed
    }

    public void check(MathExpressionSymbol sym){
        //Override if needed
    }
}
