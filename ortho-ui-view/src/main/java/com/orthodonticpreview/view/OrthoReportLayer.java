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
import com.orthodonticpreview.ui.cephalometrics.CephParameter;
import com.orthodonticpreview.ui.explorer.OrthoExplorerView;
import com.orthodonticpreview.view.internal.Messages;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.FontTools;
import org.weasis.core.ui.editor.image.AnnotationsLayer;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.PixelInfo;
import org.weasis.core.ui.graphic.model.AbstractLayer;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 12 Oct.
 */
public class OrthoReportLayer implements AnnotationsLayer {

    /** Class Logger. */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(OrthoReportLayer.class);

    /** Display prefferences (what to show). */
    private final HashMap<String, Boolean> displayPreferences =
            new HashMap<String, Boolean>();
    /** Controls visibility of this layer. */
    private boolean visible = true;
    /** Owner viewer. */
    private final DefaultView2d view2DPane;
    /** Info layer margin. */
    private int border = 10;
    /** Report header name. */
    public static final String REPORT_HEAD = "Report Header";
    /** Report result name. */
    public static final String REPORT_RESULT = "Report Result";
    /** Caption name. */
    public static final String CAPTION = "Caption";
    /** Footer name. */
    public static final String REPORT_FOOTER = "Footer";

    /** Cephalometric tags for results. */
    private static final TagW[] CEPH_TAGS = new TagW[] {
        TagO.ANB, TagO.FAC_ANGLE, //Mandibula: usar somente FacAngle.
        TagO.SNA, //maxila normal, protruida, retruida
    };

    private static final String MANDIBLE = "Mandible";

    /** OP image for header. */
    public static final URL LOGO_IMAGE =
            OrthoReportLayer.class.getResource("/image/ortho-logo.png");
    /** Cor azul de acordo com a ID visual (R0 G152 e B218). */
    private static final Color opBlueColor = new Color(0, 152, 218);

    /**
     * Creates a new report-layer for given view
     * @param view View to own the layer.
     */
    public OrthoReportLayer(DefaultView2d view) {

        view2DPane = view;
        displayPreferences.put(REPORT_HEAD, true);
        displayPreferences.put(REPORT_RESULT, true);
        displayPreferences.put(CAPTION, true);
        displayPreferences.put(REPORT_FOOTER, true);
    }

    @Override
    public boolean getDisplayPreferences(final String item) {
        Boolean val = displayPreferences.get(item);
        if (val == null) {
            val = false;
        }
        return val;
    }

    @Override
    public boolean setDisplayPreferencesValue(
            final String displayItem, final boolean selected) {

        final boolean wasSelected = getDisplayPreferences(displayItem);
        displayPreferences.put(displayItem, selected);
        return selected != wasSelected;
    }

    @Override
    public Rectangle getPreloadingProgressBound() {
       return new Rectangle();
    }

    @Override
    public Rectangle getPixelInfoBound() {
        return new Rectangle();
    }

    @Override
    public void setPixelInfo(PixelInfo pixelInfo) {
        //nothing
    }

    @Override
    public int getBorder() {
        return border;
    }

    @Override
    public void setBorder(final int border) {
        this.border = border;
    }

    @Override
    public void paint(final Graphics2D g2d) {
        final ImageElement image = view2DPane.getImage();
        if (!visible || image == null) {
            return;
        }

        final Rectangle bound = view2DPane.getBounds();
        float midx = bound.width / 2f;
        float midy = bound.height / 2f;

        g2d.setPaint(Color.white);

        final float fontHeight = FontTools.getAccurateFontHeight(g2d);

        if (!image.isReadable()) {
            String message = "Cannot read this media!";
            float y = midy;
            g2d.drawString(message,
                    (int) (midx - g2d.getFontMetrics().stringWidth(message)
                    / (double) 2), y);
            
        }

        //get patient:
        OrthodonticModel model = (OrthodonticModel) OrthoExplorerView
                .getService().getDataExplorerModel();
        MediaSeriesGroup patient = model.getParent(
                view2DPane.getSeries(), OrthodonticModel.patient);

        if (getDisplayPreferences(REPORT_HEAD)) {
            drawReportHead(g2d, fontHeight, patient);
        }

        final int resultFirstY = (int) (view2DPane.getBounds().height
                / (double) 4 * 2.4F);
        int captionHeight = bound.height / 5; //chute, por enquanto
        final int captionFirstY = resultFirstY - captionHeight;

        if (getDisplayPreferences(CAPTION)) {
            drawCaption(g2d, fontHeight, captionFirstY);
        }

        if (getDisplayPreferences(REPORT_RESULT)) {
            drawReportResults(g2d, fontHeight, resultFirstY, patient);
        }

        if (getDisplayPreferences(REPORT_FOOTER)) {
            drawFooter(g2d, fontHeight);
        }

    }

