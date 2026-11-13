// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#import "com/google/j2objc/kmp/BaseTaggedInterface.h"
#import "com/google/j2objc/kmp/ConcreteTaggedBase.h"
#import "com/google/j2objc/kmp/CovariantListSubService.h"
#import "com/google/j2objc/kmp/DefaultShimSubService.h"
#import "com/google/j2objc/kmp/GenericCovariantBase.h"
#import "com/google/j2objc/kmp/GenericItemBase.h"
#import "com/google/j2objc/kmp/Item.h"
#import "com/google/j2objc/kmp/JavaSubService.h"
#import "com/google/j2objc/kmp/KmpCallers.h"
#import "com/google/j2objc/kmp/KmpSubItemProtocol.h"
#import "com/google/j2objc/kmp/MidBridgeService.h"
#import "com/google/j2objc/kmp/OpenService.h"
#import "com/google/j2objc/kmp/SubDefaultTaggedInterface.h"
#import "com/google/j2objc/kmp/SubItem.h"
#import "com/google/j2objc/kmp/SubItemService.h"
#import "java/util/List.h"

#import <XCTest/XCTest.h>

@interface NativeOpenService : KmpOpenService
@property(nonatomic, copy) NSArray<NSString *> *nativeRecorded;
@end

@implementation NativeOpenService

- (NSArray<NSString *> *)fetchTags:(NSArray<NSString *> *)prefix {
  return @[ [NSString stringWithFormat:@"objc-direct:%@", prefix.firstObject] ];
}

- (void)recordTags:(NSArray<NSString *> *)tags {
  self.nativeRecorded = tags;
}

@end

@interface NativeGrandSubService : KmpJavaSubService
@end

@implementation NativeGrandSubService

- (NSArray<NSString *> *)fetchTags:(NSArray<NSString *> *)prefix {
  return @[ [NSString stringWithFormat:@"objc-grandsub:%@", prefix.firstObject] ];
}

@end

@interface NativeSubItemService : KmpSubItemService
@end

@implementation NativeSubItemService

- (KmpItem *)selectItem:(NSArray<NSString *> *)keys {
  return [[KmpSubItem alloc]
      initWithNSString:[NSString stringWithFormat:@"objc-subitem:%@", keys.firstObject]];
}

@end

@interface NativeCovariantListService : KmpCovariantListSubService
@end

@implementation NativeCovariantListService

- (NSObject *)computeWithTag:(id)tag items:(NSArray<NSString *> *)items {
  return @[ [NSString stringWithFormat:@"objc-covariant:%@:%@", [tag description], items.firstObject] ];
}

@end

@interface NativeDefaultShimSubService : KmpDefaultShimSubService
@end

@implementation NativeDefaultShimSubService

- (NSArray<NSString *> *)resolveLabels:(NSArray<NSString *> *)seed {
  return @[ [NSString stringWithFormat:@"objc-shim:%@", seed.firstObject] ];
}

@end

@interface NativeMidBridgeService : KmpMidBridgeService
@end

@implementation NativeMidBridgeService

- (KmpSubItem *)pickItem:(NSArray<NSString *> *)items {
  return [[KmpSubItem alloc]
      initWithNSString:[NSString stringWithFormat:@"objc-bridge:%@", items.firstObject]];
}

@end

@interface KmpNativeOverrideTest : XCTestCase
@end

@implementation KmpNativeOverrideTest

- (void)testDirectNativeOverride_getterAndVoidSetter {
  KmpOpenService *javaBase = [[KmpOpenService alloc] init];
  NativeOpenService *nativeSub = [[NativeOpenService alloc] init];
  id<JavaUtilList> seed = [KmpCallers singletonListWithNSString:@"alpha"];

  id<JavaUtilList> baseTags = [KmpCallers callOpenServiceGetTagsWithKmpOpenService:javaBase
                                                                  withJavaUtilList:seed];
  XCTAssertEqualObjects([baseTags getWithInt:0], @"java-base:alpha");
  [KmpCallers callOpenServiceSetTagsWithKmpOpenService:javaBase withJavaUtilList:seed];
  XCTAssertEqualObjects([[javaBase getRecordedTags] getWithInt:0], @"alpha");

  id<JavaUtilList> overriddenTags = [KmpCallers callOpenServiceGetTagsWithKmpOpenService:nativeSub
                                                                        withJavaUtilList:seed];
  XCTAssertEqualObjects([overriddenTags getWithInt:0], @"objc-direct:alpha");
  XCTAssertEqualObjects([nativeSub fetchTags:@[ @"beta" ]], @[ @"objc-direct:beta" ]);

  [KmpCallers callOpenServiceSetTagsWithKmpOpenService:nativeSub withJavaUtilList:seed];
  XCTAssertEqualObjects(nativeSub.nativeRecorded, @[ @"alpha" ]);
  XCTAssertEqual([[nativeSub getRecordedTags] size], 0);
}

