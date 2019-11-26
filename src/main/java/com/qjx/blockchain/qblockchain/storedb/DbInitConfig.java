package storedb;


import org.iq80.leveldb.DB;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

/**
 * 配置启用哪个db，部分Windows机器用不了rocksDB，可以选择levelDB
 * Created by caomaoboy 2019-11-16
 */
@Configuration
public class DbInitConfig {
    private final static  String dbfile ="./blockchaindb" ;
    public DB levelDB() throws IOException {
        org.iq80.leveldb.Options options = new org.iq80.leveldb.Options();
        options.createIfMissing(true);
        return Iq80DBFactory.factory.open(new File(dbfile), options);
    }

    public static void main(String[] args) throws IOException {

        DbInitConfig a= new DbInitConfig();
        DbStore b = new LevelDbStoreImpl(a.levelDB());
        b.put("123","2344");
        System.out.println(b.get("123"));
    }
}
