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
import java.io.Serializable;

/**
 * Saves important data about a Graphic to rebuild from file.
 *
 * Always check for backward compatibility when making changes on this class.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 12 Nov
 */
public class GraphicPack implements Serializable {

    /**
     * Serial as required by <code>Serializable</code>.
     */
    private static final long serialVersionUID = 3413236630508974186L;

    private Shape mainShape;
    private Color paintColor;
    private float lineThickness;
    private String clazz;
    private String owner;

    /**
     * void Constructor (required by Serializable).
     */
    public GraphicPack() {
        // Empty
    }

    public GraphicPack(final PortableGraphic graphic) {
        mainShape = graphic.getShape();
        paintColor = graphic.getPaintColor();
        lineThickness = graphic.getLineThickness();
        clazz = graphic.getClassName();
        owner = graphic.getLinkedOwner();
    }

    /**
     * @return the mainShape
     */
    public Shape getMainShape() {
        return mainShape;
    }

    /**
     * @param mainShape the mainShape to set
     */
    public void setMainShape(final Shape mainShape) {
        this.mainShape = mainShape;
    }

    /**
     * @return the paintColor
     */
    public Color getPaintColor() {
        return paintColor;
    }

    /**
     * @param paintColor the paintColor to set
     */
    public void setPaintColor(final Color paintColor) {
        this.paintColor = paintColor;
    }

    /**
     * @return the lineThickness
     */
    public float getLineThickness() {
        return lineThickness;
    }

    /**
     * @param lineThickness the lineThickness to set
     */
    public void setLineThickness(final float lineThickness) {
        this.lineThickness = lineThickness;
    }

    /**
     * @return the clazz
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * @param clazz the clazz to set
     */
    public void setClazz(final String clazz) {
        this.clazz = clazz;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(final String owner) {
        this.owner = owner;
    }
}