    /**
     * Draw the report header.
     * @param g2d Graphics to draw on.
     * @param fontHeight height of font.
     */
    private void drawReportHead(final Graphics2D g2d, final float fontHeight,
            final MediaSeriesGroup patient) {

        float lineStep = fontHeight * 1.5F;
        float line = border + fontHeight;
        final MediaSeries series = view2DPane.getSeries();
        g2d.setPaint(Color.BLACK);

        String str = Messages.getString("OrthoInfoLayer.Patient")
                + " " + patient.getTagValue(TagW.PatientName);
        g2d.drawString(str, border, line);

        //a data é da série
        str = Messages.getString("OrthoInfoLayer.Date")
                + " " + TagW.formatDate(
                (Date) series.getTagValue(TagW.SeriesDate));
        line += lineStep;
        g2d.drawString(str, border, line);
        
        final Rectangle bound = view2DPane.getBounds();

        str = Messages.getString("OrthoReportLayer."
                + getReportRole(view2DPane.getSeries()));
        g2d.drawString(str, bound.width / 3, line);

        Object ageValue = patient.getTagValue(TagO.PATIENT_AGE);
        if (ageValue instanceof String) {
            str = Messages.getString("OrthoReportLayer.Age")
                    + " " + ((String) ageValue);
            line += lineStep;
            g2d.drawString(str, border, line);
        }
        

        Object sexValue = patient.getTagValue(TagW.PatientSex);
        if (sexValue instanceof String && !"".equals(sexValue)) {
            str = Messages.getString("OrthoReportLayer.Sex")
                    + " " + ((String) sexValue);
            g2d.drawString(str, bound.width / 3, line);
        }      
        
        try {
            Image logo = ImageIO.read(LOGO_IMAGE);
            double logoReductionFactor = (bound.width / (double) 3)
                    / (double) logo.getWidth(null);
            double logoWid = logo.getWidth(null) * logoReductionFactor;
            AffineTransform aff = AffineTransform.getTranslateInstance(
                    bound.width - border - logoWid, border);
            aff.scale(logoReductionFactor, logoReductionFactor);
            g2d.drawImage(logo, aff, null);
        } catch (IOException ex) {
            LOGGER.error("Can't print logo: " + ex);
        }

    }

