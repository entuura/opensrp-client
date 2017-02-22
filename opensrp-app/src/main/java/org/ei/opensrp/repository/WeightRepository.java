package org.ei.opensrp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.ei.opensrp.Context;
import org.ei.opensrp.domain.UniqueId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeightRepository extends DrishtiRepository {
    private static final String TAG = WeightRepository.class.getCanonicalName();
    private static final String UniqueIds_SQL = "CREATE TABLE unique_ids(_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,openmrs_id VARCHAR NOT NULL,status VARCHAR NULL, used_by VARCHAR NULL,synced_by VARCHAR NULL,created_at DATETIME NULL,updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP )";
    public static final String UniqueIds_TABLE_NAME = "unique_ids";
    public static final String ID_COLUMN = "_id";
    public static final String OPENMRS_ID_COLUMN = "openmrs_id";
    public static final String STATUS_COLUMN = "status";
    private static final String USED_BY_COLUMN = "used_by";
    private static final String SYNCED_BY_COLUMN = "synced_by";
    public static final String CREATED_AT_COLUMN = "created_at";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String[] UniqueIds_TABLE_COLUMNS = {ID_COLUMN, OPENMRS_ID_COLUMN, STATUS_COLUMN, USED_BY_COLUMN, SYNCED_BY_COLUMN, CREATED_AT_COLUMN, UPDATED_AT_COLUMN};

    public static String STATUS_USED = "used";
    public static String STATUS_NOT_USED = "not_used";
    private static WeightRepository instance;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(SQLiteDatabase database) {
        database.execSQL(UniqueIds_SQL);
    }

    public void add(UniqueId uniqueId) {
        SQLiteDatabase database = masterRepository.getWritableDatabase();
        database.insert(UniqueIds_TABLE_NAME, null, createValuesFor(uniqueId));
        database.close();
    }

    public static WeightRepository getInstance() {
        if (instance == null) {
            instance = new WeightRepository();
        }
        return instance;
    }

    /**
     * inserts ids in bulk to the db in a transaction since normally, each time db.insert() is used, SQLite creates a transaction (and resulting journal file in the filesystem), which slows things down.
     *
     * @param ids
     */
    public void bulkInserOpenmrsIds(List<String> ids) {
        SQLiteDatabase database = masterRepository.getWritableDatabase();

        try {
            String userName = Context.getInstance().allSharedPreferences().fetchRegisteredANM();

            database.beginTransaction();
            for (String id : ids) {
                ContentValues values = new ContentValues();
                values.put(OPENMRS_ID_COLUMN, id);
                values.put(STATUS_COLUMN, STATUS_NOT_USED);
                values.put(SYNCED_BY_COLUMN, userName);
                values.put(CREATED_AT_COLUMN, dateFormat.format(new Date()));
                database.insert(UniqueIds_TABLE_NAME, null, values);
            }
            database.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            database.endTransaction();
        }
    }

    public Long countUnUsedIds() {
        long count = 0;
        try {
            SQLiteDatabase database = masterRepository.getWritableDatabase();

            Cursor cursor = database.rawQuery("SELECT COUNT (*) FROM " + UniqueIds_TABLE_NAME + " WHERE " + STATUS_COLUMN + "=?",
                    new String[]{String.valueOf(STATUS_NOT_USED)});
            if (null != cursor)
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    count = cursor.getInt(0);
                }
            cursor.close();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        return count;
    }

    /**
     * get next available unique id
     *
     * @return
     */
    public UniqueId getNextUniqueId() {
        SQLiteDatabase database = masterRepository.getReadableDatabase();
        Cursor cursor = database.query(UniqueIds_TABLE_NAME, UniqueIds_TABLE_COLUMNS, STATUS_COLUMN + " = ?", new String[]{STATUS_NOT_USED}, null, null, CREATED_AT_COLUMN + " ASC", "1");
        List<UniqueId> ids = readAll(cursor);
        return ids.isEmpty()?null:ids.get(0);
    }

    /**
     * mark and openmrsid as used
     *
     * @param openmrsId
     */
    public void close(String openmrsId) {
        String userName = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        if(!openmrsId.contains("-")){
            openmrsId = formatId(openmrsId);
        }
        ContentValues values = new ContentValues();
        values.put(STATUS_COLUMN, STATUS_USED);
        values.put(USED_BY_COLUMN, userName);
        masterRepository.getWritableDatabase().update(UniqueIds_TABLE_NAME, values, OPENMRS_ID_COLUMN + " = ?", new String[]{openmrsId});
    }
    private String formatId(String openmrsId) {
        int lastIndex=openmrsId.length()-1;
        String tail = openmrsId.substring(lastIndex);
        return openmrsId.substring(0, lastIndex) + "-"+tail;
    }
    private ContentValues createValuesFor(UniqueId uniqueId) {
        ContentValues values = new ContentValues();
        values.put(ID_COLUMN, uniqueId.getId());
        values.put(OPENMRS_ID_COLUMN, uniqueId.getOpenmrsId());
        values.put(STATUS_COLUMN, uniqueId.getStatus());
        values.put(USED_BY_COLUMN, uniqueId.getUsedBy());
        values.put(CREATED_AT_COLUMN, dateFormat.format(uniqueId.getCreatedAt()));
        return values;
    }

    private List<UniqueId> readAll(Cursor cursor) {
        List<UniqueId> UniqueIds = new ArrayList<UniqueId>();

        try {
            cursor.moveToFirst();
            while (cursor.getCount() > 0 && !cursor.isAfterLast()) {

                UniqueIds.add(new UniqueId(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), new Date(cursor.getLong(4))));

                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return UniqueIds;
    }


}
