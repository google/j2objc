import Foundation
import third_party_java_src_j2objc_protobuf_tests_partial_shim_j2objc_proto

// Uses the shim's own type and, through it, the re-exported types.
func partialShimGadget() -> ComGoogleJ2objctestPartialGadget {
  return ComGoogleJ2objctestPartialGadget_getDefaultInstance()
}

func partialShimWidget() -> ComGoogleJ2objctestMovedWidget {
  return partialShimGadget().getWidget()
}

func partialShimColor() -> ComGoogleJ2objctestMovedColor {
  return partialShimGadget().getColor()
}
