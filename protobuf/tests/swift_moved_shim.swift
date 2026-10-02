import Foundation
import third_party_java_src_j2objc_protobuf_tests_moved_shim_j2objc_proto

// The shim declares nothing; every type here comes from the file it
// `import public`s.
func movedShimWidget() -> ComGoogleJ2objctestMovedWidget {
  return ComGoogleJ2objctestMovedWidget.newBuilder().build()
}

func movedShimColor() -> ComGoogleJ2objctestMovedColor {
  return ComGoogleJ2objctestMovedColor_get_RED()
}

func movedShimColorOrdinal() -> ComGoogleJ2objctestMovedColor_Enum {
  return .red
}
