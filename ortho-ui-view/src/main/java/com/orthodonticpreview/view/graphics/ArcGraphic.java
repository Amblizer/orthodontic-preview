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
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
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
 * @version 2012, jul 26.
 */
public class ArcGraphic extends AbstractDragGraphic
        implements PortableGraphic {

    /**
     * Icon.
     */
    public static final Icon ICON
            = new ImageIcon(ArcGraphic.class.getResource(
                            "/icon/22x22/arc.png"));

    /**
     * "Measurement" used to store the text.
     */
    public static final Measurement TEXT = new Measurement(
            Messages.getString("ArcGraphic.Arch"), 1, true, true, true);

    /**
     * Two line points.
     */
    protected Point2D ptA, ptB;
    /**
     * true to a valid line.
     */
    protected boolean lineABvalid;

    public ArcGraphic(final float lineThickness,
            final Color paintColor, final boolean labelVisible) {
        super(2, paintColor, lineThickness, labelVisible);
    }

    public ArcGraphic(GraphicPack pack) {
        super(2, pack.getPaintColor(), pack.getLineThickness(), true);
        Shape mainShape = pack.getMainShape();
        if (mainShape instanceof GeneralPath) {
            GeneralPath path = (GeneralPath) mainShape;
            Point2D.Double[] points = getPoints(path);

            //create shape
            handlePointList.clear();
            handlePointList.add(points[0]);
            handlePointList.add(points[1]);

            buildShape(null);
        }
    }

    @Override
    protected void buildShape(MouseEventDouble mouseEvent) {
        updateTool();
        GeneralPath newShape = null;

        if (lineABvalid) {
            newShape = new GeneralPath();
            double hDist = ptB.getX() - ptA.getX();

            newShape.moveTo((float) ptA.getX(), (float) ptA.getY());
            newShape.lineTo((float) ptA.getX(), (float) ptA.getY() - hDist / 5);

            newShape.curveTo((float) ptA.getX(), (float) ptA.getY() - hDist * 1.2,
                    (float) ptB.getX(), (float) ptB.getY() - hDist * 1.2,
                    (float) ptB.getX(), (float) ptB.getY() - hDist / 5);

            newShape.lineTo((float) ptB.getX(), (float) ptB.getY());

        }

        setShape(newShape, mouseEvent);

    }

    @Override
    public List<MeasureItem> computeMeasurements(ImageLayer layer, boolean releaseEvent, Unit displayUnit) {
        final List<MeasureItem> measVal = new ArrayList<MeasureItem>();
        return measVal;
    }

    @Override
    public List<Measurement> getMeasurementList() {
        final List<Measurement> list = new ArrayList<Measurement>();
        return list;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("ArcGraphic.Arch") + " 1";
    }

    /**
     * Get point and test if its a valid line. Also manages handlePoint 1 so it
     * keeps the same y as handlePoint 0.
     */
    private void updateTool() {
        ptA = getHandlePoint(0);
        Point2D.Double handle1 = getHandlePoint(1);
        if (handle1.getY() != ptA.getY()) {
            handle1.setLocation(handle1.getX(), ptA.getY());
            setHandlePoint(1, handle1);
        }
        ptB = getHandlePoint(1);

        lineABvalid = ptA != null && ptB != null && !ptB.equals(ptA);
    }

    @Override
    public Color getPaintColor() {
        return (Color) colorPaint;
    }

    @Override
    public String getClassName() {
        return ArcGraphic.class.getName();
    }

    /**
     * Calculates first and last point of the given path.
     *
     * @param path
     * @return
     */
    private static Point2D.Double[] getPoints(GeneralPath path) {

        Point2D.Double[] pts = new Point2D.Double[2];
        double[] coords = new double[6];
        int numSubPaths = 0;
        for (PathIterator pi = path.getPathIterator(null);
                !pi.isDone();
                pi.next()) {
            switch (pi.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    double[] point = Arrays.copyOf(coords, 2);

                    if (numSubPaths == 0) {
                        pts[0] = new Point2D.Double(point[0], point[1]);
                    }

                    ++numSubPaths;
                    break;
                case PathIterator.SEG_LINETO:
                    double[] point2 = Arrays.copyOf(coords, 2);

                    if (numSubPaths == 2) {
                        pts[1] = new Point2D.Double(point2[0], point2[1]);
                    }
                    ++numSubPaths;
                    break;
                default:
                    break;
            }
        }
        return pts;
    }

    @Override
    public String getLinkedOwner() {
        return null;
    }

}
