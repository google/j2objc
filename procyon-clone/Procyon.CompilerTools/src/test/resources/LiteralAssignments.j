.version 51 0
.source LiteralAssignments.java
.class super LiteralAssignments
.super java/lang/Object

.field static b B
.field static c C
.field static s S
.field static i I
.field static l J
.field static f F
.field static d D

.method private <init> : ()V
	.limit stack 1
	.limit locals 1
	aload_0
	invokespecial java/lang/Object <init> ()V
	return
.end method

.method static public testByteAssignments : ()V
	.limit stack 2
	.limit locals 1

	bipush -128
	putstatic LiteralAssignments b B
	bipush 127
	putstatic LiteralAssignments b B

    sipush -129
    i2c
    putstatic LiteralAssignments b B
    sipush -128
    i2c
    putstatic LiteralAssignments b B
    sipush 127
    i2c
    putstatic LiteralAssignments b B
    sipush 128
    i2c
    putstatic LiteralAssignments b B

    sipush -129
	putstatic LiteralAssignments b B
	sipush -128
	putstatic LiteralAssignments b B
	sipush 127
	putstatic LiteralAssignments b B
	sipush 128
    putstatic LiteralAssignments b B

    ldc -129
    putstatic LiteralAssignments b B
    ldc -128
    putstatic LiteralAssignments b B
    ldc 127
    putstatic LiteralAssignments b B
    ldc 128
    putstatic LiteralAssignments b B

    ldc2_w -129L
    l2i
    putstatic LiteralAssignments b B
    ldc2_w -128L
    l2i
    putstatic LiteralAssignments b B
    ldc2_w 127L
    l2i
    putstatic LiteralAssignments b B
    ldc2_w 128L
    l2i
    putstatic LiteralAssignments b B

    ldc -129.0F
    f2i
    putstatic LiteralAssignments b B
    ldc -128.0F
    f2i
    putstatic LiteralAssignments b B
    ldc 127.0F
    f2i
    putstatic LiteralAssignments b B
    ldc 128.0F
    f2i
    putstatic LiteralAssignments b B

    ldc2_w -129.0
    d2i
    putstatic LiteralAssignments b B
    ldc2_w -128.0
    d2i
    putstatic LiteralAssignments b B
    ldc2_w 127.0
    d2i
    putstatic LiteralAssignments b B
    ldc2_w 128.0
    d2i
    putstatic LiteralAssignments b B

    return
.end method

.method static public testCharAssignments : ()V
    .limit stack 2
    .limit locals 1

	bipush -128
	putstatic LiteralAssignments c C
	bipush 127
	putstatic LiteralAssignments c C

    sipush -1
    i2c
    putstatic LiteralAssignments c C
    sipush 0
    i2c
    putstatic LiteralAssignments c C
    sipush 32767
    i2c
    putstatic LiteralAssignments c C

    sipush -32768
	putstatic LiteralAssignments c C
	sipush 32767
	putstatic LiteralAssignments c C

    ldc -1
    putstatic LiteralAssignments c C
    ldc 0
    putstatic LiteralAssignments c C
    ldc 32767
    putstatic LiteralAssignments c C
    ldc 32768
    putstatic LiteralAssignments c C

    ldc2_w -1L
    l2i
    putstatic LiteralAssignments c C
    ldc2_w 0L
    l2i
    putstatic LiteralAssignments c C
    ldc2_w 32767L
    l2i
    putstatic LiteralAssignments c C
    ldc2_w 32768L
    l2i
    putstatic LiteralAssignments c C

    ldc -1.0F
    f2i
    putstatic LiteralAssignments c C
    ldc 0.0F
    f2i
    putstatic LiteralAssignments c C
    ldc 32767.0F
    f2i
    putstatic LiteralAssignments c C
    ldc 32768.0F
    f2i
    putstatic LiteralAssignments c C

    ldc2_w -1.0
    d2i
    putstatic LiteralAssignments c C
    ldc2_w 0.0
    d2i
    putstatic LiteralAssignments c C
    ldc2_w 32767.0
    d2i
    putstatic LiteralAssignments c C
    ldc2_w 32768.0
    d2i
    putstatic LiteralAssignments c C

    return
.end method

