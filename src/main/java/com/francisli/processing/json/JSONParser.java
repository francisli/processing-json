package com.francisli.processing.json;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public abstract class JSONParser<T> {

    T newInstance() {
        ParameterizedType superClass = (ParameterizedType) getClass().getGenericSuperclass();
        Type type = superClass.getActualTypeArguments()[0];
        Class<T> classType;
        if (type instanceof Class) {
            classType = (Class<T>) type;
        } else {
            classType = (Class<T>) ((ParameterizedType) type).getRawType();
        }
        try {
            return classType.newInstance();
        } catch (Exception e) {
            // Oops, no default constructor
            throw new RuntimeException(e);
        }
    }

    Class itemType() {
        ParameterizedType superClass = (ParameterizedType) getClass().getGenericSuperclass();
        Type type = superClass.getActualTypeArguments()[0];
        return (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    public T parse(InputStream is) {
        try {
            org.noggit.JSONParser parser = new org.noggit.JSONParser(new InputStreamReader(is));
            T result = null;
            int event = parser.nextEvent();
            if (event != org.noggit.JSONParser.EOF) {
                keys = new ArrayDeque<String>();
                result = newInstance();
                switch (event) {
                case org.noggit.JSONParser.ARRAY_START:
                    parseArray(parser, (List) result, itemType());
                    break;
                case org.noggit.JSONParser.OBJECT_START:
                    parseObject(parser, result);
                    break;
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ArrayDeque<String> keys;

    Field getField(Object object, String key) throws Exception {
        try {
            return object.getClass().getField(key);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    void parseObject(org.noggit.JSONParser parser, Object object) throws Exception {
        int event = parser.nextEvent();
        while (event != org.noggit.JSONParser.EOF) {
            Field field;
            switch (event) {
            case org.noggit.JSONParser.STRING:
                String stringValue = parser.getString();
                if (keys.isEmpty()) {
                    keys.push(stringValue);
                } else {
                    field = getField(object, keys.pop());
                    if (field != null) {
                        field.set(object, stringValue);
                    }
                }
                break;
            case org.noggit.JSONParser.LONG:
                Long longValue = parser.getLong();
                field = getField(object, keys.pop());
                if (field != null) {
                    field.set(object, longValue);
                }
                break;
            case org.noggit.JSONParser.NUMBER:
                Double doubleValue = parser.getDouble();
                field = getField(object, keys.pop());
                if (field != null) {
                    field.set(object, doubleValue);
                }
                break;
            case org.noggit.JSONParser.BIGNUMBER:
                String bigValue = parser.getNumberChars().toString();
                field = getField(object, keys.pop());
                if (field != null) {
                    field.set(object, bigValue);
                }
                break;
            case org.noggit.JSONParser.BOOLEAN:
                boolean boolValue = parser.getBoolean();
                field = getField(object, keys.pop());
                if (field != null) {
                    field.set(object, boolValue);
                }
                break;
            case org.noggit.JSONParser.NULL:
                parser.getNull();
                keys.pop();
                break;
            case org.noggit.JSONParser.ARRAY_START:
                field = getField(object, keys.pop());
                if (field != null) {
                    ArrayList array = new ArrayList();
                    parseArray(parser, array, (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
                    field.set(object, array);
                } else {
                    parseArray(parser, new ArrayList(), Object.class);
                }
                break;
            case org.noggit.JSONParser.OBJECT_START:
                field = getField(object, keys.pop());
                if (field != null) {
                    Object nestedObject = field.getDeclaringClass().newInstance();
                    parseObject(parser, nestedObject);
                    field.set(object, nestedObject);
                } else {
                    parseObject(parser, new Object());
                }
                break;
            case org.noggit.JSONParser.OBJECT_END:
                return;
            }
            event = parser.nextEvent();
        }
    }

    void parseArray(org.noggit.JSONParser parser, List array, Class type) throws Exception {
        int event = parser.nextEvent();
        while (event != org.noggit.JSONParser.EOF) {
            switch (event) {
            case org.noggit.JSONParser.STRING:
                String stringValue = parser.getString();
                array.add(stringValue);
                break;
            case org.noggit.JSONParser.LONG:
                Long longValue = parser.getLong();
                array.add(longValue);
                break;
            case org.noggit.JSONParser.NUMBER:
                Double doubleValue = parser.getDouble();
                array.add(doubleValue);
                break;
            case org.noggit.JSONParser.BIGNUMBER:
                String bigValue = parser.getNumberChars().toString();
                array.add(bigValue);
                break;
            case org.noggit.JSONParser.BOOLEAN:
                boolean boolValue = parser.getBoolean();
                array.add(boolValue);
                break;
            case org.noggit.JSONParser.NULL:
                parser.getNull();
                array.add(null);
                break;
            case org.noggit.JSONParser.OBJECT_START:
                Object obj = type.newInstance();
                parseObject(parser, obj);
                array.add(obj);
                break;
            case org.noggit.JSONParser.ARRAY_START:
                parseArray(parser, new ArrayList(), Object.class);
                break;
            case org.noggit.JSONParser.ARRAY_END:
                return;
            }
            event = parser.nextEvent();
        }
    }
}
