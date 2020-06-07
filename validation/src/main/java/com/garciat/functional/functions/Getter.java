package com.garciat.functional.functions;

import lombok.Value;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// https://stackoverflow.com/a/21879031
@FunctionalInterface
public interface Getter<T, R> extends NullableFunction<T, R>, Serializable {

  default Info getInfo() {
    for (Class<?> cl = getClass(); cl != null; cl = cl.getSuperclass()) {
      try {
        Method m = cl.getDeclaredMethod("writeReplace");
        m.setAccessible(true);
        Object replacement = m.invoke(this);
        if (!(replacement instanceof SerializedLambda)) {
          break; // custom interface implementation
        }
        SerializedLambda l = (SerializedLambda) replacement;
        return new Info(l.getImplClass(), l.getImplMethodName());
      } catch (NoSuchMethodException e) {
      } catch (IllegalAccessException | InvocationTargetException e) {
        break;
      }
    }
    throw new RuntimeException("not a method reference");
  }

  @Value
  class Info {
    String implClass;
    String implMethodName;

    public String toReferenceName() {
      return implClass + "::" + implMethodName;
    }

    public String getPropertyName() {
      if (implMethodName.startsWith("get")) {
        return implMethodName.substring("get".length());
      } else {
        return implMethodName;
      }
    }
  }
}
