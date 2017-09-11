package common.helpers;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.rmi.runtime.Log;

public class ConfigManager {

    private Configuration configuration;
    private Logger logger;

    private ConfigManager(String configFile) {
        initConfig(configFile);
        logger = LogManager.getLogger(ConfigManager.class.getSimpleName() + ":" + configFile);
    }

    public <T> T get(Class<T> type, String key) {
        T value = configuration.get(type, key);
        logger.info("Config key get: <" + type.getSimpleName() + "," + key + ">");
        return value;
    }

    public String get(String key) {
        String value = this.get(String.class, key);
        return value;
    }

    private synchronized void initConfig(String configFile) {
        try {
            logger.info("Config loading: " + configFile);
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                            .configure(params.properties()
                                    .setFileName(configFile));
            Configuration config = builder.getConfiguration();
            this.configuration = config;

        } catch (ConfigurationException e) {
            logger.error(e);
        }
    }
}
