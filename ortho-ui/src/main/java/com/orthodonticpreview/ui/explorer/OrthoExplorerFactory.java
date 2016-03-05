/**
 * *****************************************************************************
 * Copyright (c) 2012 Cesar Moreira.
 *
 * This file is part of Orthodontic Preview.
 *
 * Orthodontic Preview is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Orthodontic Preview is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Orthodontic Preview. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package com.orthodonticpreview.ui.explorer;

import java.util.Hashtable;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.weasis.core.api.explorer.DataExplorerView;
import org.weasis.core.api.explorer.DataExplorerViewFactory;

/**
 *
 * @author Gabriela Bauermann (gabibau@gmail.com).
 * @version 2016, 20, Jan
 */
@Component(immediate = false)
@Service
@Properties(value = {
    @Property(name = "service.name", value = "Orthodontic Explorer"),
    @Property(name = "service.description", value = "Orthodontic Explorer")})
public class OrthoExplorerFactory implements DataExplorerViewFactory {

    @Override
    public DataExplorerView createDataExplorerView(Hashtable<String, Object> hshtbl) {
        return new OrthoExplorerView();
    }

}
