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
import com.orthodonticpreview.ui.internal.Messages;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.geom.Point2D;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.FontTools;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, jul 26.
 */
public abstract class AbstractWizzardStep extends JPanel {

    private final String stepTitle;
    protected JLabel status;
    private int step;

    public static final int ST_NONE = 2;
    public static final int ST_INCOMPLETE = 0;
    public static final int ST_COMPLETE = 1;
    private static final String[] ST_LABELS = new String[]{
        Messages.getString("WizzardStep.stIncomplete"),
        Messages.getString("WizzardStep.stComplete"),
        ""
    };

    private static final ImageIcon READY_ICON = new ImageIcon(
            AbstractWizzardStep.class.getResource("/icon/32x32/ready.png"));
    private static final ImageIcon UNREADY_ICON = new ImageIcon(
            AbstractWizzardStep.class.getResource("/icon/32x32/not-ready.png"));
    private static final ImageIcon[] ST_ICONS = new ImageIcon[]{
        UNREADY_ICON, READY_ICON, null};

    public AbstractWizzardStep(final String title, final int stepNumber) {
        stepTitle = title;
        step = stepNumber;
        initGUI();
    }

    private void initGUI() {

        Font useFont = FontTools.getFont11();

        if (stepTitle != null) {
            TitledBorder title = BorderFactory.createTitledBorder(
                    step + " - " + stepTitle);
            title.setTitleFont(useFont);
            setBorder(title);
        }

        status = new JLabel();
        addContent();

    }

    public void setStatus(int newStatus) {
        status.setIcon(ST_ICONS[newStatus]);
        status.setToolTipText(ST_LABELS[newStatus]);
    }

    public abstract void setPatient(MediaSeriesGroup patient);

    public abstract void addContent();

    public static JButton makeLinkButton(String text) {
        JButton button = new JButton("<html><u>" + text + "</u></html");
        button.setForeground(Color.blue);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        return button;
    }

    public static boolean isBlankOrNull(String string) {
        return string == null || string.isEmpty() || string.trim().isEmpty();
    }

    public static boolean hasCalculation(MediaSeriesGroup group) {
        if (group.getTagID().equals(TagW.StudyInstanceUID)) {
            MediaSeries imageSerie
                    = OrthoExplorerView.getService().getImageSerie(group);
            if (imageSerie != null) {
                Object tagValue = imageSerie.getTagValue(TagO.POINT_GR);
                if (tagValue instanceof Point2D) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

}
