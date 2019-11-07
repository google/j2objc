.version 49 0
.source JsrWithoutRet.java
.class super public JsrWithoutRet
.super java/lang/Object

.method public <init> : ()V
    .limit stack 1
    .limit locals 1

    aload_0
    invokespecial java/lang/Object <init> ()V
    return
.end method

.method static public main : ([Ljava/lang/String;)V
    .limit stack 2
    .limit locals 1
    goto first
    second:
        pop
        getstatic java/lang/System out Ljava/io/PrintStream;
        ldc 'Hello world'
        invokevirtual java/io/PrintStream println (Ljava/lang/String;)V
        goto third
    first:
        jsr_w second
    third:
        return
.end method
