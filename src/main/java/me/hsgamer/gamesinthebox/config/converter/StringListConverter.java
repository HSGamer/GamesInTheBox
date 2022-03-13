package me.hsgamer.gamesinthebox.config.converter;

import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.hscore.config.annotation.converter.Converter;

public class StringListConverter implements Converter {
    @Override
    public Object convert(Object raw) {
        return CollectionUtils.createStringListFromObject(raw, false);
    }

    @Override
    public Object convertToRaw(Object value) {
        return value;
    }
}
