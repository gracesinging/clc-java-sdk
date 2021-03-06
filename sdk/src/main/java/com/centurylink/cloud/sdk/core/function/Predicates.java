/*
 * (c) 2015 CenturyLink. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.centurylink.cloud.sdk.core.function;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.centurylink.cloud.sdk.core.preconditions.Preconditions.checkNotNull;
import static com.centurylink.cloud.sdk.core.util.Strings.nullToEmpty;

/**
 * @author Ilya Drabenia
 */
public abstract class Predicates {

    public static <T> Predicate<T> notNull() {
        return (T item) -> item != null;
    }

    public static <T> Predicate<T> alwaysTrue() {
        return new ConstPredicate<>(true);
    }

    public static <T> boolean isAlwaysTruePredicate(Predicate<T> predicate) {
        return
            predicate != null &&
            predicate instanceof ConstPredicate &&
            ConstPredicate.cast(predicate).getDefaultValue();
    }

    public static <T> Predicate<T> alwaysFalse() {
        return new ConstPredicate<>(false);
    }

    public static <T> Predicate<T> in(Stream<T> values) {
        return in(values, Objects::equals);
    }

    public static <T> Predicate<T> in(Stream<T> values, BinaryPredicate<T> matcher) {
        checkNotNull(values, "Stream of values must be not a null");
        checkNotNull(matcher, "Matcher must be not a null");

        return
            values
                .filter(notNull())
                .map(curValue -> (Predicate<T>) t -> matcher.test(t, curValue))
                .reduce(Predicates.alwaysFalse(), Predicate::or);
    }

    @SafeVarargs
    public static <T> Predicate<T> in(T... values) {
        return in(Stream.of(values));
    }

    public static <T> Predicate<T> in(List<T> values) {
        checkNotNull(values, "List of values must be not a null");
        return in(values.stream());
    }

    public static <T> Predicate<T> in(List<T> values, BinaryPredicate<T> matcher) {
        checkNotNull(values, "List of values must be not a null");

        return in(values.stream(), matcher);
    }

    public static <T, R> Predicate<T> combine(Function<T, R> func, Predicate<R> predicate) {
        return r -> predicate.test(func.apply(r));
    }

    public static Predicate<String> containsIgnoreCase(String substring) {
        return item -> containsIgnoreCase(item, substring);
    }

    public static boolean containsIgnoreCase(String source, String substring) {
        return upperCase(source).contains(upperCase(substring));
    }

    public static boolean equalsIgnoreCase(String firstString, String secondString) {
        return upperCase(firstString).equals(upperCase(secondString));
    }

    private static String upperCase(String source) {
        return nullToEmpty(source).toUpperCase();
    }


}
