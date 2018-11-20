/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.util;

import java.text.Collator;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import android.icu.impl.ICULocaleService;
import android.icu.impl.ICUService;
import android.icu.util.ULocale;

public class ICUServiceTestSample {
    static public void main(String[] args) {
        new HelloServiceClient();

        Thread t = new HelloUpdateThread();
        t.start();
        try {
            t.join();
        }
        catch (InterruptedException e) {
        }
        System.out.println("done");
    }

    /**
     * A class that displays the current names in the Hello service.
     * Each time the service changes, it redisplays the names.
     */
    static class HelloServiceClient implements HelloService.HelloServiceListener {

        HelloServiceClient() {
            HelloService.addListener(this);
            display();
        }


        /**
         * This will be called in the notification thread of
         * ICUNotifier.  ICUNotifier could spawn a (non-daemon) thread
         * for each listener, so that impolite listeners wouldn't hold
         * up notification, but right now it doesn't.  Instead, all
         * notifications are delivered on the notification thread.
         * Since that's a daemon thread, a notification might not
         * complete before main terminates.  
         */
        public void helloServiceChanged() {
            display();
        }

        private void display() {
            Map names = HelloService.getDisplayNames(ULocale.US);
            System.out.println("displaying " + names.size() + " names.");
            Iterator iter = names.entrySet().iterator();
            while (iter.hasNext()) {
                Entry entry = (Entry)iter.next();
                String displayName = (String)entry.getKey();
                HelloService service = HelloService.get((String)entry.getValue());
                System.out.println(displayName + " says " + service.hello());
                try {
                    Thread.sleep(50);
                }
                catch (InterruptedException e) {
                }
            }
            System.out.println("----");
        }
    }

    /**
     * A thread to update the service.
     */
    static class HelloUpdateThread extends Thread {
        String[][] updates = {
            { "Hey", "en_US_INFORMAL" },
            { "Hallo", "de_DE_INFORMAL" },
            { "Yo!", "en_US_CALIFORNIA_INFORMAL" },
            { "Chi Fanle Ma?", "zh__INFORMAL" },
            { "Munch munch... Burger?", "en" },
            { "Sniff", "fr" },
            { "TongZhi! MaoZeDong SiXiang Wan Sui!", "zh_CN" },
            { "Bier? Ja!", "de" },
        };
        public void run() {
            for (int i = 0; i < updates.length; ++i) {
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e) {
                }
                HelloService.register(updates[i][0], new ULocale(updates[i][1]));
            }
        }
    }

    /**
     * An example service that wraps an ICU service in order to export custom API and
     * notification. The service just implements 'hello'.
     */
    static final class HelloService {
        private static ICUService registry;
        private String name;
    
        private HelloService(String name) { 
            this.name = name; 
        }
    
        /**
         * The hello service...
         */
        public String hello() { 
            return name; 
        }
        
        public String toString() { 
            return super.toString() + ": " + name; 
        }
    
        /**
         * Deferred init.
         */
        private static ICUService registry() {
            if (registry == null) {
                initRegistry();
            }
            return registry;
        }
    
        private static void initRegistry() {
            registry = new ICULocaleService() {
                    protected boolean acceptsListener(EventListener l) {
                        return true; // we already verify in our wrapper APIs
                    }
                    protected void notifyListener(EventListener l) {
                        ((HelloServiceListener)l).helloServiceChanged();
                    }
                };
    
            // initialize
            doRegister("Hello", "en");
            doRegister("Bonjour", "fr");
            doRegister("Ni Hao", "zh_CN");
            doRegister("Guten Tag", "de");
        }
    
        /**
         * A custom listener for changes to this service.  We don't need to
         * point to the service since it is defined by this class and not
         * an object.
         */
        public static interface HelloServiceListener extends EventListener {
            public void helloServiceChanged();
        }
    
        /**
         * Type-safe notification for this service.
         */
        public static void addListener(HelloServiceListener l) {
            registry().addListener(l);
        }
    
        /**
         * Type-safe notification for this service.
         */
        public static void removeListener(HelloServiceListener l) {
            registry().removeListener(l);
        }
    
        /**
         * Type-safe access to the service.
         */
        public static HelloService get(String id) {
            return (HelloService)registry().get(id);
        }
    
        public static Set getVisibleIDs() {
            return registry().getVisibleIDs();
        }
    
        public static Map getDisplayNames(ULocale locale) {
            return getDisplayNames(registry(), locale);
        }
    
        /**
         * Register a new hello string for this locale.
         */
        public static void register(String helloString, ULocale locale) {
            if (helloString == null || locale == null) {
                throw new NullPointerException();
            }
            doRegister(helloString, locale.toString());
        }
    
        private static void doRegister(String hello, String id) {
            registry().registerObject(new HelloService(hello), id);
        }
        /**
         * Convenience override of getDisplayNames(ULocale, Comparator, String) that
         * uses the default collator for the locale as the comparator to
         * sort the display names, and null for the matchID.
         */
        public static SortedMap getDisplayNames(ICUService service, ULocale locale) {
            Collator col = Collator.getInstance(locale.toLocale());
            return service.getDisplayNames(locale, col, null);
        }
    }
}
