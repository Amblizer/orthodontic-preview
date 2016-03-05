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
package com.orthodonticpreview.ui;

import com.orthodonticpreview.ui.internal.Messages;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.weasis.core.api.gui.util.JMVUtils;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2016, 07 Feb
 */
public class HelpDialog extends JDialog {

    private static final Dimension SIZE = new Dimension(500, 200);
    
    public HelpDialog(Frame owner) {
        super(owner, true);
        
        setMinimumSize(SIZE);
        initGUI();
    }

    private void initGUI() {
        JTextPane msgPanel = new JTextPane();
        msgPanel.setPreferredSize(SIZE);
        msgPanel.setMinimumSize(SIZE);
        msgPanel.setContentType("text/html");
        msgPanel.setEditable(false);
        
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet ss = kit.getStyleSheet();
        ss.addRule("body {font-family:sans-serif;font-size:12pt;background-color:#"
            + "margin:3;font-weight:normal;}");
        msgPanel.setEditorKit(kit);
        msgPanel.addHyperlinkListener(JMVUtils.buildHyperlinkListener());
        
        final StringBuilder message = new StringBuilder("<div align=\"center\"><H2>");
        message.append("Orthodontic Preview</H2>");
        message.append("<a href=\"http://orthodonticpreview.com\">orthodonticpreview.com</a>");
        
        message.append("<p>Orthodontic Preview ");
        message.append(Messages.getString("HelpDialog.wasDev"));
        message.append("<br />Cesar Moreira ");
        message.append(Messages.getString("HelpDialog.and"));
        message.append(" <a href=\"https//animati.com.br\">Animati Computação Aplicada</a>.</p>");
        
        message.append("<p>");
        message.append(Messages.getString("HelpDialog.notClinical"));
        message.append("</p>");
        message.append("<p>Orthodontic Preview ");
        message.append(Messages.getString("HelpDialog.using"));
        message.append(" (<a href=\"https://dcm4che.atlassian.net/wiki/display/WEA/Home\">Weasis-framework</a>).</p>");
        
        msgPanel.setText(message.toString());
        
        add(msgPanel);
        
        pack();
    }
    
    
    
}
