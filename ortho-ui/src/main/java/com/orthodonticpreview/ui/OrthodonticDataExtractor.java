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
package com.orthodonticpreview.ui;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.List;
import javax.swing.JButton;

/**
 * Interface to make possible request data (results) from Viewer Plugins.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, Jul 31
 */
public interface OrthodonticDataExtractor {

    /** Button of Arc. */
    int ARC_TYPE_BUTTON = 0;
    /** Button of Flip. */
    int FLIP_TYPE_BUTTON = 1;

    /**
     * Returns a JButton of given Type.
     * @param dataTypeButton Button Type (ARC_TYPE_BUTTON, FLIP_TYPE_BUTTON)
     * @return The Button of given Type.
     */
    JButton getButton(int dataTypeButton);

    /**
     * @return The list of ForceVectores.
     */
    List<Line2D> getForceVectorList();

    /**
     * @return The Ortodontic Arc.
     */
    GeneralPath getOrthodonticArc();

    /**
     * @return The position (just X) of pacient middle line.
     */
    double getMidX();

    /**
     * @return The pixelSpacing (scale) form the Vectors on vectorList.
     */
    double getVectorsScale();

}