    private void drawCaption(final Graphics2D g2d, final float fontHeight,
            final int firstY) {
        float lineStep = fontHeight * 1.2F;
        //onde colocar?
        //x - à direita da imagem, começando 2/3 do inicio
        //y - logo acima do inicio da altura do resultado.

        final Rectangle bound = view2DPane.getBounds();
        float xIni = border + ((bound.width - 2 * border) * (2 / 3.0F));
        String str = Messages.getString("OrthoReportLayer.Caption");
        g2d.drawString(str, xIni, firstY);

        //linha
        g2d.setPaint(opBlueColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine((int) xIni,
                (int) (firstY + fontHeight + lineStep),
                (int) (xIni + fontHeight),
                (int) (firstY + lineStep));

        //ponto
        g2d.drawOval((int) xIni, (int) (firstY + (3 * lineStep)),
                (int) fontHeight, (int) fontHeight);

        //text
        float xText = xIni + fontHeight * 1.5F;
        g2d.setPaint(Color.black);
        float line = firstY + (lineStep * 1.2F);
        g2d.drawString(Messages.getString("OrthoReportLayer.CaptionLine1"),
                xText, line);
        line = line + lineStep;
        g2d.drawString(Messages.getString("OrthoReportLayer.CaptionLine2"),
                xText, line);
        line = line + (lineStep * 1.5F); //mais espaço
        g2d.drawString(Messages.getString("OrthoReportLayer.CaptionPoint1"),
                xText, line);
        line = line + lineStep;
        g2d.drawString(Messages.getString("OrthoReportLayer.CaptionPoint2"),
                xText, line);
    }

    
    private void drawReportResults(final Graphics2D g2d,
            final float fontHeight, int firstY, MediaSeriesGroup patient) {

        float lineStep = fontHeight * 1.2F;
        g2d.setPaint(Color.BLACK);
        g2d.drawString(Messages.getString("OrthoReportLayer.CephResults"),
                border, firstY);

        boolean showNoInfo = true;
        float line = firstY;

        //tags unicas
        for (TagW tagW : CEPH_TAGS) {
            Object value = patient.getTagValue(tagW);
            if (value instanceof Double) {
                CephParameter parameter = CephParameter.getCephParByTag(tagW);
                String concl = parameter.getConclusionFor((Double) value);

                line = line + lineStep;
                g2d.drawString(concl, border, line);
                showNoInfo = false;
            }
        }

        //tags combinadas
        //TODO se estas contas todas forem feitas junto com a digitação
        // (ou em OrthoReport!) elas serao feitas menos vezes!
        Object valTag1 = patient.getTagValue(TagO.HIF_NA);
        Object valTag2 = patient.getTagValue(TagO.DOT_NA);
        String result = "";
        if (valTag1 instanceof Double && valTag2 instanceof Double) {
            double val1 = (Double) valTag1;
            double val2 = (Double) valTag2;
            if (4 < val1 && val1 < 5 && 22 < val2 && val2 < 23) {
                result = "Incisivos superiores Dentro da Norma";
            } else {
                result = "Incisivos superiores ";
                boolean putE = false;
                if (val1 <= 4) {
                    result += "retruídos";
                    putE = true;
                } else if (val1 >= 5) {
                    result += "protruídos";
                    putE = true;
                }
                if (val2 <= 22) {
                    if (putE) {
                        result += " e ";
                    }
                    result += "linguarizados";
                } else if (val2 >= 23) {
                    if (putE) {
                        result += " e ";
                    }
                    result += "vestibularizados";
                }
                result += ".";
            }
            line = line + lineStep;
            g2d.drawString(result, border, line);
            showNoInfo = false;
        }

        valTag1 = patient.getTagValue(TagO.HIF_NB);
        valTag2 = patient.getTagValue(TagO.DOT_NB);
        result = "";
        if (valTag1 instanceof Double && valTag2 instanceof Double) {
            double val1 = (Double) valTag1;
            double val2 = (Double) valTag2;
            if (4 < val1 && val1 < 5 && 25 < val2 && val2 < 26) {
                result = "Incisivos inferiores Dentro da Norma";
            } else {
                result = "Incisivos inferiores ";
                boolean putE = false;
                if (val1 <= 4) {
                    result += "retruídos";
                    putE = true;
                } else if (val1 >= 5) {
                    result += "protruídos";
                    putE = true;
                }
                if (val2 <= 25) {
                    if (putE) {
                        result += " e ";
                    }
                    result += "linguarizados";
                } else if (val2 >= 26) {
                    if (putE) {
                        result += " e ";
                    }
                    result += "vestibularizados";
                }
                result += ".";
            }
            line = line + lineStep;
            g2d.drawString(result, border, line);
            showNoInfo = false;
        }

        if (showNoInfo) {
            line = line + lineStep;
            g2d.drawString(Messages.getString("OrthoReportLayer.noData"),
                    border, line);
        }
        
        String reportRole = getReportRole(view2DPane.getSeries());

        line = line + (lineStep * 1.7F);
        String title = Messages.getString("OrthoReportLayer.MedLineResults")
                + " (" + Messages.getString("OrthoReportLayer." + reportRole)
                + ")";
        g2d.drawString(title, border, line);

        TagW lineTag = TagO.MED_LINE_SUP;
        if (MANDIBLE.equals(reportRole)) {
            lineTag = TagO.MED_LINE_INF;
        }
        Object tagValue = patient.getTagValue(lineTag);
        String lineText = "Linha média correta.";
        if (tagValue instanceof Double) {
            double value = Math.abs((Double) tagValue);
            if (value == 0) {
                lineText = "Linha média correta.";
            } else {
                String side = "direita.";
                if (value > 0) {
                    side = "esquerda.";
                }
                lineText = "Desviada " + value + "mm para a " + side;
            }
        }
        line = line + lineStep;
        g2d.drawString(lineText, border, line);


        line = line + (lineStep * 1.7F);
        g2d.drawString(Messages.getString("OrthoReportLayer.BMResults")
                + " (" + Messages.getString("OrthoReportLayer." + reportRole)
                + ")",
                border, line);

        MediaSeries series = view2DPane.getSeries();

        line = line + lineStep;
        String str = "";
        Object twistSide = series.getTagValue(TagO.TURNING_TENDENCY);
        if (twistSide instanceof String) {
            if ("none".equalsIgnoreCase((String) twistSide)) {
                str = Messages.getString(
                    "OrthoReportLayer.TwistTendencyText.none");
            } else {
            str = Messages.getString(
                    "OrthoReportLayer.TwistTendencyText")
                    + " " + Messages.getString("OrthoReportLayer." + twistSide)
                    + ".";
            }
            g2d.drawString(str, border, line);
            line = line + lineStep;
        }

        Object projValue = series.getTagValue(TagO.PROJECTION_TENDENCY);
        if (projValue instanceof Boolean) {
            String call = ((Boolean) projValue).toString();
            str = Messages.getString(
                    "OrthoReportLayer.ProjTenText." + call);
        } else {
            str = "Tendência de projeção dos incisicos: não calculada"
                    + " (Posição dos incisivos não informada).";
        }
        g2d.drawString(str, border, line);

    }

    private void drawFooter(Graphics2D g2d, float fontHeight) {

        g2d.setPaint(opBlueColor);
        g2d.setStroke(new BasicStroke(2));
        final Rectangle bound = view2DPane.getBounds();
        int line = (int) (bound.height - border - fontHeight * 4);
        float lineStep = fontHeight * 1.2F;

        g2d.drawLine(border, line, bound.width - border, line);

        g2d.setPaint(Color.black);
        line = (int) (line + lineStep * 1.5F);
        g2d.drawString(Messages.getString("OrthoReportLayer.footer1"),
                border, line);
    }

    @Override
    public AnnotationsLayer getLayerCopy(DefaultView2d view2DPane) {
        OrthoReportLayer layer = new OrthoReportLayer(view2DPane);
        HashMap<String, Boolean> prefs = layer.displayPreferences;
        prefs.put(REPORT_HEAD, getDisplayPreferences(REPORT_HEAD));
        prefs.put(REPORT_RESULT, getDisplayPreferences(REPORT_RESULT));
        prefs.put(CAPTION, getDisplayPreferences(CAPTION));
        prefs.put(REPORT_FOOTER, getDisplayPreferences(REPORT_FOOTER));

        return layer;
    }

    @Override
    public void setVisible(boolean flag) {
        visible = flag;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setLevel(int i) {
        //do nothing;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public boolean isShowBottomScale() {
        return false;
    }

    @Override
    public void setShowBottomScale(boolean showBottomScale) {
        //empty
    }
    
    private String getReportRole(MediaSeries series) {
        Object roleValue = series.getTagValue(TagO.STUDY_ROLE);
        if (roleValue instanceof String && !"".equals(roleValue)) {
            return (String) roleValue;
        }
        return null;
    }

    @Override
    public PixelInfo getPixelInfo() {
        return null;
    }

    @Override
    public AbstractLayer.Identifier getIdentifier() {
        return AbstractLayer.ANNOTATION;
    }

}
