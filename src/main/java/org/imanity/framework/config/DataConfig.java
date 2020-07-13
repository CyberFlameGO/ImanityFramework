package org.imanity.framework.config;

import org.imanity.framework.Imanity;
import org.imanity.framework.config.converters.DatabaseTypeConverter;
import org.imanity.framework.config.util.Converter;
import org.imanity.framework.config.util.annotation.Convert;
import org.imanity.framework.config.util.yaml.BukkitYamlConfiguration;
import org.imanity.framework.config.util.format.FieldNameFormatters;
import org.imanity.framework.database.DatabaseType;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class DataConfig extends BukkitYamlConfiguration {

    @Convert(DatabaseTypeConverter.class)
    private DatabaseType DEFAULT_DATABASE = DatabaseType.FLAT_FILE;

    @Convert(SpecificDatabasesConverter.class)
    private Map<String, DatabaseType> SPECIFIC_DATABASES = new HashMap<>();

    public boolean ASYNCHRONOUS_DATA_STORING = true;

    public DataConfig() {
        super(new File(Imanity.PLUGIN.getDataFolder(), "data.yml").toPath(), BukkitYamlProperties
            .builder()
                .setFormatter(FieldNameFormatters.LOWER_CASE)
            .setPrependedComments(Arrays.asList(
                    "==============================",
                    "The configuration to adjust data settings",
                    "==============================",
                    " "
            )).build());
    }

    public DatabaseType getDatabaseType(String name) {
        if (SPECIFIC_DATABASES.containsKey(name)) {
            return SPECIFIC_DATABASES.get(name);
        }
        return DEFAULT_DATABASE;
    }

    public boolean isDatabaseTypeUsed(DatabaseType databaseType) {
        if (DEFAULT_DATABASE == databaseType) {
            return true;
        }
        return SPECIFIC_DATABASES.containsValue(databaseType);
    }

    private static class SpecificDatabasesConverter implements Converter<Map<String, DatabaseType>, List<String>> {

        @Override
        public List<String> convertTo(Map<String, DatabaseType> element, ConversionInfo info) {
            return element.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue().name())
                    .collect(Collectors.toList());
        }

        @Override
        public Map<String, DatabaseType> convertFrom(List<String> element, ConversionInfo info) {
            return element.stream()
                    .map(s -> s.split(":"))
                    .collect(Collectors.toMap(s -> s[0], s -> DatabaseType.valueOf(s[1].toUpperCase())));
        }
    }
}