package com.braunster.chatsdk.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.braunster.chatsdk.dao.BInstallation;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table BINSTALLATION.
*/
public class BInstallationDao extends AbstractDao<BInstallation, Long> {

    public static final String TABLENAME = "BINSTALLATION";

    /**
     * Properties of entity BInstallation.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property ApiKey = new Property(1, String.class, "apiKey", false, "api_key");
        public final static Property RootKey = new Property(2, String.class, "rootKey", false, "root_key");
    };

    private DaoSession daoSession;


    public BInstallationDao(DaoConfig config) {
        super(config);
    }
    
    public BInstallationDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'BINSTALLATION' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'api_key' TEXT," + // 1: apiKey
                "'root_key' TEXT);"); // 2: rootKey
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'BINSTALLATION'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, BInstallation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String apiKey = entity.getApiKey();
        if (apiKey != null) {
            stmt.bindString(2, apiKey);
        }
 
        String rootKey = entity.getRootKey();
        if (rootKey != null) {
            stmt.bindString(3, rootKey);
        }
    }

    @Override
    protected void attachEntity(BInstallation entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public BInstallation readEntity(Cursor cursor, int offset) {
        BInstallation entity = new BInstallation( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // apiKey
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2) // rootKey
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, BInstallation entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setApiKey(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setRootKey(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(BInstallation entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(BInstallation entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
