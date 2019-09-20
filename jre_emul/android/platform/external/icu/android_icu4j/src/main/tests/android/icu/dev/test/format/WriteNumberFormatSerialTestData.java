/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2001-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
 
package android.icu.dev.test.format;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import android.icu.text.NumberFormat;

/**
 * @version     1.0
 * @author Ram Viswanadha
 */
public class WriteNumberFormatSerialTestData {
    static final String header="/*\n" +
                               " *******************************************************************************\n"+
                               " * Copyright (C) 2001, International Business Machines Corporation and         *\n"+
                               " * others. All Rights Reserved.                                                *\n"+
                               " *******************************************************************************\n"+
                               " */\n\n"+
                               "package android.icu.dev.test.format;\n\n"+
                                
                               "public class NumberFormatSerialTestData {\n"+
                               "    //get Content\n"+
                               "    public static byte[][] getContent() {\n"+
                               "            return content;\n"+
                               "    }\n";
                              
    static final String footer ="\n    final static byte[][] content = {generalInstance, currencyInstance, percentInstance, scientificInstance};\n"+
                                "}\n";                           
    public static void main(String[] args){
        NumberFormat nf     = NumberFormat.getInstance(Locale.US);
        NumberFormat nfc    = NumberFormat.getCurrencyInstance(Locale.US);
        NumberFormat nfp     = NumberFormat.getPercentInstance(Locale.US);
        NumberFormat nfsp     = NumberFormat.getScientificInstance(Locale.US);
        
        try{
            FileOutputStream file = new FileOutputStream("NumberFormatSerialTestData.java");
            file.write(header.getBytes());
            write(file,(Object)nf,"generalInstance", "//NumberFormat.getInstance(Locale.US)");
            write(file,(Object)nfc,"currencyInstance","//NumberFormat.getCurrencyInstance(Locale.US)");
            write(file,(Object)nfp,"percentInstance","//NumberFormat.getPercentInstance(Locale.US)");
            write(file,(Object)nfsp,"scientificInstance","//NumberFormat.getScientificInstance(Locale.US)");
            file.write(footer.getBytes());            
            file.close();
        }catch( Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    private static void write(FileOutputStream file,Object o ,String name,String comment){
        try{
            ByteArrayOutputStream bts =  new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bts);    
            os.writeObject((Object)o);
            os.flush();
            os.close();
            byte[] myArr = bts.toByteArray();
            //String temp = new String(myArr);
            System.out.println("    "+comment+ " :");
            /*System.out.println("minimumIntegerDigits : "  + (temp.indexOf("minimumIntegerDigits")+"minimumIntegerDigits".length()));
            System.out.println("maximumIntegerDigits : "  + (temp.indexOf("maximumIntegerDigits")+"maximumIntegerDigits".length()));
            System.out.println("minimumFractionDigits : " + (temp.indexOf("minimumFractionDigits")+"minimumFractionDigits".length()));
            System.out.println("maximumFractionDigits : " + (temp.indexOf("maximumFractionDigits")+"maximumFractionDigits".length()));
            */
            //file.write(myArr);
            file.write(("\n    "+comment).getBytes());
            file.write(new String("\n    static byte[] "+name+" = new byte[]{ \n").getBytes("UTF-8"));
            file.write( "        ".getBytes());
            for(int i=0; i<myArr.length; i++){
                file.write(String.valueOf((int)myArr[i]).getBytes());
                file.write( ", ".getBytes());
                if((i+1)%20 == 0){
                    file.write("\n".getBytes());
                    file.write( "        ".getBytes());
                }
            }
            file.write(new String("\n    };\n").getBytes("UTF-8"));
        }catch( Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    
    }
}
