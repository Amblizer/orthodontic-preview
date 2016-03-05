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
package com.orthodonticpreview.view.print;

import com.orthodonticpreview.view.OrthoEventManager;
import com.orthodonticpreview.view.OrthoView;
import com.orthodonticpreview.view.ViewContainer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gabriela Bauermann (gabriela@animati.com.br)
 * @version 2012, 7 Jan.
 */
public class ReportPrintable extends SwingWorker<Void, Void>
        implements Printable {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            ReportPrintable.class);

    /**
     * Field with chosen printer options.
     */
    private PrintRequestAttributeSet aset;
    private PrintService service;
    private final ViewContainer containerToPrint;

    public ReportPrintable(
            ViewContainer report, PrintRequestAttributeSet set) {
        aset = set;
        containerToPrint = report;
    }

    public void print() {
        final PrinterJob pjob = PrinterJob.getPrinterJob();
        pjob.setPrintable(this);

        // Get a list of all printers that can handle Printable objects.
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        final PrintService[] services
                = PrintServiceLookup.lookupPrintServices(flavor, null);

        PrintService lastService
                = OrthoEventManager.getInstance().getLastService();

        final int[] placeScreen = getPlaceToCenterWindow();
        // Display a dialog that allows the user to select one of the
        // available printers and to edit the default attributes
        service = ServiceUI.printDialog(
                null, placeScreen[0], placeScreen[1], services,
                lastService, flavor, aset);

        if (service == null) {
            return;
        }
        OrthoEventManager.getInstance().setLastService(service);

        try {
            this.execute();
        } catch (Exception e) {
            LOGGER.error("Error trying to print: " + e);
        }
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
            throws PrinterException {

        if (containerToPrint == null || pageIndex > 0) {
            return Printable.NO_SUCH_PAGE;
        }

        // Set to the upper left corner
        if (graphics instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(
                    pageFormat.getImageableX(), pageFormat.getImageableY());

            OrthoView viewPane = (OrthoView) containerToPrint.getSelectedImagePane();
            PrintView image = new PrintView(viewPane);

            setZoomAndPosition(pageFormat, image);

            boolean wasBuffered = disableDoubleBuffering((JComponent) image);
            image.draw(g2d, 10);
            restoreDoubleBuffering((JComponent) image, wasBuffered);
        }

        return Printable.PAGE_EXISTS;
    }

    protected void printToService(PrintService service,
            PrintRequestAttributeSet aset) {
        // Now create a Doc that encapsulates the Printable object and its type
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        Doc doc = new SimpleDoc(this, flavor, null);

        // Java 1.4 uses DocPrintJob. Create one from the service
        DocPrintJob job = service.createPrintJob();

        job.addPrintJobListener(new PrintJobAdapter() {
            public void printJobCompleted(PrintJobEvent e) {
                LOGGER.info("Printing complete.");
            }

            public void printDataTransferCompleted(PrintJobEvent e) {
                LOGGER.info("Document transfered to printer.");
            }

            public void printJobRequiresAttention(PrintJobEvent e) {
                LOGGER.info("Check printer: out of paper?");
            }

            public void printJobFailed(PrintJobEvent e) {
                LOGGER.info("Print job failed");
            }
        });

        // Now print the Doc to the DocPrintJob
        try {
            job.print(doc, aset);
        } catch (PrintException e) {
            e.printStackTrace();
        }
    }

    private int[] getPlaceToCenterWindow() {
        int winWid = 500;
        int winHei = 400;
        int[] location = new int[2];

        try {
            final Rectangle bound = GraphicsEnvironment
                    .getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration().getBounds();
            location[0] = bound.x + (bound.width - winWid) / 2;
            location[1] = bound.y + (bound.height - winHei) / 2;
        } catch (Exception e) {
            LOGGER.info("Cant find center of screen " + e);
            location[0] = location[1] = 100;
        }
        return location;
    }

    /**
     * SwingWorker suport.
     *
     * @return
     * @throws Exception
     */
    @Override
    protected Void doInBackground() throws Exception {

        //debug
        LOGGER.debug("Printer attributes (inside doInBackground)");
        Attribute[] toArray = aset.toArray();
        for (Attribute attribute : toArray) {
            LOGGER.debug(attribute.getName() + " = " + attribute.getCategory());
        }

        LOGGER.debug("service (inside doInBackground) = " + service.getName());

        //call a method defined below to finish the printing
        printToService(service, aset);

        return null;
    }

    @Override
    protected void done() {
        LOGGER.info("done");

        try {
            get();
        } catch (Exception ignore) {
            LOGGER.debug("Catching exception - " + ignore);
        }

        //Must be here, when print is done.
        //layout.dispose();
    }

    /**
     * Disable double buffering (must be done before printing).
     *
     * @param component component to disable doublebuffering.
     * @return true if is was doublebuffered.
     */
    public static boolean disableDoubleBuffering(
            final JComponent component) {
        if (component == null) {
            return false;
        }
        final boolean wasDB = component.isDoubleBuffered();
        component.setDoubleBuffered(false);
        return wasDB;
    }

    /**
     * Restore doublebuffering to a component.
     *
     * @param component comp. to restore.
     * @param wasBuffered true if is to restore to true.
     */
    public static void restoreDoubleBuffering(
            final JComponent component, final boolean wasBuffered) {
        if (component != null) {
            component.setDoubleBuffered(wasBuffered);
        }
    }

    private void setZoomAndPosition(PageFormat pageFormat, PrintView image) {
        //a view já estava na relação correta para imprimir
        final Rectangle2D bound = (Rectangle2D) image.getActionValue(
                "origin.image.bound");

        double scaleW = pageFormat.getImageableWidth() / bound.getWidth();

        final double scaleH
                = pageFormat.getImageableHeight() / bound.getHeight();

        final Dimension canvasPixSize = new Dimension();
        canvasPixSize.setSize(bound.getWidth() * scaleW,
                bound.getHeight() * scaleH);

        //somar 0.5 foi usado como tecnica de arredondamento.
        image.setSize((int) (canvasPixSize.getWidth() + 0.5),
                (int) (canvasPixSize.getHeight() + 0.5));

        double originZoom = (Double) image.getActionValue("origin.zoom");

        final double scaleFactor = Math.min(
                canvasPixSize.getWidth() / bound.getWidth(),
                canvasPixSize.getHeight() / bound.getHeight());

        image.zoom(scaleFactor * originZoom);

        final Point2D origin
                = (Point2D) image.getActionValue("origin.origin");
        image.setOrigin(origin.getX(), origin.getY());

    }

}
