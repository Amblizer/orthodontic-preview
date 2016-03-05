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
import com.orthodonticpreview.ui.OrthodonticWin;
import com.orthodonticpreview.ui.internal.Messages;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.ui.editor.image.ViewerPlugin;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version
 */
public class ReportStep extends AbstractWizzardStep
        implements PropertyChangeListener {

    private MediaSeriesGroup groupSelected;
    private PrintRequestAttributeSet aset
            = new HashPrintRequestAttributeSet();

    private boolean addListener;

    public ReportStep(int step) {
        super(null, step);
        addListener = true;
    }

    @Override
    public void setPatient(MediaSeriesGroup group) {
        groupSelected = group;
        if (addListener) {
            OrthoExplorerView.getService().getDataExplorerModel()
                    .addPropertyChangeListener(this);
            addListener = false;
        }
    }

    @Override
    public void addContent() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 0);

        JButton cephalometButton = new JButton(
                Messages.getString("WizzardStep.cephAnalisys"));
        cephalometButton.setFont(FontTools.getFont12());
        cephalometButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                OrthodonticModel model = (OrthodonticModel) OrthoExplorerView.getService().getDataExplorerModel();
                MediaSeriesGroup parent = model.getParent(
                        groupSelected, OrthodonticModel.patient);
                WizzardPanel.openCephalometrics(parent);
            }
        });

        add(cephalometButton, gbc);

        JButton reportButton = new JButton(
                Messages.getString("WizzardStep.report"));
        reportButton.setFont(FontTools.getFont12());
        reportButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (groupSelected != null) {
                    showStudyReport(groupSelected);
                }
            }
        });

        gbc.gridy = 1;
        gbc.insets = new Insets(15, 0, 2, 0);
        add(reportButton, gbc);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof ObservableEvent) {
            ObservableEvent event = (ObservableEvent) evt;
            if (ObservableEvent.BasicAction.Select.equals(
                    event.getActionCommand())
                    && event.getSource() instanceof ViewerPlugin) {
                ViewerPlugin plugin = (ViewerPlugin) event.getSource();

                //clear
                removeAll();
                addContent();
                List<Action> printActions = plugin.getPrintActions();
                if (printActions != null && !printActions.isEmpty()) {

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridx = 0;
                    gbc.gridy = 2;
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.insets = new Insets(2, 0, 2, 0);

                    for (Action action : printActions) {
                        JButton button = new JButton(action);
                        button.setFont(FontTools.getFont12());
                        add(button, gbc);
                        gbc.gridy++;
                    }
                }
                revalidate();
            }
        }
    }

    private void showStudyReport(MediaSeriesGroup groupSelected) {
        OrthoExplorerView service
                = OrthoExplorerView.getService();

        //tem um relatorio para este paciente?
        Series patientReport = (Series) OrthoExplorerView.getService()
                .getReportSerie(groupSelected);
        ViewerPlugin viewer = null;
        if (patientReport != null) {
            viewer = service.getViewer(patientReport);
            //remove old rep serie if exists:
            DataExplorerModel dataExplorerModel
                    = service.getDataExplorerModel();
            if (dataExplorerModel instanceof OrthodonticModel) {
                ((OrthodonticModel) dataExplorerModel).removeSeries(patientReport);
            }
        }

        //encontra a serie que tem o calculo
        final Series calcSeries = (Series) OrthoExplorerView.getService()
                .getImageSerie(groupSelected);

        if (AbstractWizzardStep.hasCalculation(groupSelected)) {
            try {
                MediaSeries reportSerie
                        = service.buildReport(calcSeries);
                if (viewer == null) {
                    service.openInDefaultPlugin(reportSerie);
                } else {
                    viewer.addSeries(reportSerie);
                    viewer.setGroupID(reportSerie);
                    viewer.setSelectedAndGetFocus();
                }
            } catch (Exception ex) {
                showRepError(Messages.getString(
                        "WizzardStep.reportError") + ex);
            }
        } else {
            showRepError(Messages.getString(
                    "WizzardStep.reportErrorNoCalc"));
        }
    }

    private void showRepError(final String errorMsg) {
        JOptionPane.showMessageDialog(
                OrthodonticWin.getInstance().getFrame(), errorMsg,
                Messages.getString("WizzardStep.reportErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }
}
