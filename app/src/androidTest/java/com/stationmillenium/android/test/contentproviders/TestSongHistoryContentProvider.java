/**
 *
 */
package com.stationmillenium.android.test.contentproviders;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.test.ProviderTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.stationmillenium.android.R;
import com.stationmillenium.android.providers.SongHistoryContentProvider;
import com.stationmillenium.android.providers.SongHistoryContract;

/**
 * Test the {@link SongHistoryContentProvider}
 *
 * @author vincent
 */
public class TestSongHistoryContentProvider extends ProviderTestCase2<SongHistoryContentProvider> {

    private static final String TAG = "TestSongHistoryCP";
    private static final String FULL_TEXT_QUERY = "agenda c�te";
    private static final String SUGGEST_QUERY = "top concept";
    private static final int MAX_SUGGEST_COLUMNS_COUNT = 4;
    private static final int MAX_SUGGEST_SIZE = 5;

    /**
     * Create a new {@link TestSongHistoryContentProvider}
     */
    public TestSongHistoryContentProvider() {
        super(SongHistoryContentProvider.class, SongHistoryContract.AUTHORITY);
    }

    /**
     * Test the default search
     */
    @SmallTest
    public void testDefaultSearch() {
        Cursor cursor = getMockContentResolver().query(SongHistoryContract.CONTENT_URI, null, null, null, null);
        Log.d(TAG, "Cursor : " + cursor);
        assertCursor(cursor);
    }

    /**
     * Test the full text search
     */
    @MediumTest
    public void testFullTextSearch() {
        Uri uri = SongHistoryContract.CONTENT_URI.buildUpon().appendPath(FULL_TEXT_QUERY).build();
        Cursor cursor = getMockContentResolver().query(uri, null, null, null, null);
        Log.d(TAG, "Cursor : " + cursor);
        assertCursor(cursor);
    }

    /**
     * Test the date search
     */
    @MediumTest
    public void testDateSearch() {
        Uri uri = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(SongHistoryContract.AUTHORITY)
                .appendPath(SongHistoryContract.DATE_SEARCH_SEGMENT)
                .appendPath("20131208-0000")
                .build();
        Cursor cursor = getMockContentResolver().query(uri, null, null, null, null);
        Log.d(TAG, "Cursor : " + cursor);
        assertCursor(cursor);
    }

    /**
     * Test the suggest search
     */
    @MediumTest
    public void testSuggestSearch() {
        Uri uri = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(SongHistoryContract.AUTHORITY)
                .appendPath(SearchManager.SUGGEST_URI_PATH_QUERY)
                .appendPath(SUGGEST_QUERY)
                .build();
        Cursor cursor = getMockContentResolver().query(uri, null, null, null, null);
        Log.d(TAG, "Cursor : " + cursor);

        //assert
        assertCursor(cursor);
        assertEquals(cursor.getColumnCount(), MAX_SUGGEST_COLUMNS_COUNT);
        assertEquals(cursor.getColumnName(0), BaseColumns._ID);
        assertEquals(cursor.getColumnName(1), SearchManager.SUGGEST_COLUMN_TEXT_1);
        assertEquals(cursor.getColumnName(2), SearchManager.SUGGEST_COLUMN_TEXT_2);
        assertEquals(cursor.getColumnName(3), SearchManager.SUGGEST_COLUMN_QUERY);
        assertTrue(cursor.getCount() <= MAX_SUGGEST_SIZE);
    }

    /**
     * Assert the cursor
     *
     * @param cursor the {@link Cursor}
     */
    private void assertCursor(Cursor cursor) {
        assertNotNull(cursor);
        assertTrue(cursor.getCount() > 0);
        assertTrue(cursor.getCount() <= Integer.parseInt(getMockContext().getString(R.string.song_history_max_items)));
    }
}
