package armyc2.c5isr.renderer.symbolpicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.TreeMap;
import java.util.stream.Collectors;

import armyc2.c5isr.renderer.MilStdIconRenderer;
import armyc2.c5isr.renderer.R;
import armyc2.c5isr.renderer.utilities.DrawRules;
import armyc2.c5isr.renderer.utilities.ImageInfo;
import armyc2.c5isr.renderer.utilities.MSInfo;
import armyc2.c5isr.renderer.utilities.MSLookup;
import armyc2.c5isr.renderer.utilities.MilStdAttributes;
import armyc2.c5isr.renderer.utilities.Modifiers;
import armyc2.c5isr.renderer.utilities.SVGInfo;
import armyc2.c5isr.renderer.utilities.SVGLookup;
import armyc2.c5isr.renderer.utilities.SymbolID;
import armyc2.c5isr.renderer.utilities.SymbolUtilities;
import armyc2.c5isr.web.render.MultiPointHandler;

/**
 * Symbol picker activity. Sends selected symbol ID back to activity initialized from
 */
public class SymbolPickerActivity extends Activity {
    public static final String selectedSymbolIdKey = "selectedSymbolIdKey";
    public static final String modifiersKey = "modifiersKey";
    public static final String attributesKey = "attributesKey";
    public static final String supportedVersionsKey = "supportedVersionsKey";
    private static final String searchNodeName = "searchResults";
    private boolean activeSearch = false;
    private final int cellSize = 300; // in px
    private Stack<Node> pageTrail; // top of stack is current page
    private SymbolGVAdapter symbolTableAdapter;
    private Button configureButton;
    private ToggleButton flattenTreeToggle;
    private Node selectedSymbolNode;
    private MilStdIconRenderer mir = null;
    private final Bitmap emptyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

    // Main Modifiers Dialog
    private Dialog mainModifiersDialog;
    private RadioGroup contextRadioGroup;
    private Spinner stdIdSpinner;
    private Spinner statusSpinner;
    private Spinner hqSpinner;
    private Spinner ampCategorySpinner;
    private Spinner ampTypeSpinner;
    private LinearLayout ampTypeLayout;
    private boolean hasSectorModifiers;
    private Spinner sector1Spinner;
    private final int SECTOR_1_MINE_INDEX = 13; // position of Mine in ss_ControlMeasure_sector1_array
    private final int ANTIPERSONNEL_MINE = 0b000001;
    private final int ANTIPERSONNEL_MINE_DIRECTIONAL = 0b000010;
    private final int ANTITANK_MINE = 0b000100;
    private final int ANTITANK_MINE_ANTIHANDLING = 0b001000;
    private final int WIDE_AREA_ANTITANK_MINE = 0b010000;
    private final int MINE_CLUSTER = 0b100000;
    private SparseArray<String> mineSectorLookup;
    private ArrayList<Integer> invalidMineTrios;
    private CheckBox AntipersonnelMineBox;
    private CheckBox AntipersonnelDirectionalMineBox;
    private CheckBox AntitankMineBox;
    private CheckBox AntitankAntihandlingMineBox;
    private CheckBox WideAreaAntitankMineBox;
    private CheckBox MineClusterBox;
    private ArrayList<CheckBox> mineCheckBoxList;
    private int numMinesChecked;
    private Spinner sector2Spinner;
    private boolean hasCountryModifier;
    private TextView countryTextView;
    private TreeMap<String, String> countryMap;
    private ArrayList<String> countryNames;
    private Dialog countrySearchDialog;

    // Attributes + Extra Modifiers Dialog
    private Dialog extraModifiersDialog;
    private static final int M = 0;
    private static final int KM = 1;
    private static final int FT = 2;
    private static final int SM = 3;
    private static final int FL = 4;
    enum AltitudeUnits {
        M(SymbolPickerActivity.M, "(meters)"),
        KM(SymbolPickerActivity.KM, "(kilometers)"),
        FT(SymbolPickerActivity.FT, "(feet)"),
        SM(SymbolPickerActivity.SM, "(statute miles)"),
        FL(SymbolPickerActivity.FL, "(flight level)");

        private final int index;
        private final String desc;

        AltitudeUnits(int index, String description) {
            this.index = index;
            this.desc = description;
        }
    }
    enum AltitudeModes {
        AMSL(0, "(above mean sea level)"),
        BMSL(1, "(below mean sea level)"),
        HAE(2, "(height above ellipsoid)"),
        AGL(3, "(above ground level)");

        private final int index;
        private final String desc;

        AltitudeModes(int index, String description) {
            this.index = index;
            this.desc = description;
        }
    }

