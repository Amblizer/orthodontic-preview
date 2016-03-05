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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.weasis.core.api.image.util.ImageLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.ui.graphic.AbstractDragGraphic;
import org.weasis.core.ui.graphic.MeasureItem;
import org.weasis.core.ui.graphic.Measurement;
import org.weasis.core.ui.util.MouseEventDouble;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version
 */
public class DotGraphic extends AbstractDragGraphic
        implements PortableGraphic {

    private String linkedOwner;
    protected Point2D center;
    protected double radio = 6;

    public static final Icon ICON = new ImageIcon(
            DotGraphic.class.getResource("/icon/22x22/dot2.png"));

    public DotGraphic(final float lineThickness,
            final Color paintColor, final boolean labelVisible) {
        super(1, paintColor, lineThickness, labelVisible, false);
    }

    public DotGraphic(GraphicPack pack) {
        super(2, pack.getPaintColor(), pack.getLineThickness(), true);
        setLinkedOwner(pack.getOwner());
        Shape mainShape = pack.getMainShape();
        if (mainShape instanceof Ellipse2D) {
            Ellipse2D ellipse = (Ellipse2D) mainShape;
            createShape(new Point2D.Double(
                    ellipse.getCenterX(), ellipse.getCenterY()));
        }
    }

    /**
     * @return the linkedOwner
     */
    public String getLinkedOwner() {
        return linkedOwner;
    }

    /**
     * @param linkedOwner the linkedOwner to set
     */
    public void setLinkedOwner(String linkedOwner) {
        this.linkedOwner = linkedOwner;
    }

    @Override
    protected void buildShape(MouseEventDouble mouseEvent) {
        center = getHandlePoint(0);

        Ellipse2D shape = new Ellipse2D.Double(
                center.getX() - radio, center.getY() - radio,
                radio * 2, radio * 2);

        setShape(shape, mouseEvent);
    }

    @Override
    public List<MeasureItem> computeMeasurements(ImageLayer layer,
            boolean releaseEvent, Unit displayUnit) {
        return null;
    }

    @Override
    public List<Measurement> getMeasurementList() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("DotGraphic.Dot");
    }

    public void createShape(Point2D.Double point2D) {
        handlePointList.clear();
        handlePointList.add(point2D);

        buildShape(null);
    }

    public Point2D getCenter() {
        return center;
    }

    @Override
    public Color getPaintColor() {
        return (Color) colorPaint;
    }

    @Override
    public String getClassName() {
        return DotGraphic.class.getName();
    }
    
}
