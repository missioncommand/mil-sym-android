package armyc2.c5isr.renderer.test1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.util.SparseArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import armyc2.c5isr.renderer.MilStdIconRenderer;
import armyc2.c5isr.renderer.utilities.DrawRules;
import armyc2.c5isr.renderer.utilities.ImageInfo;
import armyc2.c5isr.renderer.utilities.MSInfo;
import armyc2.c5isr.renderer.utilities.MSLookup;
import armyc2.c5isr.renderer.utilities.MilStdAttributes;
import armyc2.c5isr.renderer.utilities.Modifiers;
import armyc2.c5isr.renderer.utilities.RendererSettings;
import armyc2.c5isr.renderer.utilities.SVGInfo;
import armyc2.c5isr.renderer.utilities.SVGLookup;
import armyc2.c5isr.renderer.utilities.SymbolID;
import armyc2.c5isr.renderer.utilities.SymbolUtilities;

public class ExportSPImagesE {
    private static File rootFolder;
    static File pdfFile;

    // Unit test IDs is a list of basic IDs with one for each unit set. Symbol is either first in set
    // or a symbol with a smaller icon to make modifiers easier to see.
    final static String[] unitTestIDs = {"01110000", "02110000", "05110000", "06110000", "10110000", "11110000", "15110000", "20110000", "27110201", "30110000", "35110000", "36110000", "40130500", "50110100", "60110100"};

    private static final int VERSION = SymbolID.Version_2525E;

    public static boolean exportTestImages(Context context) {
        boolean success = true;
        rootFolder = new File(context.getFilesDir() + File.separator + "SPImageTestE" + File.separator);
        if (rootFolder.exists()) {
            success = deleteDirectory(rootFolder);
        }
        success &= rootFolder.mkdirs();

        if (!success)
            return false;

        // Comment out any combination of the following lines to remove them from the PDF
        createUnitModTestImages();
        createTGModTestImages();
        createAffiliationTestImages();
        createContextTestImages();
        createStatusTestImages();
        createAmplifierTestImages();
        createHQTFDTestImages();
        createCustomColorTestImages();
        createSPImages();
        createSector1TestImages();
        createSector2TestImages();
        createFrameTestImages();

        generatePDF();
        return true;
    }

