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
package com.orthodonticpreview.datamodel;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

/**
 *
 * @author Gabriela Bauermann (gabibau@gmail.com)
 * @version 2016, 20 Jan.
 */
public interface PreviewCalculationModel {

    List<Line2D> getVectorUnits();

    void setDebugVectorUnits(List<Line2D> debug);

    void setPointGr(Point2D pointGr);
    
    Point2D getPointGr();

    void setResultant(Line2D result);

    GeneralPath getArcPath();

    Line2D getResultant();

    public void setResultBounds(Rectangle bounds);
}