- (void)testMultiLevelHierarchy_javaSubclassAndNativeGrandSubclass {
  KmpJavaSubService *javaSub = [[KmpJavaSubService alloc] init];
  NativeGrandSubService *nativeGrandSub = [[NativeGrandSubService alloc] init];
  id<JavaUtilList> seed = [KmpCallers singletonListWithNSString:@"gamma"];

  id<JavaUtilList> javaSubViaBase = [KmpCallers callOpenServiceGetTagsWithKmpOpenService:javaSub
                                                                        withJavaUtilList:seed];
  id<JavaUtilList> javaSubDirect =
      [KmpCallers callJavaSubServiceGetTagsWithKmpJavaSubService:javaSub withJavaUtilList:seed];
  XCTAssertEqualObjects([javaSubViaBase getWithInt:0], @"java-sub:gamma");
  XCTAssertEqualObjects([javaSubDirect getWithInt:0], @"java-sub:gamma");
  XCTAssertEqualObjects([javaSub fetchTags:@[ @"gamma" ]], @[ @"java-sub:gamma" ]);

  id<JavaUtilList> grandSubViaBase =
      [KmpCallers callOpenServiceGetTagsWithKmpOpenService:nativeGrandSub withJavaUtilList:seed];
  id<JavaUtilList> grandSubViaMid =
      [KmpCallers callJavaSubServiceGetTagsWithKmpJavaSubService:nativeGrandSub
                                                withJavaUtilList:seed];
  XCTAssertEqualObjects([grandSubViaBase getWithInt:0], @"objc-grandsub:gamma");
  XCTAssertEqualObjects([grandSubViaMid getWithInt:0], @"objc-grandsub:gamma");
}

- (void)testBoundedGenericBase_nativeSubclassOverrideCastsReturn {
  KmpSubItemService *javaSub = [[KmpSubItemService alloc] init];
  NativeSubItemService *nativeSub = [[NativeSubItemService alloc] init];
  id<JavaUtilList> keys = [KmpCallers singletonListWithNSString:@"k1"];

  KmpSubItem *javaResult = [KmpCallers callSubItemServiceSelectWithKmpSubItemService:javaSub
                                                                    withJavaUtilList:keys];
  XCTAssertEqualObjects([javaResult getName], @"java-subitem:k1");

  KmpSubItem *viaGenericBase =
      [KmpCallers callGenericItemBaseSelectWithKmpGenericItemBase:nativeSub withJavaUtilList:keys];
  KmpSubItem *viaSubService = [KmpCallers callSubItemServiceSelectWithKmpSubItemService:nativeSub
                                                                       withJavaUtilList:keys];
  XCTAssertEqualObjects([viaGenericBase getName], @"objc-subitem:k1");
  XCTAssertEqualObjects([viaSubService getName], @"objc-subitem:k1");
}

