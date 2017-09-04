rm -rf j2objc-argc-2.0.2
mkdir j2objc-argc-2.0.2
cp -R dist/* j2objc-argc-2.0.2
find j2objc-argc-2.0.2 -name "*.a" | xargs strip -S -x
