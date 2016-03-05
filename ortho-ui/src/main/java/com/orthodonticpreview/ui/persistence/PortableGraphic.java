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
package com.orthodonticpreview.ui.persistence;


import java.awt.Color;
import java.awt.Shape;

/**
 * Interface that makes a Graphis "savable".
 * 
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 12 Nov
 */
public interface PortableGraphic {

    String getClassName();

    Shape getShape();

    Color getPaintColor();

    float getLineThickness();

    String getLinkedOwner();

}
