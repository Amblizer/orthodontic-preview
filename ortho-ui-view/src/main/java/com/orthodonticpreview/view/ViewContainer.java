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

import com.orthodonticpreview.datamodel.OrthodonticModel;
import com.orthodonticpreview.datamodel.TagO;
import com.orthodonticpreview.ui.explorer.OrthoExplorerView;
import com.orthodonticpreview.view.graphics.DotGraphic;
import com.orthodonticpreview.view.graphics.VectorGraphic;
import com.orthodonticpreview.view.internal.Messages;
import com.orthodonticpreview.view.print.ReportPrintable;
import com.orthodonticpreview.view.tool.OrthoImageTool;
import com.orthodonticpreview.view.tool.TeethTool;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.image.GridBagLayoutModel;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.ui.docking.DockableTool;
import org.weasis.core.ui.editor.SeriesViewerListener;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.SynchView;
import org.weasis.core.ui.graphic.model.AbstractLayer;
import org.weasis.core.ui.util.Toolbar;
import org.weasis.core.ui.util.WtoolBar;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version
 */
public class ViewContainer extends ImageViewerPlugin<ImageElement>
        implements PropertyChangeListener {

    public static final List<DockableTool> TOOLS
            = Collections.synchronizedList(new ArrayList<DockableTool>());
    private boolean debug = false;

    private String titlePrefix = "";

    private ComponentListener pagePreviewListener = new ComponentListener() {
        @Override
        public void componentResized(ComponentEvent ce) {
            setPrintPreview(OrthoEventManager.getInstance().getPageFormat());
        }

        @Override
        public void componentMoved(ComponentEvent ce) {
        }

        @Override
        public void componentShown(ComponentEvent ce) {
        }

        @Override
        public void componentHidden(ComponentEvent ce) {
        }
    };
    private static boolean initStatic = false;

    /**
     * Constructor. Adds the TOOLS.
     */
    public ViewContainer() {
        super(OrthoEventManager.getInstance(), ImageViewerPlugin.VIEWS_1x1,
                null, null, null, null);

        if (!initStatic) {
            final TeethTool teethTool
                    = new TeethTool(Messages.getString("TeethTool.Title"), null);
            TOOLS.add(teethTool);
            eventManager.addSeriesViewerListener((SeriesViewerListener) teethTool);
            TOOLS.add(new OrthoImageTool(
                    Messages.getString("OrthoImageTool.Image")));
            initStatic = true;
        }
    }

    @Override
    public boolean isViewType(Class defaultClass, String type) {
        if (defaultClass != null) {
            try {
                final Class clazz = Class.forName(type);
                return defaultClass.isAssignableFrom(clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public int getViewTypeNumber(GridBagLayoutModel layout, Class defaultClass) {
        return 1; //somente um view (no layouts for now)
    }

    @Override
    public DefaultView2d<ImageElement> createDefaultView(String classType) {
        OrthoView view2d = new OrthoView(eventManager);
        return view2d;
    }

    @Override
    public JComponent createUIcomponent(String clazz) {
        return null;
    }

    @Override
    public List<Action> getExportActions() {
        return null;
    }

    @Override
    public List<Action> getPrintActions() {

        List<Action> list = new ArrayList<Action>();
        final String contentRole
                = (String) getGroupID().getTagValue(TagO.SERIE_ROLE);

        if (OrthoExplorerView.REPORT_IMAGE.equalsIgnoreCase(
                contentRole)) {

            AbstractAction config = new AbstractAction(
                    Messages.getString("ViewContainer.pgConfig")) {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            final PrinterJob job = PrinterJob.getPrinterJob();

                            PageFormat pageF = job.pageDialog(
                                    OrthoEventManager.getInstance().getPrintAset());
                            if (pageF != null) {
                                setPrintPreview(pageF);
                            }
                        }
                    };
            list.add(config);

            AbstractAction print = new AbstractAction(
                    Messages.getString("ViewContainer.print")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            printReport();
                        }
                    };
            list.add(print);

        }

        return list;
    }

    @Override
    public JMenu fillSelectedPluginMenu(JMenu menu) {
        return menu;
    }

    @Override
    public List<Toolbar> getToolBar() {
        return null;
    }

    @Override
    public WtoolBar getStatusBar() {
        return null;
    }

    @Override
    public List<DockableTool> getToolPanel() {
        return TOOLS;
    }

    @Override
    public void addSeries(MediaSeries sequence) {
        super.addSeries(sequence);
        final String contentRole
                = (String) sequence.getTagValue(TagO.SERIE_ROLE);

        if (OrthoExplorerView.REPORT_IMAGE.equalsIgnoreCase(
                contentRole)) {
            removeComponentListener(pagePreviewListener);
            addComponentListener(pagePreviewListener);
            setPrintPreview(OrthoEventManager.getInstance().getPageFormat());
            setTitlePrefix("(R) ");
        }

        //set mouse actions and enable/disable actions to the actual role.
        if (eventManager instanceof OrthoEventManager) {
            ((OrthoEventManager) eventManager).setActions(this, contentRole);
        }

        setPluginName();
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            eventManager.setSelectedView2dContainer(this);
            //select on explorer!
            OrthoExplorerView.getService().getDataExplorerModel()
                    .firePropertyChange(new ObservableEvent(
                                    ObservableEvent.BasicAction.Select, this, null,
                                    getGroupID()));
        } else {
            eventManager.setSelectedView2dContainer(null);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof ObservableEvent) {
            ObservableEvent obsEvt = (ObservableEvent) evt;
            ObservableEvent.BasicAction action = obsEvt.getActionCommand();
            Object newVal = obsEvt.getNewValue();
            Object source = obsEvt.getSource();
            if (ObservableEvent.BasicAction.Update.equals(action)) {

                if (source.equals(this.getGroupID())
                        && newVal == null) {
                    drawResults();
                } else if (source instanceof JDialog
                        && isRelatedToGroupID(newVal)) {
                    setPluginName();
                }
            } else if (ObservableEvent.BasicAction.Remove.equals(action)) {
                if (newVal instanceof Series) {
                    Series series = (Series) newVal;
                    if (series.equals(this.getGroupID())) {
                        getSelectedImagePane().setSeries(null);
                    }
                } else if (newVal instanceof MediaSeriesGroup) {
                    MediaSeriesGroup pat = (MediaSeriesGroup) newVal;
                    String tagValue = (String) pat.getTagValue(TagW.PatientID);
                    if (tagValue != null && tagValue.equals(
                            getGroupID().getTagValue(TagW.PatientID))) {
                        close();
                    }
                }
            } else if (ObservableEvent.BasicAction.Select.equals(action)
                    && !(obsEvt.getSource() instanceof ImageViewerPlugin)) {
                if (newVal instanceof MediaSeriesGroup) {
                    String stUid = (String) ((MediaSeriesGroup) newVal)
                            .getTagValue(TagW.StudyInstanceUID);
                    String thisUid = (String) this.getGroupID()
                            .getTagValue(TagW.StudyInstanceUID);
                    if (stUid != null && thisUid != null && thisUid.equals(stUid)) {
                        setSelectedAndGetFocus();
                    }
                }
            }
        }
    }

    private void drawResults() {
        DefaultView2d view = view2ds.get(0);
        AbstractLayer[] layers = view.getLayerModel().getLayers();
        for (AbstractLayer abstractLayer : layers) {
            if (abstractLayer.getIdentifier() == AbstractLayer.MEASURE) {

                if (debug) {
                    VectorGraphic lineGraphic = new VectorGraphic(3, Color.RED, true);
                    Line2D tagValue = (Line2D) getGroupID().getTagValue(TagO.RESULTANT);
                    lineGraphic.createShape(tagValue);
                    abstractLayer.addGraphic(lineGraphic);

                    VectorGraphic lineGraphic2 = new VectorGraphic(3, Color.RED, true);
                    Point2D tagValue2 = (Point2D) getGroupID().getTagValue(TagO.POINT_GR);
                    Line2D line = new Line2D.Double(tagValue2.getX() - 10, tagValue2.getY(), tagValue2.getX() + 10, tagValue2.getY());
                    lineGraphic2.createShape(line);
                    abstractLayer.addGraphic(lineGraphic2);

                    //debug
                    List tagVal3 = (List) getGroupID().getTagValue(TagO.DEBUG);
                    for (int i = 0; i < tagVal3.size(); i++) {
                        VectorGraphic lineGr = new VectorGraphic(3, Color.ORANGE, true);
                        Line2D ln = (Line2D) tagVal3.get(i);
                        lineGr.createShape(ln);
                        abstractLayer.addGraphic(lineGr);
                    }
                }

                DotGraphic dot = new DotGraphic(4, Color.red, true);
                dot.createShape((Point2D.Double) getGroupID().getTagValue(TagO.POINT_GR));
                abstractLayer.addGraphic(dot);

                VectorGraphic lineGraphic = new VectorGraphic(2, Color.RED, true);
                Line2D tagValue = (Line2D) getGroupID().getTagValue(TagO.RESULTANT);
                lineGraphic.createShape(tagValue);
                abstractLayer.addGraphic(lineGraphic);

                view.repaint();
            }
        }
    }

    /**
     * Set Print Preview with given pageformat.
     *
     * @param pformat page format
     */
    public void setPrintPreview(final PageFormat pformat) {
        if (pformat == null) {
            return;
        }

        //use paper size for size, imageable for infolayer border.
        final double imageableHeight = pformat.getHeight();
        final double imageableWidth = pformat.getWidth();

        double scaleFactor = Math.min(
                getSize().width / imageableWidth,
                getSize().height / imageableHeight);
        if (scaleFactor == 0) {
            scaleFactor = 1;
        }
        final int newWidth = (int) (imageableWidth * scaleFactor);
        final int newHeight = (int) (imageableHeight * scaleFactor);

        removeAll();

        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        grid.setPreferredSize(new Dimension(newWidth, newHeight));
        grid.setMaximumSize(new Dimension(newWidth, newHeight));
        grid.setMinimumSize(new Dimension(newWidth, newHeight));
        grid.setSize(newWidth, newHeight);

        grid.setBackground(Color.white);

        add(grid);

        //estimates margin
        double margin = pformat.getImageableX();
        view2ds.get(0).getInfoLayer().setBorder((int) (margin * scaleFactor));

        repaint();
    }

    private void printReport() {
        final String contentRole
                = (String) getGroupID().getTagValue(TagO.SERIE_ROLE);
        if (OrthoExplorerView.REPORT_IMAGE.equalsIgnoreCase(
                contentRole)) {
            OrthoEventManager.getInstance().getPrintAset();
            ReportPrintable printable = new ReportPrintable(this,
                    OrthoEventManager.getInstance().getPrintAset());
            printable.print();
        }
    }

    /**
     * Overriden to dispose viewers (if they don't dispose, don't store actions
     * in tag.
     */
    @Override
    public void close() {
        for (DefaultView2d<ImageElement> view : view2ds) {
            view.dispose();
        }
        super.close();
    }

    private boolean isRelatedToGroupID(Object newVal) {
        if (newVal instanceof MediaSeriesGroup) {
            OrthodonticModel model = (OrthodonticModel) OrthoExplorerView.getService().getDataExplorerModel();
            MediaSeriesGroup parent = model.getParent(
                    (MediaSeriesGroup) newVal, OrthodonticModel.patient);
            MediaSeriesGroup groupIDParent = model.getParent(
                    getGroupID(), OrthodonticModel.patient);
            if (parent.equals(groupIDParent)) {
                return true;
            }
        }
        return false;
    }

    public void setTitlePrefix(String prefix) {
        titlePrefix = prefix;
    }

    /**
     * Sets Plugin name from groupID.
     */
    public void setPluginName() {
        final MediaSeriesGroup groupID = getGroupID();
        if (groupID != null) {
            final Object tagValue = groupID.getTagValue(TagW.StudyDescription);
            setPluginName(tagValue.toString());
        }
    }

    @Override
    public void setPluginName(String name) {
        name = titlePrefix.concat(name);
        if (name.length() > 30) {
            setToolTipText(name);
            name = name.substring(0, 30);
            name = name.concat("...");
        }
        super.setPluginName(name);
    }

    @Override
    public List<SynchView> getSynchList() {
        return null;
    }

    @Override
    public List<GridBagLayoutModel> getLayoutList() {
        return null;
    }

}
