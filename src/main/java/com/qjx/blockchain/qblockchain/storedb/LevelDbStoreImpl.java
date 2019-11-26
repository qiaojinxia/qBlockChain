package com.qjx.blockchain.qblockchain.storedb;

/**
 * Created by caomaoboy 2019-11-16
 **/


import org.iq80.leveldb.DB;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;


/**
 * levelDB
 *
 * @author wuweifeng wrote on 2018/4/20.
 */
public class LevelDbStoreImpl implements DbStore {
    public LevelDbStoreImpl(DB db) {
        this.db = db;
    }
    private DB db;
    @Override
    public void put(String key, String value) {
        db.put(bytes(key), bytes(value));
    }

    @Override
    public String get(String key) {
        return asString(db.get(bytes(key)));
    }

    @Override
    public void remove(String key) {
        db.delete(bytes(key));
    }
}
