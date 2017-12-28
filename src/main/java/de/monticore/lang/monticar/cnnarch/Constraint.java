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
package de.monticore.lang.monticar.cnnarch;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchSimpleExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.TupleExpressionSymbol;

public enum Constraint {
    NUMBER {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            return exp.isNumber();
        }
    },
    INTEGER {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            return exp.isInt().get();
        }
    },
    BOOLEAN {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            return exp.isBoolean();
        }
    },
    TUPLE {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            return exp.isTuple();
        }
    },
    INTEGER_TUPLE {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            boolean res = false;
            if (exp.isTuple()){
                //todo
            }
            return false;
        }
    },
    POSITIVE {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            return false;
        }
    },
    NON_NEGATIVE {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            return false;
        }
    },
    BETWEEN_ZERO_AND_ONE {
        @Override
        public boolean check(ArchSimpleExpressionSymbol exp) {
            return false;
        }
    };

    abstract public boolean check(ArchSimpleExpressionSymbol exp);

    public boolean check(Constraint constraint, ArchSimpleExpressionSymbol exp){
        return constraint.check(exp);
    }
}
