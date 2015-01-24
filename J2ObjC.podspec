Pod::Spec.new do |s|
  s.name         = 'J2ObjC'
  s.version      = '0.9.5'
  s.license      = { type: 'Apache License, Version 2.0', file: 'LICENSE' }
  s.summary      = 'J2ObjC\'s JRE emulation library, emulates a subset of the Java runtime library.'
  s.homepage     = 'https://github.com/google/j2objc'
  s.author       = 'Google Inc.'
  s.source       = {
    http: "https://github.com/google/j2objc/releases/download/#{s.version}/j2objc-#{s.version}.zip",
    sha1: '6b53c2b47c9cd4c6678d0fee75b00039b83fc120',
  }

  s.ios.deployment_target = '5.0'
  s.osx.deployment_target = '10.7'
  s.requires_arc = false

  # Top level attributes can't be specified by subspecs.
  s.header_mappings_dir = 'dist/include'

  s.subspec 'lib' do |lib|
    lib.subspec 'jre_emul' do |jre_emul|
      jre_emul.public_header_files = %w(
        dist/include/
        dist/include/java/**/*
        dist/include/libcore/**/*
      )
      jre_emul.vendored_libraries = "dist/#{lib.base_name}/lib#{jre_emul.base_name}.a"
      jre_emul.libraries = jre_emul.base_name, 'icucore', 'z'
    end
  end
end
