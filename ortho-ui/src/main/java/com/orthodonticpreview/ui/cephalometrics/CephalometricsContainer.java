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
package com.orthodonticpreview.ui.cephalometrics;

import com.orthodonticpreview.datamodel.TagO;
import com.orthodonticpreview.ui.internal.Messages;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.JMVUtils;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.ui.docking.DockableTool;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.util.Toolbar;
import org.weasis.core.ui.util.WtoolBar;

/**
 * Container for Cephalometric Analisis fields.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 5 Nov.
 */
public class CephalometricsContainer extends ViewerPlugin
        implements PropertyChangeListener {

    private static final Dictionary<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();

    static {
        labelTable.put(-20, new JLabel("2.0"));
        labelTable.put(-10, new JLabel("1.0"));
        labelTable.put(0, new JLabel("0.0"));
        labelTable.put(10, new JLabel("1.0"));
        labelTable.put(20, new JLabel("2.0"));
    }

    /**
     * Owner group. Can be of any kind, but usualy patient.
     */
    private MediaSeriesGroup owner;
    private List<CephParameter> cpList;

    public static final List<Toolbar> TOOLBARS
            = Collections.synchronizedList(new ArrayList<Toolbar>());
    private SliderPanel supSliderPanel;
    private SliderPanel infSliderPanel;

    public CephalometricsContainer() {
        super("Cephalometrics");

        setLayout(new FlowLayout());
        initGui();
    }

    @Override
    public List<Action> getExportActions() {
        return null;
    }

    @Override
    public List<Action> getPrintActions() {
        return null;
    }

    @Override
    public List<MediaSeries<MediaElement>> getOpenSeries() {
        return new ArrayList<MediaSeries<MediaElement>>();
    }

    @Override
    public void addSeries(MediaSeries ms) {
        //empty
    }

    @Override
    public void removeSeries(MediaSeries ms) {
        //nothing
    }

    @Override
    public JMenu fillSelectedPluginMenu(JMenu menu) {
        return null;
    }

    @Override
    public List<Toolbar> getToolBar() {
        return TOOLBARS;
    }

    @Override
    public WtoolBar getStatusBar() {
        return null;
    }

    @Override
    public List<DockableTool> getToolPanel() {
        return null;
    }

    @Override
    public void setSelected(boolean selected) {
        //nothing
    }

    private void initGui() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        final JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        //cephalometric panel ......................................
        final JPanel cephPanel = new JPanel(new GridLayout(4, 4));
        cephPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                Messages.getString("CephalometricsContainer.title")));

        cpList = CephParameter.getCPList();
        for (CephParameter cephParameter : cpList) {
            cephPanel.add(cephParameter.getParameterPanel());
        }
        main.add(cephPanel);
        //.........................................................

        //sliders panel ...........................................
        final JPanel slidersPanel = new JPanel();
        slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.Y_AXIS));
        slidersPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                Messages.getString("CephalometricsContainer.mid_line")));

        slidersPanel.add(new JLabel(Messages.getString("CephalometricsContainer.sup")));
        supSliderPanel = new SliderPanel(TagO.MED_LINE_SUP);
        slidersPanel.add(supSliderPanel);

        slidersPanel.add(new JLabel(Messages.getString("CephalometricsContainer.inf")));
        infSliderPanel = new SliderPanel(TagO.MED_LINE_INF);
        slidersPanel.add(infSliderPanel);
        //.........................................................

        main.add(slidersPanel);

        final JScrollPane jScrollPane = new JScrollPane(main,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(jScrollPane);

        validate();

    }

    public void setOwner(MediaSeriesGroup group) {
        if (group != null) {
            this.owner = group;
            for (CephParameter cephParameter : cpList) {
                cephParameter.setOwnerGroup(group);
            }

            Object tagValue = owner.getTagValue(TagO.MED_LINE_SUP);
            if (tagValue instanceof Double) {
                int value = (int) Math.round(((Double) tagValue) * 10);
                supSliderPanel.setValue(value);
            }

            tagValue = owner.getTagValue(TagO.MED_LINE_INF);
            if (tagValue instanceof Double) {
                int value = (int) Math.round(((Double) tagValue) * 10);
                infSliderPanel.setValue(value);
            }

        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof ObservableEvent) {
            ObservableEvent obsEvt = (ObservableEvent) evt;
            ObservableEvent.BasicAction action = obsEvt.getActionCommand();
            Object newVal = obsEvt.getNewValue();
            if (ObservableEvent.BasicAction.Remove.equals(action)) {
                if (newVal instanceof MediaSeriesGroup) {
                    MediaSeriesGroup pat = (MediaSeriesGroup) newVal;
                    String tagValue
                            = pat.getTagValue(TagW.PatientID).toString();
                    if (tagValue != null && tagValue.equals(
                            getGroupID().getTagValue(TagW.PatientID))) {
                        close();
                    }
                }
            }
        }
    }

    private class SliderPanel extends JPanel {

        private final TagW lineTag;
        private final JSlider slider;

        private SliderPanel(TagW tag) {
            lineTag = tag;

            add(new JLabel(Messages.getString("CephalometricsContainer.left")));

            slider = new JSlider(-20, 20, 0);

            //labels
            slider.setLabelTable(labelTable);

            slider.setPaintLabels(true);
            slider.setMajorTickSpacing(10);
            slider.setMinorTickSpacing(1);

            slider.setPaintTicks(true);

            slider.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    if (owner != null) {
                        owner.setTag(lineTag, slider.getValue() / (double) 10);
                    }
                }
            });

            //must be after adding thicks and labels
            JMVUtils.setPreferredWidth(slider, 400, 400);

            add(slider);
            add(new JLabel(Messages.getString("CephalometricsContainer.right")));
        }

        private void setValue(int value) {
            slider.setValue(value);
        }
    }

}
