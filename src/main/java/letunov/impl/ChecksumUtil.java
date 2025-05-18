package letunov.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;

@Slf4j
public class ChecksumUtil {
    private static final MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getContractChecksum(Class<?> contract) {
        long checksumNumb = 0;
        for (var method : contract.getMethods()) {
            checksumNumb += getContractMethodChecksum(method);
        }
        var hash = digest.digest(String.valueOf(checksumNumb).getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    // ===================================================================================================================
    // = Implementation
    // ===================================================================================================================

    private static long getContractMethodChecksum(Method method) {
        long methodChecksum = 0;
        methodChecksum += getReturnTypeChecksum(method); //-1545402933
        methodChecksum += getMappingAnnotationsChecksum(method); //-3612296218 -- -1748668323
        methodChecksum += getMethodParamsChecksum(method); //-6607847031
        methodChecksum += getReturnTypeChecksum(method); //-8153249964
        methodChecksum += method.getModifiers(); //-8153248939
        methodChecksum += method.getName().hashCode(); //-8225028700
        return methodChecksum;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static long getMethodParamsChecksum(Method method) {
        long paramsChecksum = 0;
        for (var parameter : method.getParameters()) {
            paramsChecksum += getContractParamChecksum(parameter);
        }
        return paramsChecksum;
    }

    private static long getContractParamChecksum(Parameter parameter) {
        long checksum = 0;
        for (var annotation : parameter.getAnnotations()) {
            if (annotation instanceof RequestBody requestBody) {
                checksum += String.valueOf(requestBody.required()).hashCode();
                checksum += "RequestBody".hashCode();
            }
            if (annotation instanceof PathVariable pathVariable) {
                checksum += pathVariable.value().hashCode();
                checksum += String.valueOf(pathVariable.required()).hashCode();
                checksum += pathVariable.name().hashCode();
                checksum += "PathVariable".hashCode();
            }
            if (annotation instanceof RequestParam requestParam) {
                checksum += requestParam.value().hashCode();
                checksum += requestParam.name().hashCode();
                checksum += String.valueOf(requestParam.required()).hashCode();
                checksum += requestParam.defaultValue().hashCode();
                checksum += "RequestParam".hashCode();
            }
        }
        checksum += getParamChecksum(parameter);
        return checksum;
    }

    private static long getMappingAnnotationsChecksum(Method method) {
        long checksum = 0;
        for (var annotation : method.getAnnotations()) {
            if (annotation instanceof RequestMapping requestMapping) {
                checksum += Arrays.hashCode(requestMapping.consumes()); //1
                checksum += Arrays.hashCode(stream(requestMapping.method()).map(Enum::name).toArray()); //2461888
                checksum += requestMapping.name().hashCode(); //222250277
                checksum += Arrays.hashCode(requestMapping.headers()); //222250278
                checksum += Arrays.hashCode(requestMapping.value()); //222250279
                checksum += Arrays.hashCode(requestMapping.path()); //-87556128
                checksum += Arrays.hashCode(requestMapping.params()); //-87556127
                checksum += Arrays.hashCode(requestMapping.produces()); //-87556126
                checksum += "RequestMapping".hashCode(); //-2108138687
            }
            if (annotation instanceof GetMapping getMapping) {
                checksum = getChecksumNumb(checksum, getMapping.value(), getMapping.path(),
                    getMapping.params(),
                    getMapping.headers(), getMapping.consumes(), getMapping.produces()); //130556225
                checksum += "GetMapping".hashCode();
            }
            if (annotation instanceof PostMapping postMapping) {
                checksum =
                    getChecksumNumb(checksum, postMapping.value(), postMapping.path(),
                        postMapping.params(), postMapping.headers(), postMapping.consumes(), postMapping.produces());
                checksum += "PostMapping".hashCode();
            }
            if (annotation instanceof DeleteMapping deleteMapping) {
                checksum =
                    getChecksumNumb(checksum, deleteMapping.value(), deleteMapping.path(),
                        deleteMapping.params(), deleteMapping.headers(), deleteMapping.consumes(), deleteMapping.produces());
                checksum += "DeleteMapping".hashCode();
            }
            if (annotation instanceof PutMapping putMapping) {
                checksum = getChecksumNumb(checksum, putMapping.value(), putMapping.path(),
                    putMapping.params(), putMapping.headers(), putMapping.consumes(), putMapping.produces());
                checksum += "PutMapping".hashCode();
            }
            if (annotation instanceof PatchMapping patchMapping) {
                checksum =
                    getChecksumNumb(checksum, patchMapping.value(), patchMapping.path(),
                        patchMapping.params(), patchMapping.headers(), patchMapping.consumes(), patchMapping.produces());
                checksum += "PatchMapping".hashCode();
            }
        }
        return checksum;
    }

    private static long getChecksumNumb(long checksumNumb, String[] value, String[] path, String[] params, String[] headers,
        String[] consumes, String[] produces) {
        checksumNumb += Arrays.hashCode(value);
        checksumNumb += Arrays.hashCode(path);
        checksumNumb += Arrays.hashCode(params);
        checksumNumb += Arrays.hashCode(headers);
        checksumNumb += Arrays.hashCode(consumes);
        checksumNumb += Arrays.hashCode(produces);
        return checksumNumb;
    }

    private static long getReturnTypeChecksum(Method method) {
        long checksum = 0;
        var returnType = method.getReturnType();
        if (method.getGenericReturnType() instanceof ParameterizedType parameterizedType) {
            checksum += getParameterizedTypeChecksum(parameterizedType);
        }
        checksum += getTypeChecksum(returnType);
        return checksum;
    }

    private static long getFieldChecksum(Field field) {
        long checksum = 0;
        if (field.getGenericType() instanceof ParameterizedType parameterizedType) {
            checksum += getParameterizedTypeChecksum(parameterizedType);
        }
        checksum += getTypeChecksum(field.getType());
        checksum += field.getName().hashCode();
        return checksum;
    }

    private static long getParamChecksum(Parameter parameter) {
        long checksum = 0;
        if (parameter.getParameterizedType() instanceof ParameterizedType parameterizedType) {
            checksum += getParameterizedTypeChecksum(parameterizedType);
        }
        checksum += getTypeChecksum(parameter.getType());
        checksum += parameter.getName().hashCode();
        return checksum;
    }

    private static long getTypeChecksum(Class<?> type) {
        long checksum = 0;
        checksum += type.getTypeName().hashCode();
        var getters = getGetters(type);
        if (!type.equals(String.class) && !type.equals(Void.class) && !ClassUtils.isPrimitiveOrWrapper(type)) {
            for (var field : type.getDeclaredFields()) {
                if (getters.containsKey(field.getName())) {
                    checksum += getFieldChecksum(field);
                }
            }
        }
        return checksum;
    }

    private static long getParameterizedTypeChecksum(ParameterizedType parameterizedType) {
        long checksum = 0;
        for (var type : parameterizedType.getActualTypeArguments()) {
            if (type instanceof ParameterizedType nestedparameterizedType) {
                checksum += getParameterizedTypeChecksum(nestedparameterizedType);
            } else {
                checksum += getTypeChecksum((Class<?>) type);
            }
        }
        return checksum;
    }

    private static Map<String, Method> getGetters(Class<?> type) {
        return stream(type.getMethods())
            .filter(method -> method.getName().matches("^get[A-Z]\\w*$"))
            .collect(Collectors.toMap(method -> method.getName().substring(3), identity(), (k, v) -> v));
    }
}