    /**
     * Creates PDF of all test image files
     * Sorts images in PDF based on file structure
     */
    private static void generatePDF() {
        final PdfDocument pdfDocument = new PdfDocument();
        final int PAGE_HEIGHT = 11 * 72;
        final int PAGE_WIDTH = (int) (8.5 * 72);
        int pageNum = 1; // page counter
        final int IMG_HEIGHT = 100;
        final int TEXT_HEIGHT = 30;
        final int PAGE_MARGIN = 20;
        final int IMG_SPACING = 15;
        final Comparator<File> lastModifiedComparator = Comparator.comparingLong(File::lastModified);
        final int PAGE_COLOR = Color.LTGRAY;
        Paint textPaint = new Paint();

        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setColor(Color.BLACK);

        // Title page
        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum++).create();
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);
        Canvas canvas = myPage.getCanvas();
        canvas.drawColor(PAGE_COLOR);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(15);
        canvas.drawText("Android 2525E Renderer test", PAGE_WIDTH / 2f, PAGE_HEIGHT / 2f, textPaint);
        List<File> keyFolders = Arrays.asList(rootFolder.listFiles());
        keyFolders.sort(lastModifiedComparator);
        for (File keyFolder : keyFolders) {
            pdfDocument.finishPage(myPage);
            mypageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum++).create();
            myPage = pdfDocument.startPage(mypageInfo);
            canvas = myPage.getCanvas();
            canvas.drawColor(PAGE_COLOR);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setTextSize(15);
            canvas.drawText(keyFolder.getName(), PAGE_WIDTH / 2f, 50 - TEXT_HEIGHT / 2f, textPaint);

            int y = PAGE_MARGIN + TEXT_HEIGHT;
            List<File> valueFolders = Arrays.asList(keyFolder.listFiles());
            valueFolders.sort(lastModifiedComparator);
            for (File valueFolder : valueFolders) {
                if (y + (IMG_HEIGHT + IMG_SPACING) * 2 + TEXT_HEIGHT >= PAGE_HEIGHT - PAGE_MARGIN) {
                    pdfDocument.finishPage(myPage);
                    mypageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum++).create();
                    myPage = pdfDocument.startPage(mypageInfo);
                    canvas = myPage.getCanvas();
                    canvas.drawColor(PAGE_COLOR);
                    y = PAGE_MARGIN;
                } else if (!valueFolder.equals(valueFolders.get(0))) {
                    y += IMG_HEIGHT + IMG_SPACING;
                }

                int x = PAGE_MARGIN;
                y += TEXT_HEIGHT;
                textPaint.setTextSize(15);
                textPaint.setTextAlign(Paint.Align.LEFT);
                textPaint.setTypeface(Typeface.DEFAULT);
                canvas.drawText(valueFolder.getName(), x, y - TEXT_HEIGHT / 2f, textPaint);
                List<File> imgFiles = Arrays.asList(valueFolder.listFiles());
                imgFiles.sort(lastModifiedComparator);
                for (File img : imgFiles) {
                    Bitmap bmp = BitmapFactory.decodeFile(img.getPath());

                    final float scale = Math.min(IMG_HEIGHT / (float) bmp.getHeight(), 1);
                    final int currentBmpWidth = Math.round((float) bmp.getWidth() * scale);

                    if (x + currentBmpWidth + IMG_SPACING >= PAGE_WIDTH - PAGE_MARGIN) {
                        if (y + (IMG_HEIGHT + IMG_SPACING) * 2 + TEXT_HEIGHT >= PAGE_HEIGHT - PAGE_MARGIN) {
                            pdfDocument.finishPage(myPage);
                            mypageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum++).create();
                            myPage = pdfDocument.startPage(mypageInfo);
                            canvas = myPage.getCanvas();
                            canvas.drawColor(PAGE_COLOR);
                            y = PAGE_MARGIN;
                        } else {
                            y += IMG_HEIGHT + IMG_SPACING;
                        }
                        x = PAGE_MARGIN;
                    }
                    textPaint.setTextSize(8);
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                    canvas.drawText(img.getName().substring(0, img.getName().length() - 4), x + currentBmpWidth / 2f, y + IMG_HEIGHT + 8, textPaint);

                    // Translate has to be set after adjusting x and y
                    Matrix matrix = new Matrix();
                    if (bmp.getHeight() < IMG_HEIGHT) {
                        // Center image vertically
                        matrix.setTranslate(x, y + (IMG_HEIGHT - bmp.getHeight()) / 2f);
                    } else {
                        matrix.setTranslate(x, y);
                        matrix.preScale(scale, scale);
                    }

                    canvas.drawBitmap(bmp, matrix, null);
                    x += currentBmpWidth + IMG_SPACING;
                }
            }
        }
        pdfDocument.finishPage(myPage);

        pdfFile = new File(rootFolder, "AndroidERenderOutput.pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            System.out.println("PDF saved at " + pdfFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();
    }

    /**
     * Creates PNG of every single point symbol with no modifiers
     */
    private static void createSPImages() {
        List<String> idList = MSLookup.getInstance().getIDList(VERSION);

        for (String basicID : idList) {
            String id = VERSION + "0300000000000000000000000000";

            if (basicID.length() < 8)
                continue;

            id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));
            id = SymbolID.setSymbolSet(id, Integer.parseInt(basicID.substring(0, 2)));

            if (SymbolUtilities.isMultiPoint(id) || !canRender(id))
                continue;

            String symbolSetName = MSInfo.parseSymbolSetName(id);

            final File symbolSetFolder = new File(rootFolder + File.separator + "svg" + File.separator + symbolSetName);
            renderAndSave(symbolSetFolder, id);
        }
    }

    /**
     * Creates PNG for all units with all modifiers included
     */
    private static void createUnitModTestImages() {
        Map<String,String> modifiers = new HashMap<>();
        Map<String,String> attributes = new HashMap<>();
        populateModifiersForUnits(modifiers);

        // Mine warfare
        String id = VERSION + "0336272611000000000000000840";
        File modFolder = new File(rootFolder + File.separator + "Modifiers" + File.separator + "No Modifiers");
        renderAndSave(modFolder, id, modifiers, attributes);

        // Other symbol sets
        for (String basicID : unitTestIDs) {
            if (basicID.startsWith("36"))
                continue;

            id = VERSION + "0300272600000000000000000840";

            id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));
            id = SymbolID.setSymbolSet(id, Integer.parseInt(basicID.substring(0, 2)));

            MSInfo msi = MSLookup.getInstance().getMSLInfo(basicID, SymbolID.getVersion(id));
            if (msi.getModifiers().contains(Modifiers.R_MOBILITY_INDICATOR)) {
                id = SymbolID.setAmplifierDescriptor(id, 31);
            } else if (msi.getModifiers().contains(Modifiers.AG_AUX_EQUIP_INDICATOR)) {
                id = SymbolID.setAmplifierDescriptor(id, 61);
            }

            modFolder = new File(rootFolder + File.separator + "Modifiers" + File.separator + "Unit");
            renderAndSave(modFolder, id, modifiers, attributes);
        }
    }

    /**
     * Creates PNG for single point tactical graphics that have modifiers. Includes all modifiers
     */
    private static void createTGModTestImages() {
        Map<String,String> modifiers = new HashMap<>();
        Map<String,String> attributes = new HashMap<>();
        populateModifiersForTGs(modifiers);

        List<String> idList = MSLookup.getInstance().getIDList(VERSION);

        // Control measures
        for (String basicID : idList) {
            String id = VERSION + "0325000000000000000000000840";

            if (basicID.length() < 8 || !basicID.startsWith("25"))
                continue;

            id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));

            if (SymbolUtilities.isMultiPoint(id) || !canRender(id))
                continue;

            MSInfo msi = MSLookup.getInstance().getMSLInfo(basicID, SymbolID.getVersion(id));
            if (msi.getModifiers().isEmpty())
                continue;

            final File symbolSetFolder = new File(rootFolder + File.separator + "Modifiers" + File.separator + "Control Measure");
            renderAndSave(symbolSetFolder, id, modifiers, attributes);
        }

        // METOC - no list of modifiers in msd. List of symbols with modifiers made by hand
        for (String basicID : new String[]{"45110102", "45110202", "45162200", "45162300"}) {
            String id = VERSION + "0345000000000000000000000840";

            id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));

            final File symbolSetFolder = new File(rootFolder + File.separator + "Modifiers" + File.separator + "Atmospheric");
            renderAndSave(symbolSetFolder, id, modifiers, attributes);
        }
    }

    /**
     * Creates PNG for each type of unit with each affiliation
     */
    private static void createAffiliationTestImages() {
        // Test units
        Map<String,String> modifiers = new HashMap<>();
        Map<String,String> attributes = new HashMap<>();
        modifiers.put(Modifiers.AO_ENGAGEMENT_BAR, "AO:AOA-AO");
        for (int aff : new int[]{0, 1, 2, 3, 4, 5, 6}) {
            for (String basicID : unitTestIDs) {
                String id = VERSION + "0000000000000000000000000000";

                id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));
                id = SymbolID.setSymbolSet(id, Integer.parseInt(basicID.substring(0, 2)));
                id = SymbolID.setAffiliation(id, aff);

                String affiliationName = parseAffiliation(aff);
                final File modFolder = new File(rootFolder + File.separator + "Affiliation" + File.separator + basicID);
                renderAndSave(modFolder, affiliationName, id, modifiers, attributes);
            }
        }

        // Test all affiliations for a single control measure to check colors
        for (int aff : new int[]{0, 1, 2, 3, 4, 5, 6}) {
            String id = VERSION + "0025000013100300000000000000";

            id = SymbolID.setAffiliation(id, aff);

            String affiliationName = parseAffiliation(aff);
            final File symbolSetFolder = new File(rootFolder + File.separator + "Affiliation" + File.separator + "Action Point");
            renderAndSave(symbolSetFolder, affiliationName, id);
        }

        // Test all control measures with hostile to check full symbol changes color
        List<String> idList = MSLookup.getInstance().getIDList(VERSION);
        for (String basicID : idList) {
            String id = VERSION + "0625000000000000000000000000";

            if (basicID.length() < 8 || !basicID.startsWith("25"))
                continue;

            id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));

            if (SymbolUtilities.isMultiPoint(id) || !canRender(id))
                continue;

            final File symbolSetFolder = new File(rootFolder + File.separator + "Affiliation" + File.separator + "Hostile Control Measure");
            renderAndSave(symbolSetFolder, basicID, id);
        }
    }

    /**
     * Creates PNG for each type of unit with each context
     */
    private static void createContextTestImages() {
        for (int context : new int[]{0, 1, 2}) {
            for (String basicID : unitTestIDs) {
                if (basicID.startsWith("50")) // No exercise friend for unknown frame
                    continue;

                String id = VERSION + "0300000000000000000000000000";

                id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));
                id = SymbolID.setSymbolSet(id, Integer.parseInt(basicID.substring(0, 2)));
                id = SymbolID.setContext(id, context);

                String contextName = context + " " + parseContext(context);
                final File modFolder = new File(rootFolder + File.separator + "Context" + File.separator + contextName);
                renderAndSave(modFolder, id);
            }
        }

        // Test J and K outside frame for joker and hostile affiliations
        for (int aff : new int[]{SymbolID.StandardIdentity_Affiliation_Hostile_Faker, SymbolID.StandardIdentity_Affiliation_Suspect_Joker}) {
            for (String basicID : unitTestIDs) {
                if (basicID.startsWith("50")) // No exercise for unknown frame
                    continue;

                String id = VERSION + "1000000000000000000000000000";

                id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));
                id = SymbolID.setSymbolSet(id, Integer.parseInt(basicID.substring(0, 2)));
                id = SymbolID.setAffiliation(id, aff);

                String contextName = "1 Exercise - " + parseAffiliation(aff);
                final File modFolder = new File(rootFolder + File.separator + "Context" + File.separator + contextName);
                renderAndSave(modFolder, id);
            }
        }
    }

    /**
     * Creates PNG for each type of unit with each status
     */
    private static void createStatusTestImages() {
        for (int status : new int[]{0, 1, 2, 3, 4, 5}) {
            for (String basicID : unitTestIDs) {
                String id = VERSION + "0300000000000000000000000000";

                id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));
                id = SymbolID.setSymbolSet(id, Integer.parseInt(basicID.substring(0, 2)));
                id = SymbolID.setStatus(id, status);

                String statusName = status + " " + parseStatus(status) + " - " + parseAffiliation(3);

                MSInfo msi = MSLookup.getInstance().getMSLInfo(basicID, SymbolID.getVersion(id));
                if (status > 1) {
                    if (!msi.getModifiers().contains(Modifiers.AL_OPERATIONAL_CONDITION)) {
                        if (status == 2)
                            statusName = "2 no operational condition Expected";
                        else
                            continue;
                    }
                }

                RendererSettings.getInstance().setOperationalConditionModifierType(RendererSettings.OperationalConditionModifierType_BAR);
                File modFolder = new File(rootFolder + File.separator + "Status (Bar)" + File.separator + statusName);
                renderAndSave(modFolder, id);

                if (status >= 2 && status <= 4) {
                    RendererSettings.getInstance().setOperationalConditionModifierType(RendererSettings.OperationalConditionModifierType_SLASH);
                    modFolder = new File(rootFolder + File.separator + "Status (Slash)" + File.separator + statusName);
                    renderAndSave(modFolder, id);
                }

                // Test assumed friend with status - want to confirm dotted frame overrides planned frame
                // Also check that dotted frame is there with operational condition
                if (status <= 2) {
                    if (status == 2 && !msi.getModifiers().contains(Modifiers.AL_OPERATIONAL_CONDITION))
                        continue;
                    id = SymbolID.setStandardIdentity(id, 2);

                    statusName = status + " " + parseStatus(status) + " - " + parseAffiliation(2);
                    modFolder = new File(rootFolder + File.separator + "Assumed friend Status" + File.separator + statusName);
                    renderAndSave(modFolder, id);
                }
            }
        }
    }

    /**
     * Creates PNG for each type of unit with each HQTFD
     */
    private static void createHQTFDTestImages() {
        for (int HQTFD : new int[]{0, 1, 2, 3, 4, 5, 6, 7}) {
            for (String basicID : unitTestIDs) {
                String id = VERSION + "0300000000000000000000000000";

                id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));
                id = SymbolID.setSymbolSet(id, Integer.parseInt(basicID.substring(0, 2)));
                id = SymbolID.setHQTFD(id, HQTFD);

                String HQTFDName = HQTFD + " " + parseHQTFD(HQTFD);

                MSInfo msi = MSLookup.getInstance().getMSLInfo(basicID, SymbolID.getVersion(id));
                String HQTFD_bin = Integer.toBinaryString(HQTFD);
                if (HQTFD > 0) {
                    if (!(msi.getModifiers().contains(Modifiers.D_TASK_FORCE_INDICATOR)
                            || msi.getModifiers().contains(Modifiers.AB_FEINT_DUMMY_INDICATOR)
                            || msi.getModifiers().contains(Modifiers.S_HQ_STAFF_INDICATOR))) {
                        if (HQTFD == 7)
                            // Confirm not added
                            HQTFDName = "7 no HQTFD expected";
                        else
                            continue;
                    } else if (HQTFD_bin.charAt(HQTFD_bin.length() - 1) == '1'
                            && !msi.getModifiers().contains(Modifiers.AB_FEINT_DUMMY_INDICATOR)) {
                        continue;
                    } else if (HQTFD >= 2 && HQTFD_bin.charAt(HQTFD_bin.length() - 2) == '1'
                            && !msi.getModifiers().contains(Modifiers.S_HQ_STAFF_INDICATOR)) {
                        continue;
                    } else if (HQTFD >= 4 && !msi.getModifiers().contains(Modifiers.D_TASK_FORCE_INDICATOR)) {
                        continue;
                    }
                }

                final File modFolder = new File(rootFolder + File.separator + "HQTFD" + File.separator + HQTFDName);
                renderAndSave(modFolder, id);
            }
        }
    }

    /**
     * Creates PNG for each amplifier value for each unit affected by it
     * Additional directory created for units that should not be affected by amplifier value
     */
    private static void createAmplifierTestImages() {
        for (int amp : new int[]{11, 12, 13, 14, 15, 16, 17, 18, 21, 22, 23, 24, 25, 26, 31, 32, 33, 34, 35, 36, 37, 41, 42, 51, 52, 61, 62, 71}) {
            for (String basicID : unitTestIDs) {
                String id = VERSION + "0300000000000000000000000000";

                id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));
                id = SymbolID.setSymbolSet(id, Integer.parseInt(basicID.substring(0, 2)));
                id = SymbolID.setAmplifierDescriptor(id, amp);

                String ampName = amp + " " + parseAmp(amp);

                MSInfo msi = MSLookup.getInstance().getMSLInfo(basicID, SymbolID.getVersion(id));

                if (amp < 30) {
                    // echelon
                    if (!msi.getModifiers().contains(Modifiers.B_ECHELON)) {
                        if (amp == 11) {
                            // Confirm not added
                            ampName = "11 no echelon expected";
                        } else {
                            continue;
                        }
                    }
                } else if (amp < 60) {
                    // mobility indicator
                    if (!(msi.getModifiers().contains(Modifiers.R_MOBILITY_INDICATOR))) {
                        if (amp == 31) {
                            // Confirm not added
                            ampName = "31 no mobility expected";
                        } else {
                            continue;
                        }
                    }
                } else if (amp < 70) {
                    // auxiliary equipment indicator
                    if (!msi.getModifiers().contains(Modifiers.AG_AUX_EQUIP_INDICATOR)) {
                        if (amp == 61) {
                            // Confirm not added
                            ampName = "61 no tow array expected";
                        } else {
                            continue;
                        }
                    }
                } else {
                    // Leadership Indicator
                    if (SymbolID.getSymbolSet(id) != 27) {
                        // Confirm not added - only one leadership indicator in E (71)
                        ampName = "71 no leadership indicator expected";
                    } else {
                        // Test leadership indicator with multiple affiliations below
                        continue;
                    }
                }
                final File modFolder = new File(rootFolder + File.separator + "Amplifier" + File.separator + ampName);
                renderAndSave(modFolder, id);
            }
        }

        for (int aff : new int[]{0, 1, 2, 3, 4, 5, 6}) {
            String id = VERSION + "0327007111020100000000000000";
            id = SymbolID.setAffiliation(id, aff);
            String ampName = "71 Leader Individual - 27110201";
            final File modFolder = new File(rootFolder + File.separator + "Amplifier" + File.separator + ampName);
            renderAndSave(modFolder, parseAffiliation(aff), id);
        }
    }

    /**
     * Creates PNG for each sector 1 modifier for each unit and static depiction
     */
    private static void createSector1TestImages() {
        HashMap<String, Integer> sector1Mods = new HashMap<>();
        sector1Mods.put("01", 39);
        sector1Mods.put("02", 9);
        sector1Mods.put("05", 6);
        sector1Mods.put("06", 4);
        sector1Mods.put("10", 99);
        sector1Mods.put("11", 24);
        sector1Mods.put("15", 13);
        sector1Mods.put("20", 13);
        sector1Mods.put("25", 50);
        sector1Mods.put("27", 46);
        sector1Mods.put("30", 22);
        sector1Mods.put("35", 19);
        sector1Mods.put("36", -1);
        sector1Mods.put("40", 22);
        sector1Mods.put("45", -1);
        sector1Mods.put("46", -1);
        sector1Mods.put("47", -1);
        sector1Mods.put("50", 64);
        sector1Mods.put("60", 13);

        for (String basicID : unitTestIDs) {
            for (int sector1Mod : IntStream.rangeClosed(0, sector1Mods.get(basicID.substring(0, 2))).toArray()) {
                String id = VERSION + "0300000000000000000000000000";

                id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));
                id = SymbolID.setSymbolSet(id, Integer.parseInt(basicID.substring(0, 2)));
                id = SymbolID.setModifier1(id, sector1Mod);

                final File modFolder = new File(rootFolder + File.separator + "Sector Mods" + File.separator + basicID);
                renderAndSave(modFolder, SVGLookup.getMod1ID(id), id);
            }
        }

        // Static Depiction
        for (int sector1Mod : IntStream.rangeClosed(13, 50).toArray()) {
            String id = VERSION + "0325000027070100000000000000";
            id = SymbolID.setModifier1(id, sector1Mod);

            final File modFolder = new File(rootFolder + File.separator + "Sector Mods" + File.separator + SVGLookup.getMainIconID(id));
            renderAndSave(modFolder, SVGLookup.getMod1ID(id), id);
        }

        // Common modifiers
        for (int sector1Mod : IntStream.rangeClosed(0, 65).toArray()) {
            String id = VERSION + "03100000110100000010000000";
            id = SymbolID.setModifier1(id, sector1Mod);

            final File modFolder = new File(rootFolder + File.separator + "Common Mods" + File.separator + "Sector 1");
            renderAndSave(modFolder, SVGLookup.getMod1ID(id), id);
        }
    }

    /**
     * Creates PNG for each sector 2 modifier for each unit
     */
    private static void createSector2TestImages() {
        HashMap<String, Integer> sector2Mods = new HashMap<>();
        sector2Mods.put("01", 11); // Used with tanker only
        sector2Mods.put("02", 16);
        sector2Mods.put("05", 12);
        sector2Mods.put("06", 15);
        sector2Mods.put("10", 89);
        sector2Mods.put("11", 1);
        sector2Mods.put("15", 6);
        sector2Mods.put("20", 10);
        sector2Mods.put("25", -1);
        sector2Mods.put("27", 38);
        sector2Mods.put("30", 12);
        sector2Mods.put("35", 13);
        sector2Mods.put("36", -1);
        sector2Mods.put("40", -1);
        sector2Mods.put("45", -1);
        sector2Mods.put("46", -1);
        sector2Mods.put("47", -1);
        sector2Mods.put("50", -1);
        sector2Mods.put("60", 8);

        for (String basicID : unitTestIDs) {
            for (int sector2Mod : IntStream.rangeClosed(0, sector2Mods.get(basicID.substring(0, 2))).toArray()) {
                String id = VERSION + "0300000000000000000000000000";

                id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));
                id = SymbolID.setSymbolSet(id, Integer.parseInt(basicID.substring(0, 2)));
                id = SymbolID.setModifier2(id, sector2Mod);

                final File modFolder = new File(rootFolder + File.separator + "Sector Mods" + File.separator + basicID);
                renderAndSave(modFolder, SVGLookup.getMod2ID(id), id);
            }
        }

        // Common modifiers
        for (int sector2Mod : IntStream.rangeClosed(0, 25).toArray()) {
            String id = VERSION + "03100000110100000001000000";
            id = SymbolID.setModifier2(id, sector2Mod);

            final File modFolder = new File(rootFolder + File.separator + "Common Mods" + File.separator + "Sector 2");
            renderAndSave(modFolder, SVGLookup.getMod2ID(id), id);
        }
    }

    /**
     * Creates PNG for each unit with custom TextColor, LineColor and FillColor
     */
    private static void createCustomColorTestImages() {
        Map<String,String> modifiers = new HashMap<>();
        Map<String,String> attributes = new HashMap<>();
        populateModifiersForUnits(modifiers);
        attributes.put(MilStdAttributes.TextColor, "00FFFF");
        attributes.put(MilStdAttributes.LineColor, "FF00FF");
        attributes.put(MilStdAttributes.FillColor, "FFFF00");
        attributes.put(MilStdAttributes.EngagementBarColor, "0000FF");

        for (String basicID : unitTestIDs) {
            String id = VERSION + "0300272600000000000000000840";
            id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));
            id = SymbolID.setSymbolSet(id, Integer.parseInt(basicID.substring(0, 2)));

            MSInfo msi = MSLookup.getInstance().getMSLInfo(basicID, SymbolID.getVersion(id));
            if (msi.getModifiers().contains(Modifiers.R_MOBILITY_INDICATOR)) {
                id = SymbolID.setAmplifierDescriptor(id, 31);
            } else if (msi.getModifiers().contains(Modifiers.AG_AUX_EQUIP_INDICATOR)) {
                id = SymbolID.setAmplifierDescriptor(id, 61);
            }

            final File modFolder = new File(rootFolder + File.separator + "Custom Color" + File.separator + "Unit");
            renderAndSave(modFolder, id, modifiers, attributes);
        }
    }

    private static void createFrameTestImages() {
        for (char frame : new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'}) {
            for (String basicID : unitTestIDs) {
                String id = VERSION + "0300000000000000000000000000";
                id = SymbolID.setEntityCode(id, Integer.parseInt(basicID.substring(2)));
                id = SymbolID.setSymbolSet(id, Integer.parseInt(basicID.substring(0, 2)));

                // Set frame shape
                id = id.substring(0, 22) + frame + id.substring(23);

                String frameName = frame + " " + parseFrameShape(frame);

                final File frameFolder = new File(rootFolder + File.separator + "Frame Shape" + File.separator + frameName);
                renderAndSave(frameFolder, id);
            }
        }
    }

    private static void renderAndSave(File folder, String fileName, String id, Map<String,String> modifiers, Map<String,String> attributes) {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        attributes.put(MilStdAttributes.PixelSize, "256");
        MilStdIconRenderer mir = MilStdIconRenderer.getInstance();
        Bitmap img;
        final File saveFile = new File(folder, fileName + ".png");
        try {
            if (!mir.CanRender(id, attributes))
                System.out.println("CanRender() false: " + id);
            ImageInfo ii = mir.RenderIcon(id, modifiers, attributes);
            img = ii.getImage();
        } catch (Exception e) {
            System.out.println("Couldn't render: " + id + " - " + saveFile.getPath());
            img = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        }
        try {
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(saveFile);
            img.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void renderAndSave(File folder, String id, Map<String,String> modifiers, Map<String,String> attributes) {
        renderAndSave(folder, SVGLookup.getMainIconID(id), id, modifiers, attributes);
    }

    private static void renderAndSave(File folder, String fileName, String id) {
        renderAndSave(folder, fileName, id, new HashMap<>(), new HashMap<>());
    }

    private static void renderAndSave(File folder, String id) {
        renderAndSave(folder, id, new HashMap<>(), new HashMap<>());
    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        boolean success = true;
        File[] contents = directoryToBeDeleted.listFiles();
        if (contents != null) {
            for (File file : contents) {
                success &= deleteDirectory(file);
            }
        }
        if (!directoryToBeDeleted.delete()) {
            System.out.println("Failed to delete: " + directoryToBeDeleted);
            return false;
        }
        return success;
    }

    // Same functionality and MilStdIconRenderer.CanRender() - doesn't log if can't render
    // Copied from SymbolPickerActivity
    private static boolean canRender(String symbolID) {
        String lookupID = SymbolUtilities.getBasicSymbolID(symbolID);
        String lookupSVGID = SVGLookup.getMainIconID(symbolID);
        MSInfo msi = MSLookup.getInstance().getMSLInfo(lookupID, SymbolID.getVersion(symbolID));
        SVGInfo si = SVGLookup.getInstance().getSVGLInfo(lookupSVGID, SymbolID.getVersion(symbolID));

        // msi should never be null
        return msi != null && msi.getDrawRule() != DrawRules.DONOTDRAW && si != null;
    }

    private static String parseAffiliation(int affiliation) {
        switch (affiliation) {
            case SymbolID.StandardIdentity_Affiliation_Pending:
                return "Pending";
            case SymbolID.StandardIdentity_Affiliation_Unknown:
                return "Unknown";
            case SymbolID.StandardIdentity_Affiliation_AssumedFriend:
                return "Assumed Friend";
            case SymbolID.StandardIdentity_Affiliation_Friend:
                return "Friend";
            case SymbolID.StandardIdentity_Affiliation_Neutral:
                return "Neutral";
            case SymbolID.StandardIdentity_Affiliation_Suspect_Joker:
                return "Suspect";
            case SymbolID.StandardIdentity_Affiliation_Hostile_Faker:
                return "Hostile";
            default:
                return "UNKNOWN";
        }
    }

    private static String parseContext(int context) {
        switch (context) {
            case SymbolID.StandardIdentity_Context_Reality:
                return "Reality";
            case SymbolID.StandardIdentity_Context_Exercise:
                return "Exercise";
            case SymbolID.StandardIdentity_Context_Simulation:
                return "Simulation";
            default:
                return "UNKNOWN";
        }
    }

    private static String parseStatus(int status) {
        switch (status) {
            case SymbolID.Status_Present:
                return "Present";
            case SymbolID.Status_Planned_Anticipated_Suspect:
                return "Planned";
            case SymbolID.Status_Present_FullyCapable:
                return "Fully capable";
            case SymbolID.Status_Present_Damaged:
                return "Damaged";
            case SymbolID.Status_Present_Destroyed:
                return "Destroyed";
            case SymbolID.Status_Present_FullToCapacity:
                return "Full to capacity";
            default:
                return "UNKNOWN";
        }
    }

    private static String parseHQTFD(int HQTFD) {
        switch (HQTFD) {
            case SymbolID.HQTFD_Unknown:
                return "Unknown";
            case SymbolID.HQTFD_FeintDummy:
                return "Dummy";
            case SymbolID.HQTFD_Headquarters:
                return "Headquarters";
            case SymbolID.HQTFD_FeintDummy_Headquarters:
                return "Dummy Headquarters";
            case SymbolID.HQTFD_TaskForce:
                return "Task Force";
            case SymbolID.HQTFD_FeintDummy_TaskForce:
                return "Dummy Task Force";
            case SymbolID.HQTFD_TaskForce_Headquarters:
                return "Task Force Headquarters";
            case SymbolID.HQTFD_FeintDummy_TaskForce_Headquarters:
                return "Dummy Task Force Headquarters";
            default:
                return "UNKNOWN";
        }
    }

    private static String parseAmp(int amp) {
        switch (amp) {
            case SymbolID.Echelon_Team_Crew:
                return "Team";
            case SymbolID.Echelon_Squad:
                return "Squad";
            case SymbolID.Echelon_Section:
                return "Section";
            case SymbolID.Echelon_Platoon_Detachment:
                return "Platoon";
            case SymbolID.Echelon_Company_Battery_Troop:
                return "Company";
            case SymbolID.Echelon_Battalion_Squadron:
                return "Battalion";
            case SymbolID.Echelon_Regiment_Group:
                return "Regiment";
            case SymbolID.Echelon_Brigade:
                return "Brigade";
            case SymbolID.Echelon_Division:
                return "Division";
            case SymbolID.Echelon_Corps_MEF:
                return "Corps";
            case SymbolID.Echelon_Army:
                return "Army";
            case SymbolID.Echelon_ArmyGroup_Front:
                return "Army Group";
            case SymbolID.Echelon_Region_Theater:
                return "Region";
            case SymbolID.Echelon_Region_Command:
                return "Command";
            case SymbolID.Mobility_WheeledLimitedCrossCountry:
                return "Wheeled limited cross country";
            case SymbolID.Mobility_WheeledCrossCountry:
                return "Wheeled cross country";
            case SymbolID.Mobility_Tracked:
                return "Tracked";
            case SymbolID.Mobility_Wheeled_Tracked:
                return "Wheeled and tracked combination";
            case SymbolID.Mobility_Towed:
                return "Towed";
            case SymbolID.Mobility_Rail:
                return "Rail";
            case SymbolID.Mobility_PackAnimals:
                return "Pack animals";
            case SymbolID.Mobility_OverSnow:
                return "Over snow";
            case SymbolID.Mobility_Sled:
                return "Sled";
            case SymbolID.Mobility_Barge:
                return "Barge";
            case SymbolID.Mobility_Amphibious:
                return "Amphibious";
            case SymbolID.Mobility_ShortTowedArray:
                return "Short towed array";
            case SymbolID.Mobility_LongTowedArray:
                return "Long towed Array";
            case SymbolID.Leadership_Individual:
                return "Leader Individual";
            default:
                return "UNKNOWN";
        }
    }

    private static String parseFrameShape(char frameShape) {
        switch (frameShape){
            case SymbolID.FrameShape_Unknown:
                return "Default frame";
            case SymbolID.FrameShape_Space:
                return "Space";
            case SymbolID.FrameShape_Air:
                return "Air";
            case SymbolID.FrameShape_LandUnit:
                return "Land Unit";
            case SymbolID.FrameShape_LandEquipment_SeaSurface:
                return "Sea Surface";
            case SymbolID.FrameShape_LandInstallation:
                return "Land Installation";
            case SymbolID.FrameShape_DismountedIndividuals :
                return "Dismounted Individuals";
            case SymbolID.FrameShape_SeaSubsurface:
                return "Sea Subsurface";
            case SymbolID.FrameShape_Activity_Event:
                return "Activity Event";
            case SymbolID.FrameShape_Cyberspace:
                return "CyberSpace";
            default:
                return "UNKNOWN";
        }
    }

    // Copied from test1
    private static void populateModifiersForUnits(Map<String,String> modifiers) {
        modifiers.put(Modifiers.H_ADDITIONAL_INFO_1, "Hj");
        modifiers.put(Modifiers.H1_ADDITIONAL_INFO_2, "H1");
        modifiers.put(Modifiers.X_ALTITUDE_DEPTH, "X");//X
        modifiers.put(Modifiers.K_COMBAT_EFFECTIVENESS, "K");//K
        modifiers.put(Modifiers.Q_DIRECTION_OF_MOVEMENT, "45");//Q
        modifiers.put(Modifiers.W_DTG_1, SymbolUtilities.getDateLabel(new Date()));//W
        modifiers.put(Modifiers.W1_DTG_2, SymbolUtilities.getDateLabel(new Date()));//W1
        modifiers.put(Modifiers.J_EVALUATION_RATING, "J");
        modifiers.put(Modifiers.M_HIGHER_FORMATION, "M");
        modifiers.put(Modifiers.N_HOSTILE, "ENY");
        modifiers.put(Modifiers.P_IFF_SIF_AIS, "P");
        modifiers.put(Modifiers.Y_LOCATION, "Yj");
        modifiers.put(Modifiers.C_QUANTITY, "C");
        modifiers.put(Modifiers.F_REINFORCED_REDUCED, "RD");
        modifiers.put(Modifiers.L_SIGNATURE_EQUIP, "!");
        modifiers.put(Modifiers.AA_SPECIAL_C2_HQ, "AA");
        modifiers.put(Modifiers.G_STAFF_COMMENTS, "Gj");
        modifiers.put(Modifiers.V_EQUIP_TYPE, "Vj");
        modifiers.put(Modifiers.T_UNIQUE_DESIGNATION_1, "Tj");
        modifiers.put(Modifiers.T1_UNIQUE_DESIGNATION_2, "T1");
        modifiers.put(Modifiers.Z_SPEED, "999");//Z
        modifiers.put(Modifiers.R2_SIGNIT_MOBILITY_INDICATOR, "2");
        modifiers.put(Modifiers.AD_PLATFORM_TYPE, "AD");
        modifiers.put(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME, "AE");
        modifiers.put(Modifiers.AF_COMMON_IDENTIFIER, "AF");
        modifiers.put(Modifiers.AO_ENGAGEMENT_BAR, "AO:AOA-AO");
        modifiers.put(Modifiers.AR_SPECIAL_DESIGNATOR, "AR");
        modifiers.put(Modifiers.AQ_GUARDED_UNIT, "AQ");
        modifiers.put(Modifiers.AS_COUNTRY, "AS");
    }

    // Copied from test1
    private static void populateModifiersForTGs(Map<String,String> modifiers) {
        modifiers.put(Modifiers.H_ADDITIONAL_INFO_1, "H");
        modifiers.put(Modifiers.H1_ADDITIONAL_INFO_2, "H1");
        modifiers.put(Modifiers.X_ALTITUDE_DEPTH, "X");//X
        modifiers.put(Modifiers.Q_DIRECTION_OF_MOVEMENT, "45");//Q
        modifiers.put(Modifiers.W_DTG_1, SymbolUtilities.getDateLabel(new Date()));//W
        modifiers.put(Modifiers.W1_DTG_2, SymbolUtilities.getDateLabel(new Date()));//W1
        modifiers.put(Modifiers.N_HOSTILE, "ENY");
        modifiers.put(Modifiers.Y_LOCATION, "Y");
        modifiers.put(Modifiers.C_QUANTITY, "C");
        modifiers.put(Modifiers.L_SIGNATURE_EQUIP, "!");
        modifiers.put(Modifiers.V_EQUIP_TYPE, "V");
        modifiers.put(Modifiers.T_UNIQUE_DESIGNATION_1, "T");
        modifiers.put(Modifiers.T1_UNIQUE_DESIGNATION_2, "T1");
        modifiers.put(Modifiers.AP_TARGET_NUMBER, "AP");
        modifiers.put(Modifiers.AP1_TARGET_NUMBER_EXTENSION, "AP1");
    }
}
