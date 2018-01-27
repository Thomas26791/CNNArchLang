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

import de.monticore.lang.monticar.cnnarch._ast.ASTArchValueRange;
import de.monticore.lang.monticar.cnnarch.helper.ErrorCodes;
import de.se_rwth.commons.logging.Log;

public class CheckRangeOperators implements CNNArchASTArchValueRangeCoCo {

    @Override
    public void check(ASTArchValueRange node) {
        if (node.getParallel().isPresent()){
            if (!node.getParallel2().isPresent()){
                differentOperatorError(node);
            }
        }
        else {
            if (node.getParallel2().isPresent()){
                differentOperatorError(node);
            }
        }
    }

    private void differentOperatorError(ASTArchValueRange node){
        Log.error("0" + ErrorCodes.DIFFERENT_RANGE_OPERATORS +
                        " the second layer operator ('->' or '|') in a range has to be identical to the first one."
                , node.get_SourcePositionStart());
    }

}
