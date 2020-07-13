package org.imanity.framework.config.util;

import org.imanity.framework.config.util.Converter.ConversionInfo;
import org.imanity.framework.config.util.annotation.Format;
import org.imanity.framework.config.util.filter.FieldFilter;
import org.imanity.framework.config.util.format.FieldNameFormatter;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

enum FieldMapper {
    ;

    static Map<String, Object> instanceToMap(Object inst, MappingInfo mappingInfo) {
        Map<String, Object> map = new LinkedHashMap<>();
        Configuration.Properties props = mappingInfo.getProperties();
        FieldFilter filter = props.getFilter();
        for (Field field : filter.filterDeclaredFieldsOf(inst.getClass())) {
            Object val = toConvertibleObject(field, inst, mappingInfo);
            FieldNameFormatter fnf = selectFormatter(mappingInfo);
            String fn = fnf.fromFieldName(field.getName());
            map.put(fn, val);
        }
        return map;
    }

    private static Object toConvertibleObject(
            Field field, Object instance, MappingInfo mappingInfo
    ) {
        checkDefaultValueNull(field, instance);
        ConversionInfo info = ConversionInfo.from(field, instance, mappingInfo);
        Validator.checkFieldWithElementTypeIsContainer(info);
        Object converted = Converters.convertTo(info);
        Validator.checkConverterNotReturnsNull(converted, info);
        return converted;
    }

    static void instanceFromMap(
            Object inst, Map<String, Object> instMap, MappingInfo mappingInfo
    ) {
        FieldFilter filter = mappingInfo.getProperties().getFilter();
        for (Field field : filter.filterDeclaredFieldsOf(inst.getClass())) {
            FieldNameFormatter fnf = selectFormatter(mappingInfo);
            String fn = fnf.fromFieldName(field.getName());
            Object mapValue = instMap.get(fn);
            if (mapValue != null) {
                fromConvertedObject(field, inst, mapValue, mappingInfo);
            }
        }
    }

    private static void fromConvertedObject(
            Field field, Object instance, Object mapValue,
            MappingInfo mappingInfo
    ) {
        checkDefaultValueNull(field, instance);
        ConversionInfo info = ConversionInfo.from(
                field, instance, mapValue, mappingInfo
        );
        Validator.checkFieldWithElementTypeIsContainer(info);
        Object convert = Converters.convertFrom(info);

        if (convert == null) {
            return;
        }

        if (Reflect.isContainerType(info.getFieldType())) {
            Validator.checkFieldTypeAssignableFrom(convert.getClass(), info);
        }

        Reflect.setValue(field, instance, convert);
    }

    private static void checkDefaultValueNull(Field field, Object instance) {
        Object val = Reflect.getValue(field, instance);
        Validator.checkNotNull(val, field.getName());
    }

    static FieldNameFormatter selectFormatter(MappingInfo info) {
        Configuration<?> configuration = info.getConfiguration();
        Configuration.Properties props = info.getProperties();
        if ((configuration != null) &&
                Reflect.hasFormatter(configuration.getClass())) {
            Format format = configuration.getClass()
                    .getAnnotation(Format.class);
            return (format.formatterClass() != FieldNameFormatter.class)
                    ? Reflect.newInstance(format.formatterClass())
                    : format.value();
        }
        return props.getFormatter();
    }

    static final class MappingInfo {
        private final Configuration<?> configuration;
        private final Configuration.Properties properties;

        MappingInfo(
                Configuration<?> configuration,
                Configuration.Properties properties
        ) {
            this.configuration = configuration;
            this.properties = properties;
        }

        Configuration<?> getConfiguration() {
            return configuration;
        }

        Configuration.Properties getProperties() {
            return properties;
        }

        static MappingInfo from(Configuration<?> configuration) {
            return new MappingInfo(configuration, configuration.getProperties());
        }
    }
}