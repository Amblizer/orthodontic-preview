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

import com.orthodonticpreview.datamodel.TagO;
import com.orthodonticpreview.ui.OrthodonticDataExtractor;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ViewerPlugin;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 5 Ago.
 */
public class CalcPreview {

    /**
     * Class logger.
     */
    private static final Logger LOGGER
            = LoggerFactory.getLogger(CalcPreview.class);

    private ImageViewerPlugin calcViewer;
    private GeneralPath arc;
    private double midX;
    private double scale;

    public CalcPreview(MediaSeries groupID) {

        ViewerPlugin plugin = OrthoExplorerView.getService().getViewer(groupID);
        if (plugin instanceof ImageViewerPlugin) {
            calcViewer = (ImageViewerPlugin) plugin;
        }

        List<Line2D> findData = requestData();//findData();
        if (findData != null) {
            groupID.setTag(TagO.FORCE_VECTOR_UNITS, findData);
            if (arc != null) {
                groupID.setTag(TagO.ARC, arc);
            }
            groupID.setTag(TagO.MID_X, midX);
            groupID.setTag(TagO.VECTORS_SCALE, scale);
            OrthoExplorerView.getService().computeOP(groupID);
        } else {
            LOGGER.error("requestData returned null!");
        }
    }

    private List<Line2D> requestData() {
        List toolPanel = calcViewer.getToolPanel();
        for (Object object : toolPanel) {
            if (object instanceof OrthodonticDataExtractor) {
                OrthodonticDataExtractor extractor
                        = (OrthodonticDataExtractor) object;
                arc = extractor.getOrthodonticArc();
                midX = extractor.getMidX();
                scale = extractor.getVectorsScale();
                return extractor.getForceVectorList();
            }
        }
        return null;
    }

}
