/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *   Copyright (C) 2005-2010, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 */

package android.icu.util;

import java.io.Serializable;

final class STZInfo implements Serializable {
    private static final long serialVersionUID = -7849612037842370168L;

    void setStart(int sm, int sdwm, int sdw, int st, int sdm, boolean sa) {
        this.sm = sm;
        this.sdwm = sdwm;
        this.sdw = sdw;
        this.st = st;
        this.sdm = sdm;
        this.sa = sa;
    }

    void setEnd(int em, int edwm, int edw, int et, int edm, boolean ea) {
        this.em = em;
        this.edwm = edwm;
        this.edw = edw;
        this.et = et;
        this.edm = edm;
        this.ea = ea;
    }

    /*void applyTo(java.util.SimpleTimeZone stz) {
        if (sy != -1) {
            stz.setStartYear(sy);
        }
        if (sm != -1) {
            if (sdm == -1) {
                stz.setStartRule(sm, sdwm, sdw, st);
            } else if (sdw == -1) {
                stz.setStartRule(sm, sdm, st);
            } else {
                stz.setStartRule(sm, sdm, sdw, st, sa);
            }
        }
        if (em != -1) {
            if (edm == -1) {
                stz.setEndRule(em, edwm, edw, et);
            } else if (edw == -1) {
                stz.setEndRule(em, edm, et);
            } else {
                stz.setEndRule(em, edm, edw, et, ea);
            }
        }
    }*/
    
    void applyTo(android.icu.util.SimpleTimeZone stz) {
        if (sy != -1) {
            stz.setStartYear(sy);
        }
        if (sm != -1) {
            if (sdm == -1) {
                stz.setStartRule(sm, sdwm, sdw, st);
            } else if (sdw == -1) {
                stz.setStartRule(sm, sdm, st);
            } else {
                stz.setStartRule(sm, sdm, sdw, st, sa);
            }
        }
        if (em != -1) {
            if (edm == -1) {
                stz.setEndRule(em, edwm, edw, et);
            } else if (edw == -1) {
                stz.setEndRule(em, edm, et);
            } else {
                stz.setEndRule(em, edm, edw, et, ea);
            }
        }
    }
    
    int sy = -1;
    int sm = -1, sdwm, sdw, st, sdm;
    boolean sa;
    int em = -1, edwm, edw, et, edm;
    boolean ea;
}