- (void)testCovariantReturnNarrowing_nativeSubclassOverride {
  KmpCovariantListSubService *javaSub = [[KmpCovariantListSubService alloc] init];
  NativeCovariantListService *nativeSub = [[NativeCovariantListService alloc] init];
  id<JavaUtilList> items = [KmpCallers singletonListWithNSString:@"item"];

  id<JavaUtilList> javaResult =
      [KmpCallers callCovariantListSubComputeWithKmpCovariantListSubService:javaSub
                                                                    withInt:7
                                                           withJavaUtilList:items];
  XCTAssertEqualObjects([javaResult getWithInt:0], @"java-covariant:7:item");

  id<JavaUtilList> nativeViaSub =
      [KmpCallers callCovariantListSubComputeWithKmpCovariantListSubService:nativeSub
                                                                    withInt:7
                                                           withJavaUtilList:items];
  XCTAssertEqualObjects([nativeViaSub getWithInt:0], @"objc-covariant:7:item");

  id nativeViaBase =
      [KmpCallers callGenericCovariantBaseComputeWithKmpGenericCovariantBase:nativeSub
                                                                     withInt:7
                                                            withJavaUtilList:items];
  XCTAssertTrue([nativeViaBase conformsToProtocol:@protocol(JavaUtilList)]);
  XCTAssertEqualObjects([(id<JavaUtilList>)nativeViaBase getWithInt:0], @"objc-covariant:7:item");
  XCTAssertEqualObjects([nativeSub computeWithTag:@7 items:@[ @"direct" ]],
                        @[ @"objc-covariant:7:direct" ]);
}

- (void)testSubinterfaceDefaultMethodShim_nativeSubclassOverride {
  KmpConcreteTaggedBase *concreteBase = [[KmpConcreteTaggedBase alloc] init];
  KmpDefaultShimSubService *javaShimSub = [[KmpDefaultShimSubService alloc] init];
  NativeDefaultShimSubService *nativeShimSub = [[NativeDefaultShimSubService alloc] init];
  id<JavaUtilList> seed = [KmpCallers singletonListWithNSString:@"tag"];

  id<JavaUtilList> baseLabels =
      [KmpCallers callConcreteTaggedResolveWithKmpConcreteTaggedBase:concreteBase
                                                    withJavaUtilList:seed];
  XCTAssertEqualObjects([baseLabels getWithInt:0], @"base-default:tag");

  id<JavaUtilList> shimLabels =
      [KmpCallers callDefaultShimResolveWithKmpDefaultShimSubService:javaShimSub
                                                    withJavaUtilList:seed];
  XCTAssertEqualObjects([shimLabels getWithInt:0], @"sub-default:tag");

  id<JavaUtilList> nativeViaShim =
      [KmpCallers callDefaultShimResolveWithKmpDefaultShimSubService:nativeShimSub
                                                    withJavaUtilList:seed];
  id<JavaUtilList> nativeViaBase =
      [KmpCallers callConcreteTaggedResolveWithKmpConcreteTaggedBase:nativeShimSub
                                                    withJavaUtilList:seed];
  id<JavaUtilList> nativeViaInterface =
      [KmpCallers callBaseTaggedResolveWithKmpBaseTaggedInterface:nativeShimSub
                                                 withJavaUtilList:seed];
  XCTAssertEqualObjects([nativeViaShim getWithInt:0], @"objc-shim:tag");
  XCTAssertEqualObjects([nativeViaBase getWithInt:0], @"objc-shim:tag");
  XCTAssertEqualObjects([nativeViaInterface getWithInt:0], @"objc-shim:tag");
}

- (void)testInheritedGenericSuperclassBridge_nativeSubclassOverride {
  KmpMidBridgeService *javaBridge = [[KmpMidBridgeService alloc] init];
  NativeMidBridgeService *nativeBridge = [[NativeMidBridgeService alloc] init];
  id<JavaUtilList> items = [KmpCallers singletonListWithNSString:@"x"];

  KmpSubItem *javaPicked = [KmpCallers callMidBridgePickWithKmpMidBridgeService:javaBridge
                                                               withJavaUtilList:items];
  XCTAssertEqualObjects([javaPicked getName], @"java-super-bridge");

  KmpSubItem *nativeViaClass = [KmpCallers callMidBridgePickWithKmpMidBridgeService:nativeBridge
                                                                   withJavaUtilList:items];
  KmpSubItem *nativeViaProtocol =
      [KmpCallers callSubItemProtocolPickWithKmpSubItemProtocol:nativeBridge
                                               withJavaUtilList:items];
  XCTAssertEqualObjects([nativeViaClass getName], @"objc-bridge:x");
  XCTAssertEqualObjects([nativeViaProtocol getName], @"objc-bridge:x");
}

@end
