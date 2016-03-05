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

import com.orthodonticpreview.datamodel.OrthodonticModel;
import com.orthodonticpreview.ui.HelpDialog;
import com.orthodonticpreview.ui.OrthodonticDataExtractor;
import com.orthodonticpreview.ui.OrthodonticWin;
import com.orthodonticpreview.ui.cephalometrics.CephalometricsContainer;
import com.orthodonticpreview.ui.internal.Messages;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.JMVUtils;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.ui.docking.UIManager;
import org.weasis.core.ui.editor.image.ViewerPlugin;

/**
 * Countains steps and the utils panel.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 20 Jul
 */
public class WizzardPanel extends JPanel {

    public WizzardPanel() {
        initGUI();
    }

    private void initGUI() {
        setLayout(new GridBagLayout());

        final GridBagConstraints stepsConst = new GridBagConstraints();
        stepsConst.anchor = GridBagConstraints.PAGE_START;
        stepsConst.insets = new Insets(8, 2, 8, 2);
        stepsConst.fill = GridBagConstraints.BOTH;
        stepsConst.weightx = 1;
        stepsConst.weighty = 1;
        int line = stepsConst.gridx = stepsConst.gridy = 0;
        int step = 1;

        add(createPatInfoStep(step), stepsConst);

        stepsConst.gridy = ++line;
        add(createImgStep(++step), stepsConst);

        stepsConst.gridy = ++line;
        add(createArcStep(++step), stepsConst);

        stepsConst.gridy = ++line;
        add(createTeethMeasureStep(++step), stepsConst);

        stepsConst.gridy = ++line;
        add(createCalcStep(++step), stepsConst);

        stepsConst.gridy = ++line;
        add(createRepStep(++step), stepsConst);

        stepsConst.gridy = ++line;
        add(createFilePanel(++step), stepsConst);

    }

    public void changePatient(final MediaSeriesGroup patient) {
        for (Component component : getComponents()) {
            if (component instanceof AbstractWizzardStep) {
                ((AbstractWizzardStep) component).setPatient(patient);
            }
        }
    }

    public static void openCephalometrics(MediaSeriesGroup patient) {

        if (patient == null) {
            return;
        }

        //1- ver se já tem outra janela do mesmo aberta, se sim, focus
        synchronized (UIManager.VIEWER_PLUGINS) {
            for (int i = UIManager.VIEWER_PLUGINS.size() - 1; i >= 0; i--) {
                ViewerPlugin plugin = UIManager.VIEWER_PLUGINS.get(i);
                if (patient.equals(plugin.getGroupID())
                        && plugin instanceof CephalometricsContainer) {
                    plugin.setSelectedAndGetFocus();
                    return;
                }
            }
        }

        //factory.create seriesviewer
        CephalometricsContainer container = new CephalometricsContainer();

        //add name, series, register, etc.
        container.setGroupID(patient);
        OrthoExplorerView.getService().getDataExplorerModel()
                .addPropertyChangeListener(container);

        //TODO tambem devia trocar o nome quando edita nome do paciente.
        String title = "[Cephalo] - "
                + (String) patient.getTagValue(TagW.PatientName);
        if (title.length() > 30) {
            container.setToolTipText(title);
            title = title.substring(0, 30);
            title = title.concat("...");
        }
        container.setPluginName(title);

        OrthodonticWin.getInstance().registerPlugin(container);
        container.setSelectedAndGetFocus();

        container.setOwner(patient);
        container.setSelected(true);

    }