.method static public testShortAssignments : ()V
    .limit stack 2
    .limit locals 1

	bipush -128
	putstatic LiteralAssignments s S
	bipush 127
	putstatic LiteralAssignments s S

    sipush -1
    i2c
    putstatic LiteralAssignments s S
    sipush 0
    i2c
    putstatic LiteralAssignments s S
    sipush 32767
    i2c
    putstatic LiteralAssignments s S

    sipush -32768
	putstatic LiteralAssignments s S
	sipush 32767
	putstatic LiteralAssignments s S

    ldc -32769
    putstatic LiteralAssignments s S
    ldc -32768
    putstatic LiteralAssignments s S
    ldc 32767
    putstatic LiteralAssignments s S
    ldc 32768
    putstatic LiteralAssignments s S

    ldc2_w -32769L
    l2i
    putstatic LiteralAssignments s S
    ldc2_w 0L
    l2i
    putstatic LiteralAssignments s S
    ldc2_w 32767L
    l2i
    putstatic LiteralAssignments s S
    ldc2_w 32768L
    l2i
    putstatic LiteralAssignments s S

    ldc -32769.0F
    f2i
    putstatic LiteralAssignments s S
    ldc -32768.0F
    f2i
    putstatic LiteralAssignments s S
    ldc 32767.0F
    f2i
    putstatic LiteralAssignments s S
    ldc 32768.0F
    f2i
    putstatic LiteralAssignments s S

    ldc2_w -32769.0
    d2i
    putstatic LiteralAssignments s S
    ldc2_w -32768.0
    d2i
    putstatic LiteralAssignments s S
    ldc2_w 32767.0
    d2i
    putstatic LiteralAssignments s S
    ldc2_w 32768.0
    d2i
    putstatic LiteralAssignments s S

    return
.end method

.method static public testIntAssignments : ()V
    .limit stack 2
    .limit locals 1

	bipush -128
	putstatic LiteralAssignments i I
	bipush 127
	putstatic LiteralAssignments i I

    sipush -1
    i2c
    putstatic LiteralAssignments i I
    sipush 0
    i2c
    putstatic LiteralAssignments i I
    sipush 32767
    i2c
    putstatic LiteralAssignments i I

    sipush -32768
	putstatic LiteralAssignments i I
	sipush 32767
	putstatic LiteralAssignments i I

    ldc -2147483648
    putstatic LiteralAssignments i I
    ldc 2147483647
    putstatic LiteralAssignments i I

    ldc2_w -2147483649L
    l2i
    putstatic LiteralAssignments i I
    ldc2_w 0L
    l2i
    putstatic LiteralAssignments i I
    ldc2_w 2147483647L
    l2i
    putstatic LiteralAssignments i I
    ldc2_w 2147483648L
    l2i
    putstatic LiteralAssignments i I

    ldc -2147483649.0F
    f2i
    putstatic LiteralAssignments i I
    ldc -2147483648.0F
    f2i
    putstatic LiteralAssignments i I
    ldc 2147483647.0F
    f2i
    putstatic LiteralAssignments i I
    ldc 2147483648.0F
    f2i
    putstatic LiteralAssignments i I

    ldc2_w -2147483649.0
    d2i
    putstatic LiteralAssignments i I
    ldc2_w -2147483648.0
    d2i
    putstatic LiteralAssignments i I
    ldc2_w 2147483647.0
    d2i
    putstatic LiteralAssignments i I
    ldc2_w 2147483648.0
    d2i
    putstatic LiteralAssignments i I

    return
.end method

.method static public testFloatAssignments : ()V
    .limit stack 2
    .limit locals 1

	bipush -128
	putstatic LiteralAssignments f F
	bipush 127
	putstatic LiteralAssignments f F

    sipush -1
    i2c
    putstatic LiteralAssignments f F
    sipush 0
    i2c
    putstatic LiteralAssignments f F
    sipush 32767
    i2c
    putstatic LiteralAssignments f F

    sipush -32768
	putstatic LiteralAssignments f F
	sipush 32767
	putstatic LiteralAssignments f F

    ldc -2147483648
    putstatic LiteralAssignments f F
    ldc 2147483647
    putstatic LiteralAssignments f F

    ldc2_w -2147483649L
    l2i
    putstatic LiteralAssignments f F
    ldc2_w 0L
    l2i
    putstatic LiteralAssignments f F
    ldc2_w 2147483647L
    l2i
    putstatic LiteralAssignments f F
    ldc2_w 2147483648L
    l2i
    putstatic LiteralAssignments f F

    ldc -2147483649.0F
    f2i
    putstatic LiteralAssignments f F
    ldc -2147483648.0F
    f2i
    putstatic LiteralAssignments f F
    ldc 2147483647.0F
    f2i
    putstatic LiteralAssignments f F
    ldc 2147483648.0F
    f2i
    putstatic LiteralAssignments f F

    ldc2_w -2147483649.0
    d2i
    putstatic LiteralAssignments f F
    ldc2_w -2147483648.0
    d2i
    putstatic LiteralAssignments f F
    ldc2_w 2147483647.0
    d2i
    putstatic LiteralAssignments f F
    ldc2_w 2147483648.0
    d2i
    putstatic LiteralAssignments f F

    return
