package org.platformlayer.ops;

import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.platformlayer.xaas.model.ServiceAuthorization;
import org.platformlayer.xaas.model.Setting;
import org.platformlayer.xaas.model.SettingCollection;
import org.platformlayer.xml.JaxbHelper;

import com.google.common.base.Strings;

public class OpsConfig {
    final Properties properties;

    public static OpsConfig build(ServiceAuthorization serviceAuthorization) throws OpsException {
        Properties properties = new Properties();

        try {
            JaxbHelper jaxbHelper = JaxbHelper.get(SettingCollection.class);
            if (!Strings.isNullOrEmpty(serviceAuthorization.data)) {
                SettingCollection settings = (SettingCollection) jaxbHelper.unmarshal(serviceAuthorization.data);
                if (settings.items != null) {
                    for (Setting setting : settings.items) {
                        properties.put(setting.key, setting.value);
                    }
                }
            }
        } catch (JAXBException e) {
            throw new OpsException("Error reading configuration", e);
        }

        OpsConfig opsConfig = new OpsConfig(properties);
        return opsConfig;
    }

    public OpsConfig(Properties properties) {
        super();
        this.properties = properties;
    }

    public String getRequiredString(String key) throws OpsConfigException {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new OpsConfigException("Required configuration value not found: " + key, key);
        }
        return value;
    }

    public String getString(String key, String defaultValue) throws OpsConfigException {
        return properties.getProperty(key, defaultValue);
    }

}
