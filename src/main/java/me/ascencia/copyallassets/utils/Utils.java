package me.ascencia.copyallassets.utils;

public class Utils {
    public static boolean checkFieldExist(Class<?> aClass, String field) {
        try {
            return aClass.getDeclaredField(field) != null;
        } catch (NoSuchFieldException e) {}

        return false;
    }
}
