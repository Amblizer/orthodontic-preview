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

import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.weasis.core.api.gui.util.JMVUtils;

/**
 * Code base for Cephalometric parameters, and GUI to edit them.
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 5 Nov.
 */
public abstract class AbstractCephParameter implements DocumentListener {

    protected final String title;
    protected double value;
    protected JTextField field;
    private JLabel resLabel;

    public AbstractCephParameter(String name) {
        title = name;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the value
     */
    public double getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(double value) {
        this.value = value;
    }

    public abstract String getConclusion();

    public JPanel getParameterPanel() {
        JPanel panel = new JPanel();
        GridLayout gridLayout = new GridLayout(2, 1, 10, 10);
        panel.setLayout(gridLayout);

        Border loweredetched = BorderFactory.createEtchedBorder(
                EtchedBorder.LOWERED);
        TitledBorder borderTitle = BorderFactory.createTitledBorder(
                loweredetched, title);

        panel.setBorder(borderTitle);

        field = new JTextField();
        JMVUtils.setPreferredWidth(field, 200, 200);
        field.getDocument().addDocumentListener(this);
        panel.add(field);

        resLabel = new JLabel("...");
        panel.add(resLabel);

        return panel;
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        updateResult();
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        updateResult();
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        updateResult();
    }

    private void updateResult() {
        String text = field.getText();
        try {
            double parseDouble = Double.parseDouble(text);
            setValue(parseDouble);
            resLabel.setText(getConclusion());
        } catch (NumberFormatException ex) {
            resLabel.setText("...");
        }
    }

}
