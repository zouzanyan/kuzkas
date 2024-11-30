package config;

import com.moandjiezana.toml.Toml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class KuzkasConfig {

    private static final Logger logger = LoggerFactory.getLogger(KuzkasConfig.class);

    private static final KuzkasConfig instance = new KuzkasConfig();
    private static final String DEFAULT_CONFIG_FILE_PATH = "./config.toml";

    private int port;
    private boolean persistent_file_read;
    private boolean persistent_file_write;
    private String persistent_file_path;
    private long persistent_interval;


    private KuzkasConfig() {
        readConfig();
    }

    private void readConfig() {
        try {
            Toml toml = new Toml();
            File file = new File(KuzkasConfig.DEFAULT_CONFIG_FILE_PATH);
            if (!file.exists()){
                logger.warn("Kuzkas config file not found, using default config");
                this.port = 7508;
                this.persistent_file_read = true;
                this.persistent_file_write = true;
                this.persistent_file_path = "./dump.kuzkas";
                this.persistent_interval = 5_000L;
                return;
            }
            Toml config = toml.read(file);
            Toml kuzkas = config.getTable("kuzkas");
            this.port = kuzkas.getLong("port", 7508L).intValue();
            this.persistent_file_read = kuzkas.getBoolean("persistence_write", true);
            this.persistent_file_write = kuzkas.getBoolean("persistence_read", true);
            this.persistent_file_path = kuzkas.getString("data_persistence_file_path", "./dump.kuzkas");
            this.persistent_interval = kuzkas.getLong("data_persistence_interval", 5_000L);
            logger.info("Kuzkas config ("+ file.getAbsolutePath() + ") loaded successfully.");
        } catch (Exception e) {
            logger.error("Configuration file format error: ", e);
        }
    }
    public static KuzkasConfig getInstance() {
        return instance;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isPersistent_file_read() {
        return persistent_file_read;
    }

    public void setPersistent_file_read(boolean persistent_file_read) {
        this.persistent_file_read = persistent_file_read;
    }

    public boolean isPersistent_file_write() {
        return persistent_file_write;
    }

    public void setPersistent_file_write(boolean persistent_file_write) {
        this.persistent_file_write = persistent_file_write;
    }

    public String getPersistent_file_path() {
        return persistent_file_path;
    }

    public void setPersistent_file_path(String persistent_file_path) {
        this.persistent_file_path = persistent_file_path;
    }

    public long getPersistent_interval() {
        return persistent_interval;
    }

    public void setPersistent_interval(long persistent_interval) {
        this.persistent_interval = persistent_interval;
    }
}

