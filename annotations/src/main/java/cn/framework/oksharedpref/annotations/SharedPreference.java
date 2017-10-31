/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 David Medenjak
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cn.framework.oksharedpref.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation to generate a default wrapper class for the annotated interface.
 * Supply a String value
 * to change the name of the genereated class to <i>value</i>Prefs and <i>value</i>Editor.
 * </p>
 * <p>By not specifying a value <i>DefaultPrefs</i> and <i>DefaultEditor</i> will be generated.</p>
 * <p>
 * <p>Additionally you may change the class name suffixes by setting {@link #preferencesSuffix()}
 * or {@link #editorSuffix()}.</p>
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SharedPreference {
    /**
     * the prefix for the generated preferences and editor.
     *
     * @return the prefix, the name of the annotated interface by default.
     */
    String value() default "";

    /**
     * the suffix for the preferences.
     *
     * @return the suffix, "Prefs" by default.
     */
    String preferencesSuffix() default "Prefs";

    /**
     * preferences name which used by getSharedPreferences(preferenceName).
     * @return the name, "default_preferences" by default.
     */
    String preferenceName() default "default_preferences";

    /**
     * Set the default type for all the contained properties in the interface if not specified.
     * To use a different type for a single property
     * annotate the properties with {@link Type}.
     * @return the default type, PreferenceType.STRING by default.
     */
    PreferenceType defaultPreferenceType() default PreferenceType.STRING;

    /**
     * the suffix for the editor.
     *
     * @return the suffix, "Editor" by default.
     */
    String editorSuffix() default "Editor";

    /**
     * implement interface of SharedPreference if true
     *
     * @return true
     */
    boolean implSharedPreference() default true;
}
