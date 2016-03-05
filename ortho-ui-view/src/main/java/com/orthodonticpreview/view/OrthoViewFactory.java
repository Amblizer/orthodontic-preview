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
package com.orthodonticpreview.view;

import com.orthodonticpreview.ui.OrthodonticDataExtractor;
import com.orthodonticpreview.view.graphics.ArcGraphic;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.graphic.Graphic;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version
 */
public class OrthoViewFactory
        implements SeriesViewerFactory, OrthodonticDataExtractor {

    public static final String NAME = "Orhtodontic Viewer";

    @Override
    public SeriesViewer<? extends MediaElement<?>> createSeriesViewer(Map<String, Object> properties) {

        ViewContainer instance = new ViewContainer();
        if (properties != null) {
            Object obj = properties.get(DataExplorerModel.class.getName());
            if (obj instanceof DataExplorerModel) {
                // Register the PropertyChangeListener
                DataExplorerModel m = (DataExplorerModel) obj;
                m.addPropertyChangeListener(instance);
            }
        }
        return instance;
    }

    @Override
    public boolean canReadMimeType(String mimeType) {
        if (mimeType != null && mimeType.startsWith("image/")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isViewerCreatedByThisFactory(SeriesViewer viewer) {
        return viewer instanceof ViewContainer;
    }

    @Override
    public int getLevel() {
        return 4;
    }

    @Override
    public List<Action> getOpenActions() {
        ArrayList<Action> actions = new ArrayList<Action>();
        return actions;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getUIName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public JButton getButton(final int dataTypeButton) {
        JButton button = null;
        if (dataTypeButton == OrthodonticDataExtractor.ARC_TYPE_BUTTON) {
            for (Graphic graph : OrthoEventManager.graphicList) {
                if (graph instanceof ArcGraphic) {
                    button = new JButton(graph.getIcon());
                    button.addActionListener(
                            (ActionListener) OrthoEventManager.getInstance());
                    button.setActionCommand(((ArcGraphic) graph).toString());
                    button.setToolTipText(graph.toString());
                }
            }
        }
        return button;
    }

    @Override
    public List<Line2D> getForceVectorList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GeneralPath getOrthodonticArc() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getMidX() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getVectorsScale() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canAddSeries() {
        return false;
    }

    @Override
    public boolean canExternalizeSeries() {
        return false;
    }

}
