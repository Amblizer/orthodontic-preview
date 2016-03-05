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
import org.weasis.core.api.media.data.MediaSeriesGroup;

/**
 *
 * @author Gabriela Bauermann (gabibau@gmail.com)
 * @version 2016, 20 Jan.
 */
public class MediaSeriesCalculationModel implements PreviewCalculationModel {

    private final MediaSeriesGroup dataSeries;

    public MediaSeriesCalculationModel(MediaSeriesGroup groupID) {
        dataSeries = groupID;
    }

    @Override
    public List<Line2D> getVectorUnits() {
        final Object tagValue = dataSeries.getTagValue(TagO.FORCE_VECTOR_UNITS);
        if (tagValue instanceof List) {
            return (List<Line2D>) tagValue;
        }
        return null;
    }

    @Override
    public void setDebugVectorUnits(List<Line2D> debug) {
        dataSeries.setTag(TagO.DEBUG, debug);
    }

    @Override
    public void setPointGr(Point2D pointGr) {
        dataSeries.setTag(TagO.POINT_GR, pointGr);
    }

    @Override
    public void setResultant(Line2D result) {
        dataSeries.setTag(TagO.RESULTANT, result);
    }

    @Override
    public GeneralPath getArcPath() {
        Object tagValue = dataSeries.getTagValue(TagO.ARC);
        if (tagValue instanceof GeneralPath) {
            return (GeneralPath) tagValue;
        }
        return null;
    }

    @Override
    public Line2D getResultant() {
        Object tagValue = dataSeries.getTagValue(TagO.RESULTANT);
        if (tagValue instanceof Line2D) {
            return (Line2D) tagValue;
        }
        return null;
    }

    @Override
    public void setResultBounds(Rectangle bounds) {
        dataSeries.setTag(TagO.RESULT_BOUNDS, bounds);
    }

    @Override
    public Point2D getPointGr() {
        Object tagValue = dataSeries.getTagValue(TagO.POINT_GR);
        if (tagValue instanceof Point2D) {
            return (Point2D) tagValue;
        }
        return null;
    }

}