.end method

.method static public testLongAssignments : ()V
    .limit stack 2
    .limit locals 1

	bipush -128
	i2l
	putstatic LiteralAssignments l J
	bipush 127
	i2l
	putstatic LiteralAssignments l J

    sipush -1
    i2c
	i2l
    putstatic LiteralAssignments l J
    sipush 0
    i2c
	i2l
    putstatic LiteralAssignments l J
    sipush 32767
    i2c
	i2l
    putstatic LiteralAssignments l J

    sipush -32768
	i2l
	putstatic LiteralAssignments l J
	sipush 32767
	i2l
	putstatic LiteralAssignments l J

    ldc -2147483648
	i2l
    putstatic LiteralAssignments l J
    ldc 2147483647
	i2l
    putstatic LiteralAssignments l J

    ldc2_w -2147483649L
    putstatic LiteralAssignments l J
    ldc2_w 0L
    putstatic LiteralAssignments l J
    ldc2_w 2147483647L
    putstatic LiteralAssignments l J
    ldc2_w 2147483648L
    putstatic LiteralAssignments l J

    ldc -2147483649.0F
    f2l
    putstatic LiteralAssignments l J
    ldc -2147483648.0F
    f2l
    putstatic LiteralAssignments l J
    ldc 2147483647.0F
    f2l
    putstatic LiteralAssignments l J
    ldc 2147483648.0F
    f2l
    putstatic LiteralAssignments l J

    ldc2_w -2147483649.0
    d2l
    putstatic LiteralAssignments l J
    ldc2_w -2147483648.0
    d2l
    putstatic LiteralAssignments l J
    ldc2_w 2147483647.0
    d2l
    putstatic LiteralAssignments l J
    ldc2_w 2147483648.0
    d2l
    putstatic LiteralAssignments l J

    return
.end method

.method static public testDoubleAssignments : ()V
    .limit stack 2
    .limit locals 1

	bipush -128
	i2d
	putstatic LiteralAssignments d D
	bipush 127
	i2d
	putstatic LiteralAssignments d D

    sipush -1
    i2c
	i2d
    putstatic LiteralAssignments d D
    sipush 0
    i2c
	i2d
    putstatic LiteralAssignments d D
    sipush 32767
    i2c
	i2d
    putstatic LiteralAssignments d D

    sipush -32768
	i2d
	putstatic LiteralAssignments d D
	sipush 32767
	i2d
	putstatic LiteralAssignments d D

    ldc -2147483648
	i2d
    putstatic LiteralAssignments d D
    ldc 2147483647
	i2d
    putstatic LiteralAssignments d D

    ldc2_w -2147483649L
    l2d
    putstatic LiteralAssignments d D
    ldc2_w 0L
    l2d
    putstatic LiteralAssignments d D
    ldc2_w 2147483647L
    l2d
    putstatic LiteralAssignments d D
    ldc2_w 2147483648L
    l2d
    putstatic LiteralAssignments d D

    ldc -2147483649.0F
    f2d
    putstatic LiteralAssignments d D
    ldc -2147483648.0F
    f2d
    putstatic LiteralAssignments d D
    ldc 2147483647.0F
    f2d
    putstatic LiteralAssignments d D
    ldc 2147483648.0F
    f2d
    putstatic LiteralAssignments d D

    ldc2_w -2147483649.0
    putstatic LiteralAssignments d D
    ldc2_w -2147483648.0
    putstatic LiteralAssignments d D
    ldc2_w 2147483647.0
    putstatic LiteralAssignments d D
    ldc2_w 2147483648.0
    putstatic LiteralAssignments d D

    return
.end method
