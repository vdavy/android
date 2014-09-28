package com.stationmillenium.android.utils.preferences;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;
import android.widget.ListView;

import com.stationmillenium.android.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Class to emulate {@link MultiSelectListPreference} on Android 2.3
 * Little adapatation for this app
 * 
 * @author declanshanaghy
 * http://blog.350nice.com/wp/archives/240
 * MultiChoice Preference Widget for Android
 *
 * @contributor matiboy
 * Added support for check all/none and custom separator defined in XML.
 * IMPORTANT: The following attributes MUST be defined (probably inside attr.xml) for the code to even compile
 * <declare-styleable name="ListPreferenceMultiSelect">
    	<attr format="string" name="checkAll" />
    	<attr format="string" name="separator" />
    </declare-styleable>
 *  Whether you decide to then use those attributes is up to you.
 *
 */
public class ListPreferenceMultiSelect extends ListPreference {

	private static final String DEFAULT_SEPARATOR = "OV=I=XseparatorX=I=VO"; 

	private String separator;
	private String checkAllKey = null;
	private boolean[] mClickedDialogEntryIndices;

	// Constructor
	public ListPreferenceMultiSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListPreferenceMultiSelect);
		checkAllKey = a.getString( R.styleable.ListPreferenceMultiSelect_checkAll );
		String s = a.getString(R.styleable.ListPreferenceMultiSelect_separator );
		if( s != null ) {
			separator = s;
		} else {
			separator = DEFAULT_SEPARATOR;
		}
		// Initialize the array of boolean to the same size as number of entries
		CharSequence[] entries = getEntries();
		mClickedDialogEntryIndices = new boolean[(entries != null) ? entries.length : 0];
		a.recycle();
	}

	@Override
	public void setEntries(CharSequence[] entries) {
		super.setEntries(entries);
		// Initialize the array of boolean to the same size as number of entries
		mClickedDialogEntryIndices = new boolean[(entries != null) ? entries.length : 0];
	}

	public ListPreferenceMultiSelect(Context context) {
		this(context, null);
	}

	@Override
    protected void onPrepareDialogBuilder(@NotNull Builder builder) {
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        if (entries == null || entryValues == null || entries.length != entryValues.length ) {
			throw new IllegalStateException(
					"ListPreference requires an entries array and an entryValues array which are both the same length");
		}

		restoreCheckedEntries();
		builder.setMultiChoiceItems(entries, mClickedDialogEntryIndices, 
				new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean val) {
				if( isCheckAllValue( which ) == true ) {
					checkAll( dialog, val );
				}
				mClickedDialogEntryIndices[which] = val;
			}
		});
	}

	private boolean isCheckAllValue( int which ){
		final CharSequence[] entryValues = getEntryValues();
		if(checkAllKey != null) {
			return entryValues[which].equals(checkAllKey);
		}
		return false;
	}

	private void checkAll( DialogInterface dialog, boolean val ) {
		ListView lv = ((AlertDialog) dialog).getListView();
		int size = lv.getCount();
		for(int i = 0; i < size; i++) {
			lv.setItemChecked(i, val);
			mClickedDialogEntryIndices[i] = val;
		}
	}

	public String[] parseStoredValue(CharSequence val) {
		if ( val == null || "".equals(val) ) {
			return null;
		}
		else {
			return ((String)val).split(separator);
		}
	}

	private void restoreCheckedEntries() {
		CharSequence[] entryValues = getEntryValues();

		// Explode the string read in sharedpreferences
		String[] vals = parseStoredValue(getValue());

		if ( vals != null ) {
			List<String> valuesList = Arrays.asList(vals);
			for ( int i=0; i<entryValues.length; i++ ) {
				CharSequence entry = entryValues[i];
				if ( valuesList.contains(entry) ) {
					mClickedDialogEntryIndices[i] = true;
				}
			}
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
        ArrayList<String> values = new ArrayList<>();

		CharSequence[] entryValues = getEntryValues();
		if (positiveResult && entryValues != null) {
			for ( int i=0; i<entryValues.length; i++ ) {
				if ( mClickedDialogEntryIndices[i] == true ) {
					// Don't save the state of check all option - if any
					String val = (String) entryValues[i];
					if( checkAllKey == null || (val.equals(checkAllKey) == false) ) {
						values.add(val);
					}
				}
			}

			String newValue = join(values, separator);
			if (callChangeListener(newValue)) {
				setValue(newValue);
			}
		}
	}

	// Credits to kurellajunior on this post http://snippets.dzone.com/posts/show/91
	protected static String join( Iterable< ? extends Object > pColl, String separator )
	{
		Iterator< ? extends Object > oIter;
		if ( pColl == null || ( !( oIter = pColl.iterator() ).hasNext() ) )
			return "";
		StringBuilder oBuilder = new StringBuilder( String.valueOf( oIter.next() ) );
		while ( oIter.hasNext() )
			oBuilder.append( separator ).append( oIter.next() );
		return oBuilder.toString();
	}

}