    Spinner altitudeUnitSpinner;
    Spinner altitudeModeSpinner;
    private ArrayList<String> modifiersToGet;
    private HashMap<String, String> modifiersToSend;
    private HashMap<String, String> attributesToSend;
    private TreeManager treeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symbol_picker);

        pageTrail = new Stack<>();

        Button backButton = findViewById(R.id.symbol_picker_back_button);
        backButton.setOnClickListener(view -> onBackPressed());

        configureButton = findViewById(R.id.symbol_picker_configure_button);
        configureButton.setOnClickListener(view -> onConfigureSymbol());

        symbolTableAdapter = new SymbolGVAdapter(this, new ArrayList<>());
        GridView symbolTable = findViewById(R.id.symbol_picker_table);
        symbolTable.setAdapter(symbolTableAdapter);
        symbolTable.setColumnWidth(cellSize);

        flattenTreeToggle = findViewById(R.id.symbol_picker_flatten_toggle);
        flattenTreeToggle.setOnClickListener(view -> updateSymbolTable());

        // Do not reinitialize render or change settings in child activity
        mir = MilStdIconRenderer.getInstance();

        treeManager = new TreeManager();
        try {
            int[] versions = getIntent().getIntArrayExtra(supportedVersionsKey);
            if (versions == null)
                versions = new int[]{SymbolID.Version_2525Dch1};
            treeManager.buildTree(getApplicationContext(), versions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        updateSelectedSymbol(treeManager.mil2525Tree);

        // read in country names and codes
        String line;
        String[] segments;
        countryMap = new TreeMap<>();
        try (InputStream in = this.getResources().openRawResource(R.raw.genc);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            while ((line = reader.readLine()) != null) {
                segments = line.split("\t+");
                StringBuilder countryCode = new StringBuilder(segments[1]);
                while (countryCode.length() < 3) {
                    countryCode.insert(0, "0");
                }
                countryMap.put(segments[2], countryCode.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        countryNames = new ArrayList<>(countryMap.keySet());
        Collections.sort(countryNames);

        SearchView symbolSearchView = findViewById(R.id.symbol_picker_search);
        symbolSearchView.setOnQueryTextListener(new SearchBoxListener());

        // these four combinations of 3 mines do not have a code in 2525D Change 1
        invalidMineTrios = new ArrayList<>();
        invalidMineTrios.add(ANTITANK_MINE | ANTITANK_MINE_ANTIHANDLING | WIDE_AREA_ANTITANK_MINE);
        invalidMineTrios.add(ANTITANK_MINE | ANTITANK_MINE_ANTIHANDLING | MINE_CLUSTER);
        invalidMineTrios.add(ANTITANK_MINE | WIDE_AREA_ANTITANK_MINE | MINE_CLUSTER);
        invalidMineTrios.add(ANTITANK_MINE_ANTIHANDLING | WIDE_AREA_ANTITANK_MINE | MINE_CLUSTER);
    }

    @Override
    public void onBackPressed() {
        if (pageTrail.peek().getName().equals(searchNodeName)) {
            // Clearing the query will remove the search page
            SearchView symbolSearchView = findViewById(R.id.symbol_picker_search);
            symbolSearchView.setQuery("", false);
            symbolSearchView.clearFocus();
        } else if (pageTrail.size() > 1) {
            // Go to next higher level
            pageTrail.pop();
            updateSelectedSymbol(pageTrail.pop());
        } else {
            // Ask if the user wishes to exit
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Exit")
                    .setMessage("Do you want to exit the symbol picker?")
                    .setPositiveButton(android.R.string.cancel, null)
                    .setNegativeButton(android.R.string.ok, (dialog, whichButton) -> {
                        // Exit symbol picker return blank symbol code
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(selectedSymbolIdKey, "");
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    })
                    .show();
        }
    }

    // Updates symbol preview on modifier change
    private class ModifierSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            updateSymbolPreview();

            // Disables altitudeModeSpinner if user selects "FL" (flight level) because it doesn't use an altitude mode
            // (though the selected Mode would be ignored anyway)
            if (altitudeUnitSpinner != null && altitudeModeSpinner != null) {
                altitudeModeSpinner.setEnabled(altitudeUnitSpinner.getSelectedItemPosition() != AltitudeUnits.valueOf("FL").index);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            updateSymbolPreview();
        }
    }

    // Called when "configure symbol" is clicked
    private void onConfigureSymbol() {
        // There's no good way to send a SparseArray<String> in an Intent, so use HashMaps here and
        // the calling application can build SparseArrays from them.
        modifiersToSend = new HashMap<>();
        attributesToSend = new HashMap<>();

        final int selectedSymbolVersion = Integer.parseInt(selectedSymbolNode.getVersion());
        final String selectedSymbolSet = selectedSymbolNode.getSymbolSetCode();
        final String selectedSymbolEntityCode = selectedSymbolNode.getCode();
        final String selectedSymbolName = selectedSymbolNode.getName();

        // TODO temporarily bypasses weather all modifiers because the extra ones that are applicable are not implemented yet (and thus sends empty modifiers HashMap back up)
        switch (Integer.parseInt(selectedSymbolSet)) {
            case SymbolID.SymbolSet_Atmospheric:
            case SymbolID.SymbolSet_Oceanographic:
            case SymbolID.SymbolSet_MeteorologicalSpace:
                // for debugging modifiers:
                /*MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolSetCode + selectedSymbolEntityCode,0);
                ArrayList<Integer> initialModifiers = msi.getModifiers();
                ArrayList<Integer> ignoreModifiers = Modifiers.GetPredeterminedModifiersList();
                modifiersToGet = new ArrayList<>();
                for (int i : initialModifiers) {
                    if (!ignoreModifiers.contains(i)) {
                        modifiersToGet.add(i);
                        Log.d("onSelectPressed", "added modifier: " + i);
                    }
                }*/

                // construct neutral present (---4--0) code for all weather symbols
                String weatherSymbolID = selectedSymbolVersion + "04" + selectedSymbolSet + "0000" + selectedSymbolEntityCode + "00000000000000";
                Intent resultIntent = new Intent();
                resultIntent.putExtra(selectedSymbolIdKey, weatherSymbolID);
                resultIntent.putExtra(modifiersKey, modifiersToSend);
                resultIntent.putExtra(attributesKey, attributesToSend);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                return;
        }

        mainModifiersDialog = new Dialog(this);
        mainModifiersDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mainModifiersDialog.setContentView(R.layout.modifiers_page1);
        mainModifiersDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        mainModifiersDialog.show();

        contextRadioGroup = mainModifiersDialog.findViewById(R.id.context_radio_group);
        contextRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> updateSymbolPreview());

        stdIdSpinner = mainModifiersDialog.findViewById(R.id.std_id_spinner);
        ArrayAdapter<CharSequence> stdIdAdapter = ArrayAdapter.createFromResource(this,
                R.array.std_id_array, android.R.layout.simple_spinner_item);
        stdIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stdIdSpinner.setAdapter(stdIdAdapter);
        stdIdSpinner.setSelection(3); // Default to friendly identity
        stdIdSpinner.setOnItemSelectedListener(new ModifierSpinnerListener());

        statusSpinner = mainModifiersDialog.findViewById(R.id.status_spinner);
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.status_array, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);
        statusSpinner.setOnItemSelectedListener(new ModifierSpinnerListener());

        hqSpinner = mainModifiersDialog.findViewById(R.id.hq_spinner);
        ArrayAdapter<CharSequence> hqAdapter = ArrayAdapter.createFromResource(this,
                R.array.hq_array, android.R.layout.simple_spinner_item);
        hqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hqSpinner.setAdapter(hqAdapter);
        hqSpinner.setOnItemSelectedListener(new ModifierSpinnerListener());

        ampCategorySpinner = mainModifiersDialog.findViewById(R.id.amplifier_category_spinner);
        ArrayAdapter<CharSequence> ampCategoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.amplifier_category_array, android.R.layout.simple_spinner_item);
        ampCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ampCategorySpinner.setAdapter(ampCategoryAdapter);
        ampCategorySpinner.setOnItemSelectedListener(new AmplifierSelectedItemListener());

        ampTypeLayout = mainModifiersDialog.findViewById(R.id.amplifier_type_layout);
        ampTypeLayout.setVisibility(View.GONE);

        ampTypeSpinner = mainModifiersDialog.findViewById(R.id.amplifier_type_spinner);
        ArrayAdapter<CharSequence> ampTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.amp0_unknown_array, android.R.layout.simple_spinner_item);
        ampTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ampTypeSpinner.setAdapter(ampTypeAdapter);
        ampTypeSpinner.setOnItemSelectedListener(new ModifierSpinnerListener());

        int sector1ArrayID;
        int sector2ArrayID;

        switch (Integer.parseInt(selectedSymbolSet)) {
            case SymbolID.SymbolSet_Air:
                sector1ArrayID = R.array.ss_Air_sector1_array;
                sector2ArrayID = R.array.ss_Air_sector2_array;
                break;
            case SymbolID.SymbolSet_AirMissile:
                sector1ArrayID = R.array.ss_AirMissile_sector1_array;
                sector2ArrayID = R.array.ss_AirMissile_sector2_array;
                break;
            case SymbolID.SymbolSet_Space:
                sector1ArrayID = R.array.ss_Space_sector1_array;
                sector2ArrayID = R.array.ss_Space_sector2_array;
                break;
            case SymbolID.SymbolSet_SpaceMissile:
                sector1ArrayID = R.array.ss_SpaceMissile_sector1_array;
                sector2ArrayID = R.array.ss_SpaceMissile_sector2_array;
                break;
            case SymbolID.SymbolSet_LandUnit:
                sector1ArrayID = R.array.ss_LandUnit_sector1_array;
                sector2ArrayID = R.array.ss_LandUnit_sector2_array;
                break;
            case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                sector1ArrayID = R.array.ss_LandCivilianUnitOrganization_sector1_array;
                sector2ArrayID = R.array.ss_LandCivilianUnitOrganization_sector2_array;
                break;
            case SymbolID.SymbolSet_LandEquipment:
                sector1ArrayID = R.array.ss_LandEquipment_sector1_array;
                sector2ArrayID = R.array.ss_LandEquipment_sector2_array;
                break;
            case SymbolID.SymbolSet_LandInstallation:
                sector1ArrayID = R.array.ss_LandInstallation_sector1_array;
                sector2ArrayID = R.array.ss_LandInstallation_sector2_array;
                break;
            case SymbolID.SymbolSet_ControlMeasure:
                // Control Measures only use Sector 1
                sector1ArrayID = R.array.ss_ControlMeasure_sector1_array;
                sector2ArrayID = R.array.ss_NotApplicable_array;
                break;
            case SymbolID.SymbolSet_SeaSurface:
                sector1ArrayID = R.array.ss_SeaSurface_sector1_array;
                sector2ArrayID = R.array.ss_SeaSurface_sector2_array;
                break;
            case SymbolID.SymbolSet_SeaSubsurface:
                sector1ArrayID = R.array.ss_SeaSubsurface_sector1_array;
                sector2ArrayID = R.array.ss_SeaSubsurface_sector2_array;
                break;
            case SymbolID.SymbolSet_Activities:
                sector1ArrayID = R.array.ss_Activities_sector1_array;
                sector2ArrayID = R.array.ss_Activities_sector2_array;
                break;
            case SymbolID.SymbolSet_SignalsIntelligence_Space:
            case SymbolID.SymbolSet_SignalsIntelligence_Air:
            case SymbolID.SymbolSet_SignalsIntelligence_Land:
            case SymbolID.SymbolSet_SignalsIntelligence_SeaSurface:
            case SymbolID.SymbolSet_SignalsIntelligence_SeaSubsurface:
                sector1ArrayID = R.array.ss_SignalsIntelligence_sector1_array;
                sector2ArrayID = R.array.ss_SignalsIntelligence_sector2_array;
                break;
            default:
                // Unknown, MineWarfare, Cyberspace
                // (also Atmospheric, Oceanographic, and MeteorologicalSpace if we don't skip their modifier dialogs)
                sector1ArrayID = R.array.ss_NotApplicable_array;
                sector2ArrayID = R.array.ss_NotApplicable_array;
                break;
        }

        View sectorsView = mainModifiersDialog.findViewById(R.id.sectors_layout);
        if (sector1ArrayID == R.array.ss_NotApplicable_array) {
            sectorsView.setVisibility(View.GONE);
            hasSectorModifiers = false;
        } else {
            sectorsView.setVisibility(View.VISIBLE);
            hasSectorModifiers = true;

            sector1Spinner = mainModifiersDialog.findViewById(R.id.sector_1_spinner);
            ArrayAdapter<CharSequence> sector1Adapter;
            String[] landUnitSector1Mods = getResources().getStringArray(R.array.ss_LandUnit_sector1_array);
            if (sector1ArrayID == R.array.ss_LandUnit_sector1_array) {
                // create custom adapter to skip two "{Reserved for future use}" codes without altering the positions of the rest
                sector1Adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, landUnitSector1Mods) {
                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View v;

                        if ("{Reserved for future use}".equals(landUnitSector1Mods[position])) {
                            TextView tv = new TextView(getContext());
                            tv.setHeight(0);
                            tv.setVisibility(View.GONE);
                            v = tv;
                        } else {
                            // Pass convertView as null to prevent reuse of special case views
                            v = super.getDropDownView(position, null, parent);
                        }
                        return v;
                    }
                };
            } else {
                sector1Adapter = ArrayAdapter.createFromResource(this,
                        sector1ArrayID, android.R.layout.simple_spinner_item);
            }

            sector1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sector1Spinner.setAdapter(sector1Adapter);
            sector1Spinner.setOnItemSelectedListener(new ModifierSpinnerListener());

            sector2Spinner = mainModifiersDialog.findViewById(R.id.sector_2_spinner);
            ArrayAdapter<CharSequence> sector2Adapter = ArrayAdapter.createFromResource(this,
                    sector2ArrayID, android.R.layout.simple_spinner_item);
            sector2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sector2Spinner.setAdapter(sector2Adapter);
            sector2Spinner.setOnItemSelectedListener(new ModifierSpinnerListener());
        }

        boolean isControlMeasure = (Integer.parseInt(selectedSymbolSet) == SymbolID.SymbolSet_ControlMeasure);
        if (isControlMeasure) {
            // set up Mines checkboxes for Control Measures
            View mineContainer = mainModifiersDialog.findViewById(R.id.mine_type_layout);
            sector1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == SECTOR_1_MINE_INDEX) {
                        mineContainer.setVisibility(View.VISIBLE);
                    } else {
                        mineContainer.setVisibility(View.GONE);
                    }
                    updateSymbolPreview(); // necessary since this ItemSelectedListener overwrites the previously set ModifierSpinnerListener
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Auto-generated method stub
                }
            });

            String line;
            String[] segments;
            mineSectorLookup = new SparseArray<>(51); // needs space for indices up to 50
            try (InputStream in = this.getResources().openRawResource(R.raw.mine_bitmasks_to_codes);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                while ((line = reader.readLine()) != null) {
                    segments = line.split("\t+");
                    // converts segments[0] binary string to int index, and reads segments[1] as string for symbol code
                    mineSectorLookup.put(Integer.parseInt(segments[0], 2), segments[1]);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            AntipersonnelMineBox = mainModifiersDialog.findViewById(R.id.antipersonnel_mine_checkbox);
            AntipersonnelDirectionalMineBox = mainModifiersDialog.findViewById(R.id.antipersonnel_mine_directional_checkbox);
            AntitankMineBox = mainModifiersDialog.findViewById(R.id.antitank_mine_checkbox);
            AntitankAntihandlingMineBox = mainModifiersDialog.findViewById(R.id.antitank_mine_antihandling_checkbox);
            WideAreaAntitankMineBox = mainModifiersDialog.findViewById(R.id.wide_area_antitank_mine_checkbox);
            MineClusterBox = mainModifiersDialog.findViewById(R.id.mine_cluster_checkbox);

            mineCheckBoxList = new ArrayList<>();
            mineCheckBoxList.add(AntipersonnelMineBox);
            mineCheckBoxList.add(AntipersonnelDirectionalMineBox);
            mineCheckBoxList.add(AntitankMineBox);
            mineCheckBoxList.add(AntitankAntihandlingMineBox);
            mineCheckBoxList.add(WideAreaAntitankMineBox);
            mineCheckBoxList.add(MineClusterBox);

            CompoundButton.OnCheckedChangeListener mineSelectionLimiter = (cb, isChecked) -> {
                if (isChecked) {
                    numMinesChecked++;
                    if (numMinesChecked == 3) {
                        for (CheckBox mine : mineCheckBoxList) {
                            if (!mine.isChecked()) {
                                mine.setEnabled(false);
                            }
                        }
                    }
                } else {
                    numMinesChecked--;
                    for (CheckBox mine : mineCheckBoxList) {
                        mine.setEnabled(true);
                    }
                }
                updateSymbolPreview();
            };

            for (CheckBox mine : mineCheckBoxList) {
                mine.setOnCheckedChangeListener(mineSelectionLimiter);
            }
        }

        View countryView = mainModifiersDialog.findViewById(R.id.country_layout);
        MSInfo msi = MSLookup.getInstance().getMSLInfo(selectedSymbolSet + selectedSymbolEntityCode, selectedSymbolVersion);
        if (!msi.getModifiers().contains(Modifiers.AS_COUNTRY)) {
            countryView.setVisibility(View.GONE);
            hasCountryModifier = false;
        } else {
            countryView.setVisibility(View.VISIBLE);
            hasCountryModifier = true;
            countryTextView = mainModifiersDialog.findViewById(R.id.country_textview);
            countryTextView.setBackgroundColor(Color.TRANSPARENT);

            countryTextView.setOnClickListener(v -> {
                countrySearchDialog = new Dialog(this);
                countrySearchDialog.setContentView(R.layout.country_selector);
                countrySearchDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                countrySearchDialog.show();

                EditText editText = countrySearchDialog.findViewById(R.id.country_edittext);
                editText.setBackgroundColor(Color.TRANSPARENT);
                ListView listView = countrySearchDialog.findViewById(R.id.country_listview);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, countryNames);
                listView.setAdapter(adapter);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // Auto-generated method stub
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // Auto-generated method stub
                    }
                });

                listView.setOnItemClickListener((parent, view, position, id) -> {
                    countryTextView.setText(adapter.getItem(position));
                    countrySearchDialog.dismiss();
                    updateSymbolPreview();
                });
            });
        }

        Button modifiersBackButton = mainModifiersDialog.findViewById(R.id.modifiers_page1_back_button);
        modifiersBackButton.setOnClickListener(w -> mainModifiersDialog.dismiss());

        ViewGroup symbolPreviewView = mainModifiersDialog.findViewById(R.id.symbol_picker_modifiers_preview);
        TextView symbolPreviewTV = symbolPreviewView.findViewById(R.id.symbol_picker_cell_TV);
        symbolPreviewTV.setText(selectedSymbolName);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(symbolPreviewTV, 8,
                12, 1, TypedValue.COMPLEX_UNIT_SP);
        ViewGroup.LayoutParams params = symbolPreviewView.getLayoutParams();
        params.height = cellSize;
        params.width = cellSize;
        symbolPreviewView.setLayoutParams(params);

        Button sendButton = mainModifiersDialog.findViewById(R.id.send_button);
        sendButton.setOnClickListener(view -> onPickSymbol());
        sendButton.setText(getString(R.string.send_btn_label, "'" + selectedSymbolName + "'"));

        // build list of modifiers that aren't already determined by the symbol code
        ArrayList<String> initialModifiers = msi.getModifiers();
        ArrayList<String> ignoreModifiers = Modifiers.GetSymbolCodeModifiersList();
        modifiersToGet = new ArrayList<>();
        for (String i : initialModifiers) {
            if (!ignoreModifiers.contains(i)) {
                modifiersToGet.add(i);
            }
        }

        Button extraModifiersButton = mainModifiersDialog.findViewById(R.id.extra_modifiers_button);
        if (!modifiersToGet.isEmpty() || isControlMeasure) {
            extraModifiersButton.setEnabled(true);
            extraModifiersButton.setOnClickListener(v -> {
                extraModifiersDialog = new Dialog(this);
                extraModifiersDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                extraModifiersDialog.setContentView(R.layout.modifiers_page2);
                extraModifiersDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                extraModifiersDialog.show();

                altitudeUnitSpinner = null;
                altitudeModeSpinner = null;

                // Attributes
                LinearLayout attributesLayout = extraModifiersDialog.findViewById(R.id.attributes_layout);
                CheckBox outlineCheckbox = extraModifiersDialog.findViewById(R.id.outline_checkbox);
                // show outline checkbox just for single-point Control Measures
                String tempCode = selectedSymbolVersion + "03" + selectedSymbolSet + "0000" + selectedSymbolEntityCode + "0000";
                if (isControlMeasure && SymbolUtilities.isMultiPoint(tempCode) == false) {
                    outlineCheckbox.setVisibility(View.VISIBLE);
                }

                EditText lineColorField = extraModifiersDialog.findViewById(R.id.edit_LineColor);
                EditText fillColorField = extraModifiersDialog.findViewById(R.id.edit_FillColor);
                EditText lineWidthField = extraModifiersDialog.findViewById(R.id.edit_LineWidth);
                EditText textColorField = extraModifiersDialog.findViewById(R.id.edit_TextColor);
                if (isControlMeasure) {
                    attributesLayout.setVisibility(View.VISIBLE);
                    // restores previous values if dialog was reopened
                    outlineCheckbox.setChecked(Boolean.parseBoolean(attributesToSend.get(MilStdAttributes.OutlineSymbol)));
                    lineColorField.setText(attributesToSend.getOrDefault(MilStdAttributes.LineColor, ""));
                    fillColorField.setText(attributesToSend.getOrDefault(MilStdAttributes.FillColor, ""));
                    lineWidthField.setText(attributesToSend.getOrDefault(MilStdAttributes.LineWidth, ""));
                    textColorField.setText(attributesToSend.getOrDefault(MilStdAttributes.TextColor, ""));
                }

                // Extra modifiers
                LinearLayout ll = extraModifiersDialog.findViewById(R.id.modifiers_layout);
                LinearLayout unitLayout = null;

                for (String i : modifiersToGet) {
                    EditText et = new EditText(this);
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    et.setLayoutParams(p);

                    // set maximum char length for the field?
                    /*
                    et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3) });
                    //Disabling suggestions seems to be the only way to stop the buffer from
                    //filling past the length limit if you keep typing.
                    et.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    */

                    et.setHint(Modifiers.getModifierLetterCode(i) + ": " + Modifiers.getModifierName(i));
                    //et.setId(i);
                    et.setId(convertStringIDtoInt(i));
                    et.setText(modifiersToSend.getOrDefault(i, "")); // restores previous values if dialog was reopened

                    // DateTime fields
                    if (i == Modifiers.W_DTG_1 || i == Modifiers.W1_DTG_2) {
                        et.setInputType(InputType.TYPE_NULL); // prevents users from typing directly in datetime field
                        // ensures the EditText only has to be clicked once to show dialog
                        et.setOnFocusChangeListener((d, hasFocus) -> {
                            if (hasFocus)
                                callDatetimeDialog(et);
                        });
                        et.setOnClickListener(l -> callDatetimeDialog(et));
                    }

                    // Dynamically add spinners for multi-point graphics' X (altitude/depth) Units and Mode.
                    // (For single-point graphics, the X field is free-type.)
                    if (i == Modifiers.X_ALTITUDE_DEPTH) {
                        if (isControlMeasure) {
                            LinearLayout.LayoutParams altParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                            // build unit array, label, and spinner
                            List<String> unitArray = new ArrayList<>();
                            for (AltitudeUnits u : AltitudeUnits.values()) {
                                unitArray.add(u.name() + " " + u.desc);
                            }

                            unitLayout = new LinearLayout(this);
                            unitLayout.setLayoutParams(altParams);
                            TextView unitLabel = new TextView(this);
                            unitLabel.setText(R.string.altitude_unit_label);

                            altitudeUnitSpinner = new Spinner(this);
                            ArrayAdapter<String> unitAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, unitArray) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View v = super.getView(position, convertView, parent);
                                    v.setMinimumHeight((int) (48*this.getContext().getResources().getDisplayMetrics().density));
                                    return v;
                                }
                            };
                            unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            altitudeUnitSpinner.setAdapter(unitAdapter);
                            altitudeUnitSpinner.setOnItemSelectedListener(new ModifierSpinnerListener());

                            unitLayout.addView(unitLabel);
                            unitLayout.addView(altitudeUnitSpinner);

                            // build mode array, label, and spinner
                            List<String> modeArray = new ArrayList<>();
                            for (AltitudeModes m : AltitudeModes.values()) {
                                modeArray.add(m.name() + " " + m.desc);
                            }

                            LinearLayout modeLayout = new LinearLayout(this);
                            modeLayout.setLayoutParams(altParams);
                            TextView modeLabel = new TextView(this);
                            modeLabel.setText(R.string.altitude_mode_label);

                            altitudeModeSpinner = new Spinner(this);
                            ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, modeArray) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View v = super.getView(position, convertView, parent);
                                    v.setMinimumHeight((int) (48*this.getContext().getResources().getDisplayMetrics().density));
                                    return v;
                                }
                            };
                            modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            altitudeModeSpinner.setAdapter(modeAdapter);
                            altitudeModeSpinner.setOnItemSelectedListener(new ModifierSpinnerListener());

                            modeLayout.addView(modeLabel);
                            modeLayout.addView(altitudeModeSpinner);

                            // change multi-point X hint and add X, Units, and Mode fields to the main view
                            et.setHint(Modifiers.getModifierLetterCode(i) + ": " + Modifiers.getModifierName(i) + "\n(Type one or more comma-separated numbers)");
                            ll.addView(et);
                            ll.addView(unitLayout);
                            ll.addView(modeLayout);

                            // if the calling application passes any invalid values for altitude units or mode, this sets both to the defaults
                            try {
                                altitudeUnitSpinner.setSelection(AltitudeUnits.valueOf(attributesToSend.getOrDefault(MilStdAttributes.AltitudeUnits, "1,M").split(",")[1]).index);
                                altitudeModeSpinner.setSelection(AltitudeModes.valueOf(attributesToSend.getOrDefault(MilStdAttributes.AltitudeMode, "AMSL")).index);
                            } catch (Exception e) {
                                altitudeUnitSpinner.setSelection(AltitudeUnits.valueOf("M").index);
                                altitudeModeSpinner.setSelection(AltitudeModes.valueOf("AMSL").index);
                            }
                            continue;
                        } else {
                            // change single-point X hint
                            et.setHint(Modifiers.getModifierLetterCode(i) + ": " + Modifiers.getModifierName(i) + "\n(Type anything, e.g. 500 M AMSL)");
                        }
                    }
                    ll.addView(et);
                }

                Button saveExtraModsButton = extraModifiersDialog.findViewById(R.id.modifiers_dialog_save_button);
                saveExtraModsButton.setOnClickListener(w -> {
                    // save attributes
                    if (isControlMeasure) {
                        attributesToSend.put(MilStdAttributes.OutlineSymbol, String.valueOf(outlineCheckbox.isChecked()));

                        String value = String.valueOf(lineColorField.getText());
                        if (value.isEmpty()) {
                            attributesToSend.remove(MilStdAttributes.LineColor);
                        } else {
                            attributesToSend.put(MilStdAttributes.LineColor, value);
                        }

                        value = String.valueOf(fillColorField.getText());
                        if (value.isEmpty()) {
                            attributesToSend.remove(MilStdAttributes.FillColor);
                        } else {
                            attributesToSend.put(MilStdAttributes.FillColor, value);
                        }

                        value = String.valueOf(lineWidthField.getText());
                        if (value.isEmpty()) {
                            attributesToSend.remove(MilStdAttributes.LineWidth);
                        } else {
                            attributesToSend.put(MilStdAttributes.LineWidth, value);
                        }

                        value = String.valueOf(textColorField.getText());
                        if (value.isEmpty()) {
                            attributesToSend.remove(MilStdAttributes.TextColor);
                        } else {
                            attributesToSend.put(MilStdAttributes.TextColor, value);
                        }
                    }

                    if (altitudeUnitSpinner != null && altitudeUnitSpinner.getVisibility() == View.VISIBLE) {
                        // We are assuming the user will not do any unit conversion--they will always type a value in (e.g.) meters if they want to display meters.
                        // Therefore the AltitudeUnits string should always be "1,<x>" where <x> is one of M/KM/FT/SM/FL because the conversion factor will always be 1.
                        attributesToSend.put(MilStdAttributes.AltitudeUnits, "1," + ((String) altitudeUnitSpinner.getSelectedItem()).split(" ")[0]);
                        attributesToSend.put(MilStdAttributes.AltitudeMode, ((String) altitudeModeSpinner.getSelectedItem()).split(" ")[0]);
                    }

                    // save extra modifiers
                    for (String j : modifiersToGet) {
                        EditText et = extraModifiersDialog.findViewById(convertStringIDtoInt(j));
                        String value = String.valueOf(et.getText());
                        if (value.isEmpty()) {
                            modifiersToSend.remove(j);
                        } else {
                            modifiersToSend.put(j, value);
                        }
                    }
                    extraModifiersDialog.dismiss();
                    updateSymbolPreview();
                });
            });
            updateSymbolPreview();
        } else {
            extraModifiersButton.setEnabled(false);
        }
    }

    /**
     * Used to call Datetime selector from Extra Modifiers page (should only be called from that page)
     * @param et Datetime EditText field that called this dialog
     */
    private void callDatetimeDialog(EditText et) {
        Dialog datetimeDialog = new Dialog(this);
        datetimeDialog.setContentView(R.layout.datetime_selector);
        datetimeDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        datetimeDialog.show();

        DatePicker datePicker = datetimeDialog.findViewById(R.id.datePicker);
        TimePicker timePicker = datetimeDialog.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        Button nowButton = datetimeDialog.findViewById(R.id.datetime_now_button);
        nowButton.setOnClickListener(n -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            datePicker.init(year, month, day, null);
            timePicker.setHour(c.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(c.get(Calendar.MINUTE));
        });
        nowButton.callOnClick();

        Button onOrderButton = datetimeDialog.findViewById(R.id.datetime_on_order_button);
        onOrderButton.setOnClickListener(o -> {
            et.setText("O/O");
            datetimeDialog.dismiss();
            updateSymbolPreview();
        });

        Button confirmTimeButton = datetimeDialog.findViewById(R.id.datetime_confirm_button);
        confirmTimeButton.setOnClickListener(c -> {
            Calendar myCalendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute());
            Date selectedDate = myCalendar.getTime();
            et.setText(SymbolUtilities.getDateLabel(selectedDate));
            datetimeDialog.dismiss();
            updateSymbolPreview();
        });
    }

    private int getMineBitmask() {
        int mineBitmask = 0;

        if (Integer.parseInt(selectedSymbolNode.getSymbolSetCode()) == SymbolID.SymbolSet_ControlMeasure) {
            if (AntipersonnelMineBox.isChecked())
                mineBitmask |= ANTIPERSONNEL_MINE;
            if (AntipersonnelDirectionalMineBox.isChecked())
                mineBitmask |= ANTIPERSONNEL_MINE_DIRECTIONAL;
            if (AntitankMineBox.isChecked())
                mineBitmask |= ANTITANK_MINE;
            if (AntitankAntihandlingMineBox.isChecked())
                mineBitmask |= ANTITANK_MINE_ANTIHANDLING;
            if (WideAreaAntitankMineBox.isChecked())
                mineBitmask |= WIDE_AREA_ANTITANK_MINE;
            if (MineClusterBox.isChecked())
                mineBitmask |= MINE_CLUSTER;
        }
        return mineBitmask;
    }

    // Updates preview in configure symbol page
    // Uses modifiers - different than update selected symbol
    private void updateSymbolPreview() {
        // update Control Measures modifier N ("ENY" if hostile)
        boolean isHostile = (SymbolID.getAffiliation(getSymbolID()) == SymbolID.StandardIdentity_Affiliation_Hostile_Faker);
        modifiersToSend.put(Modifiers.N_HOSTILE, isHostile ? "ENY" : null);

        Map<String,String> modifiers = new HashMap<>();
        for (String i : modifiersToSend.keySet()) {
            modifiers.put(i, modifiersToSend.get(i));
        }
        Map<String,String> attributes = new HashMap<>();
        for (String i : attributesToSend.keySet()) {
            attributes.put(i, attributesToSend.get(i));
        }
        attributes.put(MilStdAttributes.PixelSize, "240");

        ImageInfo ii = mir.RenderIcon(getSymbolID(), modifiers, attributes);

        ViewGroup symbolPreviewView = mainModifiersDialog.findViewById(R.id.symbol_picker_modifiers_preview);
        ImageView symbolPreviewIV = symbolPreviewView.findViewById(R.id.symbol_picker_cell_IV);
        symbolPreviewIV.setBackgroundColor(Color.LTGRAY);

        if (ii != null && ii.getImage() != null)
            symbolPreviewIV.setImageBitmap(ii.getImage());
        else
            // If can't render show empty preview
            symbolPreviewIV.setImageBitmap(emptyBitmap);
    }

    // Returns symbol id with no modifiers just symbol set and entity code in correct positions
    private String getGenericSymbolID(Node symbolNode) {
        return symbolNode.getVersion() + "03" + symbolNode.getSymbolSetCode() + "0011" + symbolNode.getCode() + "00000000000000";
    }

    // Includes modifiers in code
    private String getSymbolID() {
        final String selectedSymbolSet = selectedSymbolNode.getSymbolSetCode();
        final String selectedSymbolEntityCode = selectedSymbolNode.getCode();

        // Resource IDs will be non-final by default in Android Gradle Plugin version 8.0,
        // so Google recommends using if/else for them instead of switch statements.
        int checkedRadioButtonId = contextRadioGroup.getCheckedRadioButtonId();
        String contextCode;
        if (checkedRadioButtonId == R.id.context_radio_exercise) {
            contextCode = String.valueOf(SymbolID.StandardIdentity_Context_Exercise);
        } else if (checkedRadioButtonId == R.id.context_radio_simulation) {
            contextCode = String.valueOf(SymbolID.StandardIdentity_Context_Simulation);
        } else {
            contextCode = String.valueOf(SymbolID.StandardIdentity_Context_Reality);
        }

        // For spinners, the selected item position equals the digit to use in the symbol code
        // (except for ampTypeSpinner, which is position+1 when not category Unknown).
        String stdIdCode = String.valueOf(stdIdSpinner.getSelectedItemPosition());
        String statusCode = String.valueOf(statusSpinner.getSelectedItemPosition());
        String hqCode = String.valueOf(hqSpinner.getSelectedItemPosition());

        String amplifierCode = "00";
        if (ampCategorySpinner.getSelectedItemPosition() != 0) {
            amplifierCode = ampCategorySpinner.getSelectedItemPosition() +
                    String.valueOf(ampTypeSpinner.getSelectedItemPosition() + 1);
        }

        String sector1Code = "00";
        String sector2Code = "00";
        if (hasSectorModifiers) {
            DecimalFormat twoDigitFormatter = new DecimalFormat("00");
            sector1Code = twoDigitFormatter.format(sector1Spinner.getSelectedItemPosition());
            sector2Code = twoDigitFormatter.format(sector2Spinner.getSelectedItemPosition());

            // (Control Measures) Gets symbol code section matching mineBitmask, or returns value for "Unspecified Mine" if combination is not found.
            if (Integer.parseInt(selectedSymbolSet) == SymbolID.SymbolSet_ControlMeasure && Integer.parseInt(sector1Code) == SECTOR_1_MINE_INDEX) {
                sector1Code = mineSectorLookup.get(getMineBitmask(), String.valueOf(SECTOR_1_MINE_INDEX));
            }
        }

        String countryCode = "000";
        if (hasCountryModifier) {
            String key = (String) countryTextView.getText();
            if (!Objects.equals(key, "")) {
                countryCode = countryMap.get(key);
            }
        }

        return selectedSymbolNode.getVersion() + contextCode + stdIdCode + selectedSymbolSet + statusCode + hqCode + amplifierCode +
                selectedSymbolEntityCode + sector1Code + sector2Code + "0000000" + countryCode;
    }

    // Called when pick button is selected from the configure window
    private void onPickSymbol() {
        String canRender;
        int mineBitmask = getMineBitmask();
        final String symbolID = getSymbolID();
        if (SymbolUtilities.isMultiPoint(symbolID)) {
            Map<String,String> modifiers = new HashMap<>();
            for (String i : modifiersToSend.keySet()) {
                modifiers.put(i, modifiersToSend.get(i));
            }
            canRender = MultiPointHandler.canRenderMultiPoint(symbolID, modifiers, Integer.MAX_VALUE);
            if (!canRender.equals("true")) {
                // Clean up error message for user
                canRender = selectedSymbolNode.getName() + canRender.substring(30);
                canRender = canRender.replace("a modifiers object that has ", "");
            }
        } else {
            Map<String,String> attributes = new HashMap<>();
            for (String i : attributesToSend.keySet()) {
                attributes.put(i, attributesToSend.get(i));
            }
            if (MilStdIconRenderer.getInstance().CanRender(symbolID, attributes)) {
                canRender = "true";
            } else {
                // Shouldn't be able to get here with a single point that can't be rendered
                canRender = "Unable to render " + selectedSymbolNode.getName();
            }
        }

        if (invalidMineTrios.contains(mineBitmask)) {
            Toast.makeText(mainModifiersDialog.getContext(), "Invalid combination of mines. Please make a different selection.", Toast.LENGTH_LONG).show();
        } else if (canRender.equals("true")) {
            mainModifiersDialog.dismiss();
            Intent resultIntent = new Intent();
            resultIntent.putExtra(selectedSymbolIdKey, getSymbolID());
            resultIntent.putExtra(modifiersKey, modifiersToSend);
            resultIntent.putExtra(attributesKey, attributesToSend);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(mainModifiersDialog.getContext(), canRender, Toast.LENGTH_LONG).show();
        }
    }

    // Updates selected symbol variable, selected symbol preview and symbols on page if necessary
    private void updateSelectedSymbol(Node newSelectedSymbol) {
        // Update the selected symbol set code
        selectedSymbolNode = newSelectedSymbol;
        ImageView selectedSymbolIV = findViewById(R.id.selected_symbol_iv);
        Bitmap render = getRender(selectedSymbolNode);

        String selectedSymbolName;
        if (selectedSymbolNode.getName().equalsIgnoreCase("root")) {
            selectedSymbolName = "Symbol";
        } else {
            selectedSymbolName = selectedSymbolNode.getName();
        }
        configureButton.setText(getString(R.string.configure_btn_label, "'" + selectedSymbolName + "'"));

        if (render != null) {
            selectedSymbolIV.setImageBitmap(render);
            configureButton.setEnabled(canRender(getGenericSymbolID(selectedSymbolNode)));

            // change button text for weather symbols because the modifier dialogs are skipped
            switch (Integer.parseInt(selectedSymbolNode.getSymbolSetCode())) {
                case SymbolID.SymbolSet_Atmospheric:
                case SymbolID.SymbolSet_Oceanographic:
                case SymbolID.SymbolSet_MeteorologicalSpace:
                    configureButton.setText(getString(R.string.send_btn_label, "'" + selectedSymbolName + "'"));
            }
        } else {
            selectedSymbolIV.setImageResource(R.drawable.baseline_folder_24);
            configureButton.setEnabled(false);
        }

        if (!newSelectedSymbol.getChildren().isEmpty()) {
            pageTrail.add(selectedSymbolNode);
            updateSymbolTable();
        }
    }

    // Updates symbols in page based on top of pageTrail
    private void updateSymbolTable() {
        Node symbolTree = pageTrail.peek();
        ArrayList<Node> symbols; // Symbols to be in new page

        // Scroll to top
        GridView symbolTable = findViewById(R.id.symbol_picker_table);
        symbolTable.smoothScrollToPositionFromTop(0, 0, 0);

        if (!flattenTreeToggle.isChecked()) {
            symbols = new ArrayList<>(symbolTree.getChildren());
        } else {
            symbols = new ArrayList<>(symbolTree.flatten());
        }

        TextView symbolPath = findViewById(R.id.symbol_picker_path);

        StringBuilder pathStr = new StringBuilder();
        if (pageTrail.size() == 1) {
            pathStr.append("Home");
        } else if (activeSearch) {
            int searchNodeIndex = 1;
            while (!pageTrail.get(searchNodeIndex).getName().equals(searchNodeName)) {
                searchNodeIndex++;
            }

            pathStr.append("Search");
            for (int i = searchNodeIndex + 1; i < pageTrail.size(); i++)
                pathStr.append(" > ").append(pageTrail.get(i).getName());
        } else {
            pathStr.append(pageTrail.get(1).getName());
            for (int i = 2; i < pageTrail.size(); i++)
                pathStr.append(" > ").append(pageTrail.get(i).getName());
        }
        symbolPath.setText(pathStr);

        // (optional) sort symbols by name
        symbols.sort(Comparator.comparing(Node::getName));

        symbolTableAdapter.clear();
        symbolTableAdapter.addAll(symbols);
    }

    // returns null if can't render or other error
    // Does not include modifiers
    private Bitmap getRender(Node symbolNode) {
        final String version = symbolNode.getVersion();
        final String symbolSetCode = symbolNode.getSymbolSetCode();
        final String entityCode = symbolNode.getCode();
        String symbolID = getGenericSymbolID(symbolNode);

        if (version.equals("XX") || entityCode.equals("XXXXXX") || entityCode.equals("XX"))
            // Error in code
            return null;
        else if (entityCode.equals("000000") &&
                (symbolSetCode.equals(SymbolID.SymbolSet_Atmospheric + "") ||
                        symbolSetCode.equals(SymbolID.SymbolSet_ControlMeasure + "") ||
                        symbolSetCode.equals(SymbolID.SymbolSet_MineWarfare + "") ||
                        symbolSetCode.equals(SymbolID.SymbolSet_Oceanographic + "")))
            // Top level symbol set that can't be rendered
            return null;
        else if (!entityCode.equals("000000") && !canRender(symbolID))
            // Check if can render icon. canRender() says it can't render "000000" but RenderIcon() will
            // return an empty frame for the symbol sets not excluded above
            return null;

        Map<String,String> modifiers = new HashMap<>();
        Map<String,String> attributes = new HashMap<>();

        attributes.put(MilStdAttributes.PixelSize, "50");
        attributes.put(MilStdAttributes.DrawAsIcon, "true"); // Make all symbols same size

        ImageInfo ii = mir.RenderIcon(symbolID, modifiers, attributes);
        if (ii != null)
            return ii.getImage();
        else
            return null;
    }

    // Same functionality and MilStdIconRenderer.CanRender() - doesn't log if can't render
    // Symbol picker calls canRender() with symbols it doesn't expect to be valid
    private Boolean canRender(String symbolID) {
        int version = SymbolID.getVersion(symbolID);
        String lookupID = SymbolUtilities.getBasicSymbolID(symbolID);
        String lookupSVGID = SVGLookup.getMainIconID(symbolID);
        MSInfo msi = MSLookup.getInstance().getMSLInfo(lookupID,SymbolID.getVersion(symbolID));
        SVGInfo si = SVGLookup.getInstance().getSVGLInfo(lookupSVGID, version);

        // msi should never be null
        return msi != null && msi.getDrawRule() != DrawRules.DONOTDRAW && si != null;
    }

    private class AmplifierSelectedItemListener implements AdapterView.OnItemSelectedListener {
        @Override
        // specifically used for the Amplifier Category spinner to show/hide and change the subtype spinner
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            int arrayId;
            switch (ampCategorySpinner.getSelectedItemPosition()) {
                case 1:
                    arrayId = R.array.amp1_echelon_bb_array;
                    break;
                case 2:
                    arrayId = R.array.amp2_echelon_da_array;
                    break;
                case 3:
                    arrayId = R.array.amp3_eqp_land_array;
                    break;
                case 4:
                    arrayId = R.array.amp4_eqp_snow_array;
                    break;
                case 5:
                    arrayId = R.array.amp5_eqp_water_array;
                    break;
                case 6:
                    arrayId = R.array.amp6_naval_towed_array;
                    break;
                case 0:
                default:
                    arrayId = R.array.amp0_unknown_array;
                    break;
            }
            if (arrayId == R.array.amp0_unknown_array) {
                ampTypeLayout.setVisibility(View.GONE);
            } else {
                ampTypeLayout.setVisibility(View.VISIBLE);
            }
            ArrayAdapter<CharSequence> ampTypeAdapter = ArrayAdapter.createFromResource(SymbolPickerActivity.this,
                    arrayId, android.R.layout.simple_spinner_item);
            ampTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ampTypeSpinner.setAdapter(ampTypeAdapter);

            updateSymbolPreview(); // necessary for ampCategorySpinner since this AmplifierSelectedItemListener is used instead of ModifierSpinnerListener
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // Auto-generated method stub
        }
    }

    private class SearchBoxListener implements SearchView.OnQueryTextListener {
        private List<Node> searchSymbolTree(String searchQuery) {
            // Search by name
            List<Node> searchResults = treeManager.mil2525Tree.flatten().stream()
                    .filter(node -> node.getName().trim().toLowerCase(Locale.ROOT).replaceAll("-", " ")
                            .contains(searchQuery.toLowerCase(Locale.ROOT).replaceAll("-", " "))).collect(Collectors.toList());
            // Search by entity code with symbol set
            searchResults.addAll(treeManager.mil2525Tree.flatten().stream()
                    .filter(node -> (node.getSymbolSetCode() + node.getCode())
                            .contains(searchQuery)).collect(Collectors.toList()));
            // Search by entity code
            searchResults.addAll(treeManager.mil2525Tree.flatten().stream()
                    .filter(node -> node.getCode().contains(searchQuery)).collect(Collectors.toList()));

            // Remove duplicates
            searchResults = searchResults.stream().distinct().collect(Collectors.toList());

            return searchResults;
        }

        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        // Removes search page if query is empty or updates table with search results
        @Override
        public boolean onQueryTextChange(String s) {
            // Remove all pages at and below searchNode
            while (activeSearch) {
                if (pageTrail.pop().getName().equals(searchNodeName)) {
                    activeSearch = false;
                }
            }

            if (s != null && !s.equals("")) {
                List<Node> searchResults = searchSymbolTree(s);
                Node searchNode = new Node(searchNodeName, "XX", "XX", "XX");
                searchNode.addChildren(searchResults);
                pageTrail.add(searchNode);
                activeSearch = true;
            }
            updateSymbolTable();
            return true;
        }
    }

    /**
     * Symbol Grid View Adapter
     * Renders views for symbols from ArrayList of tree nodes for each page
     */
    private class SymbolGVAdapter extends ArrayAdapter<Node> {
        public SymbolGVAdapter(Context context, ArrayList<Node> nodeArrayList) {
            super(context, 0, nodeArrayList);
        }

        // Gets individual view for symbol for table
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Node symbolTree = getItem(position);

            if (convertView == null) {
                // Layout Inflater inflates each item to be displayed in GridView
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.symbol_cell, parent, false);
            }
            ViewGroup symbolCell = (ViewGroup) convertView;

            // On cell click either open folder or select symbol
            symbolCell.setOnClickListener(view -> updateSelectedSymbol(symbolTree));

            TextView symbolTV = symbolCell.findViewById(R.id.symbol_picker_cell_TV);
            symbolTV.setText(symbolTree.getName());
            // Add auto text sizing to fit longer symbol names
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(symbolTV, 8,
                    12, 1, TypedValue.COMPLEX_UNIT_SP);
            ImageView symbolIV = symbolCell.findViewById(R.id.symbol_picker_cell_IV);
            Bitmap render = getRender(symbolTree);
            if (render != null)
                // Whether folder or leaf set icon if can render
                symbolIV.setImageBitmap(render);
            else if (!symbolTree.getChildren().isEmpty())
                // Can't render use folder icon
                symbolIV.setImageResource(R.drawable.baseline_folder_24);
            else
                // Can't render leaf node
                symbolIV.setImageBitmap(emptyBitmap);

            // Add a folder icon in corner if rendering a symbol with children
            ImageView cellFolder = symbolCell.findViewById(R.id.symbol_picker_cell_folder);
            if (render != null && !symbolTree.getChildren().isEmpty())
                cellFolder.setImageResource(R.drawable.baseline_folder_24);
            else
                cellFolder.setImageBitmap(emptyBitmap);

            ViewGroup.LayoutParams params = symbolCell.getLayoutParams();
            params.height = cellSize;
            params.width = cellSize;
            symbolCell.setLayoutParams(params);

            return symbolCell;
        }
    }

    private int convertStringIDtoInt(String key)
    {
        try {
            StringBuilder sb = new StringBuilder();
            if (key != null & key.length() > 0)
            {
                /*for (int i = 0; i < key.length(); i++) {
                    String temp = String.valueOf((int) key.charAt(i));
                    if (temp.length() == 1)
                        temp = "0" + temp;
                    sb.append(temp);
                }//*/
                for (int i = 0; (i < 3); i++) {
                    String temp = String.valueOf((int) key.charAt(i));
                    sb.append(temp);
                }
                return Integer.parseInt(sb.toString());
            } else
                return -1;
        }
        catch(Exception exc)
        {
            System.out.println(exc.getMessage());
        }
        return -1;
    }

    private String convertIntToStringID(int id)
    {
        if(id==-1)
            return null;

        StringBuilder sb = new StringBuilder();
        String tempID = String.valueOf(id);
        for(int i = 0; i+1 < tempID.length(); i=i+2)
        {
            int c = Integer.parseInt(tempID.substring(i,i+2));

            sb.append(Character.toChars(c));
        }
        return sb.toString();
    }
}