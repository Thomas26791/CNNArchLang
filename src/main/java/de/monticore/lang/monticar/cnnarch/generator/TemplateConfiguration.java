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
package de.monticore.lang.monticar.cnnarch.generator;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public class TemplateConfiguration {

    private static TemplateConfiguration instance;
    private Configuration configuration;

    private TemplateConfiguration() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setClassForTemplateLoading(TemplateConfiguration.class, "/templates/");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public static Configuration get(){
        if (instance == null){
            instance = new TemplateConfiguration();
        }
        return instance.getConfiguration();
    }

}
