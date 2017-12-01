package by.citech.util;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import by.citech.param.Tags;

public class Reflection {

    public void logStaticFields(Class<?> clazz) throws IllegalAccessException {
        for (Field f : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                boolean wasAccessible = f.isAccessible();
                f.setAccessible(true);
                Log.i (Tags.DECODE, "Debug output of static field " + f.getName() + ": " + f.get( null ) );
                f.setAccessible( wasAccessible );
            }
        }
    }

    public void printStaticFields(Class<?> clazz) throws IllegalAccessException {
        for (Field f : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                boolean wasAccessible = f.isAccessible();
                f.setAccessible(true);
                Log.i (Tags.DECODE, "Debug output of static field " + f.getName() + ": " + f.get( null ) );
                f.setAccessible(wasAccessible);
            }
        }
    }

    public static <T> String getConstantNameMap(Class<?> clazz, T var) throws IllegalAccessException {
        Map<T, String> cNames = new HashMap<T, String>();
        Class<?> varClazz = var.getClass();
//      String result = null;

        for (Field field : clazz.getDeclaredFields()){
            boolean wasAccessible = field.isAccessible();
            field.setAccessible(true);
            Log.i (Tags.DECODE, "getConstantName Found field: " + field.getName());
            field.setAccessible(wasAccessible);
            if ((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0) {
                Log.i (Tags.DECODE, "getConstantName: modifiers are OK");
//              if (varClazz == field.getType()) {
                if (varClazz.isInstance(field.getType())) {
                    Log.i (Tags.DECODE, "getConstantName: type is match");
                    if (field == var) {
                        Log.i (Tags.DECODE, "getConstantName: got one match");
                        wasAccessible = field.isAccessible();
                        field.setAccessible(true);
                        cNames.put((T) field.get(null), field.getName());
//                      result = field.getName();
                        field.setAccessible(wasAccessible);
                        break;
                    } else {
                        Log.i (Tags.DECODE, "getConstantName: mismatch");
                    }
                }
            }
        }
        Log.i (Tags.DECODE, "Searching for fields done.");
        return cNames.get(var);
//      return result;
    }

    public static <T> String getConstantName(Class<?> clazz, T var, boolean debug) throws IllegalAccessException {
        Class<?> varClazz = var.getClass();
        String result = null;
        for (Field field : clazz.getDeclaredFields()){
            boolean wasAccessible = field.isAccessible();
            field.setAccessible(true);
            if (debug) System.out.println("Found field <" + field.getName() + ">.");
            field.setAccessible(wasAccessible);
            if ((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0) {
                if (debug) System.out.print("Modifiers are matching. ");
//              if (varClazz == field.getType()) {
//              if (varClazz.isInstance(field.getType())) {
                if (field.get(null).getClass() == varClazz) {
                    if (debug) System.out.print("Type is matching. ");
//                  if (field.get(null) == var) {
//                  if (field.equals(var)) {
                    if (field.get(null).equals(var)) {
                        if (debug) System.out.println("Value is matching. Got it!");
                        wasAccessible = field.isAccessible();
                        field.setAccessible(true);
//                      cNames.put((T) field.get(null), field.getName());
                        result = field.getName();
                        field.setAccessible(wasAccessible);
                        break;
                    } else if (debug) System.out.println("Value is not matching.");
                } else if (debug) System.out.println("Type is not matching.");
            } else if (debug) System.out.println("Modifiers is not matching.");
        }
        return result;
    }

}
