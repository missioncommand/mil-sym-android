package armyc2.c5isr.renderer.symbolpicker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import armyc2.c5isr.renderer.MilStdIconRenderer;
import armyc2.c5isr.renderer.utilities.ImageInfo;
import armyc2.c5isr.renderer.utilities.MilStdAttributes;
import armyc2.c5isr.renderer.utilities.SymbolID;

/**
 * Example client application that uses the symbol picker to get a symbol code
 */
public class SymbolPickerTester extends Activity {
    private final int SymbolPickerRequestCode = 0;
    private MilStdIconRenderer mir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sp_tester);
        Intent i = new Intent(this, SymbolPickerActivity.class);

        i.putExtra(SymbolPickerActivity.supportedVersionsKey, new int[]{SymbolID.Version_2525Dch1, SymbolID.Version_2525E});

        // Button to initialize symbol picker
        Button newCodeBtn = findViewById(R.id.new_code_btn);
        newCodeBtn.setOnClickListener(view -> startActivityForResult(i, SymbolPickerRequestCode));

        mir = MilStdIconRenderer.getInstance();
        mir.init(this);

        // Start symbol picker (could remove and wait for button press)
        startActivityForResult(i, SymbolPickerRequestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SymbolPickerRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                String selectedSymbolID = data.getStringExtra(SymbolPickerActivity.selectedSymbolIdKey);
                if (selectedSymbolID.equals("")) {
                    return;
                }
                // Client application can use selectedSymbolID as necessary


                // Example use of selected symbol code
                TextView resultCodeTv = findViewById(R.id.selected_code_text);
                resultCodeTv.setText(getString(R.string.selected_code, selectedSymbolID));
                HashMap<String, String> modifiersFromIntent = (HashMap<String, String>)data.getSerializableExtra(SymbolPickerActivity.modifiersKey);
                Map<String,String> modifiers = new HashMap<>();
                for (String i : modifiersFromIntent.keySet()) {
                    modifiers.put(i, modifiersFromIntent.get(i));
                    // Log.d("Modifiers SparseArray", String.format("Added index %s (%s) = %s", i, Modifiers.getModifierName(i), modifiersFromIntent.get(i)));
                }
                HashMap<String, String> attributesFromIntent = (HashMap<String, String>)data.getSerializableExtra(SymbolPickerActivity.attributesKey);
                Map<String,String> attributes = new HashMap<>();
                for (String i : attributesFromIntent.keySet()) {
                    attributes.put(i, attributesFromIntent.get(i));
                    // Log.d("Attributes SparseArray", String.format("Added index %s (%s) = %s", i, MilStdAttributes.getAttributeName(i), attributesFromIntent.get(i)));
                }
                attributes.put(MilStdAttributes.PixelSize, "240");
                ImageInfo ii = mir.RenderIcon(selectedSymbolID, modifiers, attributes);
                ImageView selectedSymbolView = findViewById(R.id.tester_selected_symbol);
                if (ii != null && ii.getImage() != null)
                    selectedSymbolView.setImageBitmap(ii.getImage());
                selectedSymbolView.setBackgroundColor(Color.LTGRAY);
            }
        }
    }
}
