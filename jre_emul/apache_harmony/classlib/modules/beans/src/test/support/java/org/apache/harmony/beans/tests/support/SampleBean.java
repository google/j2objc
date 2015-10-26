/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Maxim V. Berkultsev
 */
package org.apache.harmony.beans.tests.support;

import java.util.Iterator;
import java.util.TooManyListenersException;
import java.util.Vector;

/**
 * @author Maxim V. Berkultsev
 */

public class SampleBean {

    private String text = null;

    private String otherText = null;

    private SampleBean bean = null;

    private int x = 0;

    private double[] smth;

    private Object[] smthObjs;

    private Vector<SampleListener> listeners;

    public SampleBean() {
        this.text = null;
    }

    public SampleBean(String text) {
        this.text = text;
        this.otherText = "Constructor with args";
    }

    protected SampleBean(String text, SampleBean bean) {
        this.text = text;
        this.bean = bean;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public SampleBean getObject() {
        return bean;
    }

    public void setObject(SampleBean bean) {
        this.bean = bean;
    }

    public String getOtherText() {
        return otherText;
    }

    public void setOtherText(String value) {
        this.otherText = value;
    }

    public int getX() {
        return x;
    }

    public void setX(int value) {
        this.x = value;
    }

    public double getSmthByIdx(int i) {
        return smth[i];
    }

    public void setSmthByIdx(int i, double value) {
        smth[i] = value;
    }

    public double[] getSmth() {
        return this.smth;
    }

    public void setSmth(double[] value) {
        this.smth = value;
    }

    public Object getObjectByIdx(int i) {
        return smthObjs[i];
    }

    public void setObjectByIdx(int i, Object value) {
        this.smthObjs[i] = value;
    }

    public Object[] getObjects() {
        return smthObjs;
    }

    public void setObjects(Object[] value) {
        this.smthObjs = value;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SampleBean) {
            SampleBean sb = (SampleBean) other;
            if ((sb.bean == null) && (bean == null)) {
                return true;
            } else if ((sb.bean != null) && (bean != null)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public static SampleBean create(String text, SampleBean bean) {
        return new SampleBean(text, bean);
    }

    public void addSampleListener(SampleListener listener)
            throws TooManyListenersException {
        if (listeners == null) {
            listeners = new Vector<SampleListener>();
        }

        if (listeners.size() >= 100) {
            throw new TooManyListenersException(
                    "Number of listeners could not exceed 100");
        }
        listeners.add(listener);
    }

    public void removeSampleListener(SampleListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public SampleListener[] getSampleListeners() {
        if (listeners != null) {
            SampleListener[] result = new SampleListener[listeners.size()];

            Iterator<SampleListener> i = listeners.iterator();

            int idx = 0;
            while (i.hasNext()) {
                result[idx++] = i.next();
            }

            return result;
        }
        return new SampleListener[] {};
    }
}
