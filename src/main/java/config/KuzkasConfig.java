package config;

import com.moandjiezana.toml.Toml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

public class KuzkasConfig {

    private static final Logger logger = LoggerFactory.getLogger(KuzkasConfig.class);

    private static final KuzkasConfig instance = new KuzkasConfig();
    private static final String DEFAULT_CONFIG_FILE_PATH = "./config.toml";

    private int port;
    private boolean persistence;
    private String data_persistence_file_path;
    private long data_persistence_interval;


    private KuzkasConfig() {
        this(DEFAULT_CONFIG_FILE_PATH);
    }

    private KuzkasConfig(String config_path) {

        try {
            Toml toml = new Toml();
            File file = new File(config_path);
            if (!file.exists()){
                logger.warn("Kuzkas config file not found, using default config");
                this.port = 7508;
                this.persistence = true;
                this.data_persistence_file_path = "./dump.kuzkas";
                this.data_persistence_interval = 10_000L;
                return;
            }
            Toml config = toml.read(file);
            Toml kuzkas = config.getTable("kuzkas");
            this.port = kuzkas.getLong("port", 7508L).intValue();
            this.persistence = kuzkas.getBoolean("persistence", true);
            this.data_persistence_file_path = kuzkas.getString("data_persistence_file_path", "./dump.kuzkas");
            this.data_persistence_interval = kuzkas.getLong("data_persistence_interval", 10_000L);
            logger.info("Kuzkas config ("+ file.getAbsolutePath() + ") loaded successfully.");
        } catch (Exception e) {
            logger.error("Error reading config file: ", e);
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

    public boolean isPersistence() {
        return persistence;
    }

    public void setPersistence(boolean persistence) {
        this.persistence = persistence;
    }

    public String getData_persistence_file_path() {
        return data_persistence_file_path;
    }

    public void setData_persistence_file_path(String data_persistence_file_path) {
        this.data_persistence_file_path = data_persistence_file_path;
    }

    public long getData_persistence_interval() {
        return data_persistence_interval;
    }

    public void setData_persistence_interval(long data_persistence_interval) {
        this.data_persistence_interval = data_persistence_interval;
    }
}