import Foundation
import third_party_java_src_j2objc_protobuf_tests_transitive_shim_j2objc_proto

// The moved types are two levels of `import public` away.
func transitiveShimWidget() -> ComGoogleJ2objctestMovedWidget {
  return ComGoogleJ2objctestMovedWidget.newBuilder().build()
}

func transitiveShimColor() -> ComGoogleJ2objctestMovedColor {
  return ComGoogleJ2objctestMovedColor_get_RED()
}
