This directory shows how the Protocol Buffers examples can be built for
macOS using J2ObjC.

You must install the protobuf package before you can build these
(https://github.com/google/protobuf#protocol-compiler-installation).

To build all the examples in a J2ObjC distribution bundle, run "make".
This creates the following scripts:

  add_person     list_people

Both of these scripts take an address book file as their parameter.
The add_person script will create the file if it doesn't already exist.
For example:

    $ ./add_person /tmp/addressbook.bin
    $ ./list_people /tmp/addressbook.bin

These examples were adapted from the Protocol Buffers tutorial, located at:
  https://developers.google.com/protocol-buffers/docs/tutorials
