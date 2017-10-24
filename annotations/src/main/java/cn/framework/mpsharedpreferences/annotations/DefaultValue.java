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

package cn.framework.mpsharedpreferences.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to set the default value for this preference. THis value is supplied as String, because
 * the code is generated and the value simply inserted as is. Please be sure to support a valid
 * value.
 * <p>By setting {@link #createDefaultGetter} to <b>false</b> you
 * can suppress the additional getter of the form <em>get(Type defaultValue)</em>.</p>
 *
 * @author sevenshal
 * @version 1.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface DefaultValue {
    /**
     * Supplies the default value to use for a preference in String format. e.g. "3.0f", "hi", ...
     * @return the value to use as String.
     */
    String value();

    /**
     * Used to specify the creation of the default getter of the form <em>get(Type defaultValue)</em>.
     * @return default true, to create the default getter nonetheless.
     */
    boolean createDefaultGetter() default true;
}
