import Foundation
import third_party_java_src_j2objc_protobuf_tests_moved_j2objc_proto
import third_party_java_src_j2objc_protobuf_tests_moved_shim_j2objc_proto

// Both modules expose moved.proto's header; the types must resolve to one
// declaration.
func shimAndMovedWidget() -> ComGoogleJ2objctestMovedWidget {
  return ComGoogleJ2objctestMovedWidget.newBuilder().build()
}

func shimAndMovedColor() -> ComGoogleJ2objctestMovedColor {
  return ComGoogleJ2objctestMovedColor_get_RED()
}
