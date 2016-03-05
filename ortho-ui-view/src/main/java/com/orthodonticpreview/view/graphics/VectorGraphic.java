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
package com.orthodonticpreview.view.graphics;

import com.orthodonticpreview.ui.persistence.GraphicPack;
import com.orthodonticpreview.ui.persistence.PortableGraphic;
import com.orthodonticpreview.view.internal.Messages;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;
import org.weasis.core.api.image.util.ImageLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.ui.graphic.Graphic;
import org.weasis.core.ui.graphic.LineGraphic;
import org.weasis.core.ui.graphic.MeasureItem;
import org.weasis.core.ui.graphic.model.AbstractLayerModel;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, aug 5
 */
public class VectorGraphic extends LineGraphic implements PortableGraphic {

    private String linkedOwner;

    public VectorGraphic(
            float lineThickness, Color paintColor, boolean labelVisible) {
        super(lineThickness, paintColor, labelVisible);
    }

    public VectorGraphic(GraphicPack pack) {
        super(pack.getLineThickness(), pack.getPaintColor(), true);
        setLinkedOwner(pack.getOwner());
        Shape mainShape = pack.getMainShape();

        if (mainShape instanceof Line2D) {
            createShape((Line2D) mainShape);
        }
    }

    @Override
    public String getUIName() {
        return Messages.getString("VectorGraphic.Vector");
    }

    /**
     * Overriden so it never shows labels.
     *
     * @param layer
     * @param releaseEvent
     * @return Allways null.
     */
    @Override
    public List<MeasureItem> computeMeasurements(ImageLayer layer,
            boolean releaseEvent, Unit displayUnit) {
        return null;
    }

    public void createShape(Line2D line) {
        handlePointList.clear();
        handlePointList.add((Point2D.Double) line.getP1());
        handlePointList.add((Point2D.Double) line.getP2());

        buildShape(null);
    }

    public double getVectorLenght() {
        return ptA.distance(ptB);
    }

    public void changeShape(Line2D line) {
        handlePointList.get(0).setLocation(line.getP1());
        handlePointList.get(1).setLocation(line.getP2());

        buildShape(null);
    }

    /**
     * @return the linkedOwner
     */
    public String getLinkedOwner() {
        return linkedOwner;
    }

    /**
     * Sets the linked Owner.
     *
     * @param linkedOwner the linkedOwner to set
     */
    public void setLinkedOwner(final String linkedOwner) {
        this.linkedOwner = linkedOwner;
    }

    /**
     * Sets the linked Owner, after cleaning other Vectors with the same owner
     * at the same Layer model.
     *
     * Ensures that no more that one Vector will have this linkedOwner.
     *
     * @param linkedOwner the linkedOwner to set
     * @param model LayerModel to check
     */
    public void setLinkedOwner(final String linkedOwner,
            final AbstractLayerModel model) {

        if (model != null) {
            for (Graphic graphic : model.getAllGraphics()) {
                if (graphic instanceof VectorGraphic) {
                    final VectorGraphic vector = (VectorGraphic) graphic;
                    if (linkedOwner.equals((vector.getLinkedOwner()))) {
                        vector.setLinkedOwner(null);
                    }
                }
            }
        }

        this.linkedOwner = linkedOwner;
    }

    @Override
    public Color getPaintColor() {
        return (Color) colorPaint;
    }

    @Override
    public String getClassName() {
        return VectorGraphic.class.getName();
    }
}