    private AbstractWizzardStep createPatInfoStep(int step) {
        return new AbstractWizzardStep(
                Messages.getString("WizzardStep.infoStepTitle"), step) {

                    private JLabel patIdLabel;
                    private JLabel patNameLabel;
                    private MediaSeriesGroup selObj;

                    @Override
                    public void addContent() {

                        setLayout(new GridBagLayout());

                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.gridx = gbc.gridy = 0;
                        gbc.anchor = GridBagConstraints.WEST;
                        gbc.insets = new Insets(5, 5, 5, 5);

                        JLabel id = new JLabel("ID:");
                        id.setFont(FontTools.getFont12Bold());
                        add(id, gbc);

                        patIdLabel = new JLabel();
                        patIdLabel.setFont(FontTools.getFont11());
                        gbc.gridx = 1;
                        add(patIdLabel, gbc);

                        gbc.gridx = 2;
                        gbc.anchor = GridBagConstraints.NORTHEAST;
                        add(status, gbc);

                        gbc.gridy = 1;
                        gbc.gridx = 0;
                        gbc.anchor = GridBagConstraints.WEST;
                        JLabel name = new JLabel(Messages.getString("WizzardStep.name"));
                        name.setFont(FontTools.getFont12Bold());
                        add(name, gbc);

                        patNameLabel = new JLabel();
                        patNameLabel.setFont(FontTools.getFont11());
                        gbc.gridx = 1;
                        gbc.gridwidth = 2;
                        add(patNameLabel, gbc);

                        JButton editButton = AbstractWizzardStep.makeLinkButton(
                                Messages.getString("WizzardStep.edit"));
                        editButton.setFont(FontTools.getFont12());
                        editButton.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (selObj != null) {
                                    //obtem paciente, mesmo que seja o estudo o selected:
                                    OrthodonticModel model
                                    = (OrthodonticModel) OrthoExplorerView
                                    .getService().getDataExplorerModel();
                                    MediaSeriesGroup editGroup = model.getParent(selObj,
                                            OrthodonticModel.patient);

                                    //editar paciente
                                    JDialog createOP = new CreateOPDialog(
                                            OrthodonticWin.getInstance(), editGroup);
                                    JMVUtils.showCenterScreen(createOP);
                                }
                            }
                        });
                        gbc.gridy = 3;
                        gbc.gridx = 2;
                        gbc.gridwidth = 1;
                        gbc.anchor = GridBagConstraints.EAST;
                        add(editButton, gbc);

                    }

                    @Override
                    public void setPatient(MediaSeriesGroup group) {
                        if (group == null) {
                            patIdLabel.setText("");
                            patNameLabel.setText("");
                            selObj = null;
                            setStatus(ST_NONE);
                        } else {
                            selObj = group;
                            final String id = (String) group.getTagValue(TagW.PatientID);
                            String name = (String) group.getTagValue(TagW.PatientName);
                            patIdLabel.setText(id);
                            if (name.length() > 16) {
                                patNameLabel.setToolTipText(name);
                                name = name.substring(0, 16);
                                name = name.concat("...");
                            }
                            patNameLabel.setText(name);

                            if (!isBlankOrNull(id) && !isBlankOrNull(name)) {
                                setStatus(ST_COMPLETE);
                            }
                        }
                    }
                };
    }

    private AbstractWizzardStep createImgStep(int step) {
        return new AbstractWizzardStep(
                Messages.getString("WizzardStep.imageStepTitle"), step) {

                    private final String ADD_IMAGE
                    = Messages.getString("WizzardStep.NOImage");

                    private JButton openButton;
                    private MediaSeries serie;

                    @Override
                    public void setPatient(MediaSeriesGroup group) {

                        if (group != null
                        && group.getTagID().equals(TagW.StudyInstanceUID)) {
                            serie = OrthoExplorerView.getService().getImageSerie(group);
                            if (serie != null) {
                                String tagValue
                                = serie.getTagValue(TagW.FilePath).toString();
                                if (tagValue.length() > 28) {
                                    tagValue = tagValue.substring(
                                            tagValue.length() - 28);
                                    tagValue = "..." + tagValue;
                                }
                                openButton.setText(tagValue);
                                openButton.setEnabled(true);
                                setStatus(AbstractWizzardStep.ST_COMPLETE);
                            }
                        } else {
                            serie = null;
                            openButton.setText(ADD_IMAGE);
                            openButton.setEnabled(false);
                            setStatus(AbstractWizzardStep.ST_INCOMPLETE);
                        }
                        if (group == null) {
                            setStatus(ST_NONE);
                        }
                    }

                    @Override
                    public void addContent() {

                        setLayout(new GridBagLayout());

                        final GridBagConstraints gbc = new GridBagConstraints();
                        gbc.gridx = gbc.gridy = 0;
                        gbc.anchor = GridBagConstraints.NORTHEAST;
                        gbc.insets = new Insets(0, 5, 5, 5);

                        add(status, gbc);

                        openButton = AbstractWizzardStep.makeLinkButton(
                                Messages.getString("WizzardStep.openImage"));
                        openButton.setToolTipText(
                                Messages.getString("WizzardStep.openImage"));
                        openButton.setFont(FontTools.getFont11());
                        openButton.setEnabled(false);
                        openButton.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent event) {
                                openImage();
                            }
                        });
                        gbc.gridy = 1;
                        gbc.anchor = GridBagConstraints.WEST;
                        add(openButton, gbc);

                    }

                    private void openImage() {
                        //Ver se ja está aberta...
                        ViewerPlugin viewer
                        = OrthoExplorerView.getService().getViewer(serie);
                        if (viewer != null) {
                            viewer.setSelectedAndGetFocus();
                            return;
                        }

                        OrthoExplorerView.getService().openInDefaultPlugin(serie);
                    }
                };
    }

    private AbstractWizzardStep createArcStep(int step) {
        return new AbstractWizzardStep(
                Messages.getString("WizzardStep.arcStepTitle"), step) {

                    private MediaSeries calcSeries;
                    private boolean contenOK = false;

                    @Override
                    public void setPatient(MediaSeriesGroup group) {
                        if (group != null) {
                            calcSeries = OrthoExplorerView.getService()
                            .getImageSerie(group);

                            if (!contenOK && calcSeries != null) {
                                addContent();
                            }
                        } else {
                            removeAll();
                            contenOK = false;
                        }
                    }

                    @Override
                    public void addContent() {
                        if (calcSeries != null) {
                            ViewerPlugin calcViewer
                            = OrthoExplorerView.getService().getViewer(calcSeries);

                            JButton button = null;
                            if (calcViewer != null) {
                                for (Object object : calcViewer.getToolPanel()) {
                                    if (object instanceof OrthodonticDataExtractor) {
                                        OrthodonticDataExtractor ext
                                        = (OrthodonticDataExtractor) object;
                                        button = ext.getButton(
                                                OrthodonticDataExtractor.ARC_TYPE_BUTTON);
                                    }
                                }
                            }

                            if (button != null) {
                                setLayout(new GridBagLayout());

                                GridBagConstraints gbc = new GridBagConstraints();
                                gbc.gridx = gbc.gridy = 0;
                                gbc.gridwidth = 2;
                                add(getArcStepLabel("31", 200, 55), gbc);

                                gbc.gridy = 1;
                                gbc.gridwidth = 2;
                                add(getArcStepLabel("32", 200, 60), gbc);

                                gbc.gridy = 2;
                                gbc.gridwidth = 2;
                                gbc.gridx = 0;
                                add(getArcStepLabel("33", 200, 60), gbc);

                                gbc.gridy = 3;
                                gbc.gridwidth = 1;
                                add(getArcStepLabel("34", 150, 45), gbc);
                                gbc.gridx = 1;
                                add(button, gbc);

                                contenOK = true;
                                return;
                            }
                        }
                    }

                    private JLabel getArcStepLabel(String sufix, int wid, int heig) {
                        JLabel label = new JLabel("<html>"
                                + Messages.getString("WizzardStep.arcStep" + sufix)
                                + "</html>");
                        label.setFont(FontTools.getFont11());
                        label.setMinimumSize(new Dimension(wid, heig));
                        label.setPreferredSize(new Dimension(wid, heig));
                        return label;
                    }

                };
    }

    private AbstractWizzardStep createTeethMeasureStep(int step) {
        return new AbstractWizzardStep(
                Messages.getString("WizzardStep.teethMesTitle"), step) {

                    @Override
                    public void setPatient(MediaSeriesGroup patient) {
                        //do nothing
                    }

                    @Override
                    public void addContent() {
                        JLabel label = new JLabel("<html>"
                                + Messages.getString("WizzardStep.teethMesLabel")
                                + "</html>");
                        label.setFont(FontTools.getFont11());
                        label.setMinimumSize(new Dimension(200, 45));
                        label.setPreferredSize(new Dimension(200, 45));
                        add(label);
                    }

                };
    }

    private AbstractWizzardStep createCalcStep(int step) {
        return new CalcStep(
                Messages.getString("WizzardStep.calcTitle"), step);
    }

    private AbstractWizzardStep createRepStep(int step) {
        ReportStep repStep = new ReportStep(++step);
        //nao pode ser aqui.
        //OrthoExplorerView.getService().addPropertyChangeListener(repStep);
        return repStep;
    }

    private AbstractWizzardStep createFilePanel(int step) {
        return new AbstractWizzardStep(null, ++step) {

            @Override
            public void setPatient(MediaSeriesGroup patient) {
                //do nothing
            }

            @Override
            public void addContent() {
                setLayout(new GridBagLayout());

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(2, 2, 2, 2);

                JButton saveAsButton = new JButton(
                        Messages.getString("OrthodonticWin.saveAs"));
                saveAsButton.setFont(FontTools.getFont12());
                saveAsButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        OrthoExplorerView.getService()
                                .saveSelectedPatient(true);
                    }
                });
                add(saveAsButton, gbc);

                JButton saveButton = new JButton(new ImageIcon(
                        WizzardPanel.class.getResource("/icon/16x16/save.png")));
                saveButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        OrthoExplorerView.getService()
                                .saveSelectedPatient(false);
                    }
                });
                gbc.gridx = 1;
                add(saveButton, gbc);

                JButton newButton = new JButton(
                        Messages.getString("OrthodonticWin.newMenu"));
                newButton.setFont(FontTools.getFont12());
                newButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JDialog createOP = new CreateOPDialog(
                                OrthodonticWin.getInstance().getFrame());
                        JMVUtils.showCenterScreen(createOP);
                    }
                });
                gbc.gridy = 1;
                gbc.gridx = 0;
                add(newButton, gbc);

                JButton openButton = new JButton(
                        Messages.getString("OrthodonticWin.open"));
                openButton.setFont(FontTools.getFont12());
                openButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        OrthoExplorerView.getService().openPatient();
                    }
                });
                gbc.gridx = 1;
                add(openButton, gbc);

                JButton closeOPButton = new JButton(
                        Messages.getString("OrthodonticWin.closeOP"));
                closeOPButton.setFont(FontTools.getFont12());
                closeOPButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent evt) {
                        final int ans = JOptionPane.showConfirmDialog(
                                OrthodonticWin.getInstance().getFrame(),
                                Messages.getString("WizzardPanel.confirmClose"),
                                Messages.getString("OrthodonticWin.closeOP"),
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (ans == JOptionPane.OK_OPTION) {
                            OrthoExplorerView.getService()
                                    .closeSelectedPatient();
                        }
                    }
                });
                gbc.gridx = 0;
                gbc.gridy = 2;
                add(closeOPButton, gbc);

                JButton helpButton = AbstractWizzardStep.makeLinkButton("Help");
                helpButton.setFont(FontTools.getFont12());
                helpButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showHelp();
                    }

                });
                gbc.gridx = 1;
                gbc.gridy = 2;
                add(helpButton, gbc);
            }

        };
    }

    private void showHelp() {
//        JOptionPane.showMessageDialog(
//                OrthodonticWin.getInstance().getFrame(), "help text!");
        HelpDialog helpDialog = new HelpDialog(OrthodonticWin.getInstance().getFrame());
        JMVUtils.showCenterScreen(helpDialog, OrthodonticWin.getInstance().getFrame());
    }

    private static class CalcStep extends AbstractWizzardStep
            implements PropertyChangeListener {

        private MediaSeriesGroup study;
        private boolean listenerFlag = false;

        public CalcStep(String string, int step) {
            super(string, step);
        }

        @Override
        public void setPatient(MediaSeriesGroup group) {
            if (group != null
                    && group.getTagID().equals(TagW.StudyInstanceUID)) {
                study = group;
                if (AbstractWizzardStep.hasCalculation(group)) {
                    setStatus(AbstractWizzardStep.ST_COMPLETE);
                } else {
                    setStatus(AbstractWizzardStep.ST_NONE);
                }
            } else if (group == null) {
                study = null;
                setStatus(ST_NONE);
            }

            if (!listenerFlag) {
                //add as listener on first "setPatient"
                OrthodonticModel model = (OrthodonticModel) OrthoExplorerView.getService().getDataExplorerModel();
                model.addPropertyChangeListener(this);
                listenerFlag = true;
            }
        }

        @Override
        public void addContent() {
            JButton calcButton = new JButton(
                    Messages.getString("WizzardStep.calcButton"));
            calcButton.setFont(FontTools.getFont12());
            calcButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (study != null) {
                        new CalcPreview(OrthoExplorerView.getService()
                                .getImageSerie(study));
                    }
                }
            });

            add(calcButton);

            add(status);

        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt instanceof ObservableEvent) {
                ObservableEvent obs = (ObservableEvent) evt;
                if (ObservableEvent.BasicAction.Update.equals(
                        obs.getActionCommand()) && study != null) {
                    if (obs.getSource().equals(OrthoExplorerView.getService()
                            .getImageSerie(study))
                            && obs.getNewValue() == null) {
                        setStatus(AbstractWizzardStep.ST_COMPLETE);
                    } else if (obs.getNewValue() instanceof Exception) {
                        setStatus(AbstractWizzardStep.ST_INCOMPLETE);
                    }
                }
            }
        }
    }

}
