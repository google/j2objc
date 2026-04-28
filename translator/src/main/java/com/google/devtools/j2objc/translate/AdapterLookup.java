package com.google.devtools.j2objc.translate;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;

import com.google.common.collect.ImmutableList;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import org.jspecify.annotations.Nullable;

/** Utility class for finding converter functions in an adapter class using J2ObjC AST/Types. */
public final class AdapterLookup {

  private static final MatchResult EXACT_MATCH = new MatchResult(MatchType.EXACT, 0);
  private static final MatchResult NO_MATCH = new MatchResult(MatchType.NONE, -1);
  private static final MatchResult NO_MATCH_EXACT_REQUIRED =
      new MatchResult(MatchType.NONE_EXACT_REQUIRED, -1);
  private final TypeUtil typeUtil;
  // Cache for converter functions to avoid repeated lookups.
  private final ConcurrentHashMap<CacheKey, CacheValue> converterCache = new ConcurrentHashMap<>();

  AdapterLookup(TypeUtil typeUtil) {
    this.typeUtil = typeUtil;
  }

  public ConverterMatch findConverterByParamType(
      TypeElement adapterElement, TypeMirror targetType) {
    return findConverter(
        adapterElement,
        targetType,
        "to",
        ExecutableElement::getReturnType,
        func -> func.getParameters().get(0).asType());
  }

  public ConverterMatch findConverterByReturnType(
      TypeElement adapterElement, TypeMirror paramType) {
    return findConverter(
        adapterElement,
        paramType,
        "from",
        func -> func.getParameters().get(0).asType(),
        ExecutableElement::getReturnType);
  }

  private ConverterMatch findConverter(
      TypeElement adapterElement,
      TypeMirror type,
      String prefix,
      TypeSelector getMatchingType,
      TypeSelector getTargetElement) {

    CacheValue result =
        converterCache.computeIfAbsent(
            new CacheKey(adapterElement, type, prefix),
            cacheKey -> {
              // Find all applicable functions with the given prefix and matching parameter count.
              ImmutableList<ExecutableElement> applicableFunctions =
                  stream(ElementUtil.getMethods(adapterElement))
                      .filter(
                          function ->
                              function.getParameters().size() == 1
                                  && function.getSimpleName().toString().startsWith(prefix))
                      .collect(toImmutableList());

              // Find the best match for each applicable function based on #matchTypes.
              ImmutableList<MatchCandidate> matchesWithResult =
                  applicableFunctions.stream()
                      .map(
                          function -> {
                            TypeMirror matchingType = getMatchingType.select(function);
                            MatchResult matchResult =
                                matchTypes(matchingType, type, function.getTypeParameters());
                            int depth = getDepth(matchingType);
                            return new MatchCandidate(function, matchResult, depth);
                          })
                      .filter(m -> m.result().type() != MatchType.NONE)
                      .collect(toImmutableList());

              if (matchesWithResult.isEmpty()) {
                return new CacheValue(MatchType.NONE, null);
              }

              MatchCandidate best = Collections.min(matchesWithResult);

              // Find if there are multiple functions with the same best match depth
              ImmutableList<ExecutableElement> bestMatches =
                  matchesWithResult.stream()
                      .filter(m -> m.result().equals(best.result()) && m.depth() == best.depth())
                      .map(MatchCandidate::function)
                      .collect(toImmutableList());

              long distinctTargetElements =
                  bestMatches.stream().map(getTargetElement::select).distinct().count();

              if (distinctTargetElements > 1) {
                throw new IllegalStateException(
                    "Ambiguous converters found in adapter "
                        + adapterElement.getSimpleName()
                        + " with prefix "
                        + prefix
                        + " for "
                        + type
                        + ".\n"
                        + "Matches: "
                        + bestMatches.stream()
                            .map(f -> f.getSimpleName().toString())
                            .collect(Collectors.joining(", ")));
              }

              return new CacheValue(best.result().type(), bestMatches.get(0));
            });

    return new ConverterMatch(result.matchType(), result.function());
  }

  /**
   * Matches a generic type from the adapter function signature against a concrete type from the
   * target function signature.
   *
   * @param genericType the generic type from the adapter function signature
   * @param concreteType the concrete type from the target function signature
   * @param typeParameters the type parameters of the adapter function
   * @param typeUtil the type utility class
   */
  private MatchResult matchTypes(
      TypeMirror genericType,
      TypeMirror concreteType,
      List<? extends TypeParameterElement> typeParameters) {

    // Match if identical types.
    if (typeUtil.isSameType(genericType, concreteType)) {
      return EXACT_MATCH;
    }

    // Match if the generic type is a type variable.
    if (genericType.getKind() == TypeKind.TYPEVAR
        && genericType instanceof TypeVariable tv
        && typeParameters.contains(tv.asElement())) {
      // If the concrete type is a mapped type, then return a NO_MATCH_EXACT_REQUIRED because it
      // must be matched exactly, otherwise return a WILDCARD match.
      if (isMappedTypeOrContainsMappedType(concreteType)) {
        return NO_MATCH_EXACT_REQUIRED;
      } else {
        return new MatchResult(MatchType.WILDCARD, 1);
      }
    }

    // Match if the generic type and concrete type are not the same kind.
    if (genericType.getKind() != concreteType.getKind()) {
      return NO_MATCH;
    }

    if (genericType instanceof DeclaredType gDeclared
        && concreteType instanceof DeclaredType cDeclared) {
      // If the generic type and concrete type are not the same type, return a NO_MATCH.
      if (!gDeclared.asElement().equals(cDeclared.asElement())) {
        return NO_MATCH;
      }
      // If the generic type and concrete type have different number of type arguments, return a
      // NO_MATCH.
      if (gDeclared.getTypeArguments().size() != cDeclared.getTypeArguments().size()) {
        return NO_MATCH;
      }

      MatchType maxType = MatchType.EXACT;
      int totalScore = 0;

      // Zip through the type arguments and match them recursively.
      for (int i = 0; i < gDeclared.getTypeArguments().size(); i++) {
        TypeMirror gArg = gDeclared.getTypeArguments().get(i);
        TypeMirror cArg = cDeclared.getTypeArguments().get(i);

        MatchResult result;
        if (gArg.getKind() == TypeKind.WILDCARD && cArg.getKind() == TypeKind.WILDCARD) {
          WildcardType gWild = (WildcardType) gArg;
          WildcardType cWild = (WildcardType) cArg;
          if (gWild.getExtendsBound() == null
              && gWild.getSuperBound() == null
              && cWild.getExtendsBound() == null
              && cWild.getSuperBound() == null) {
            // Both are wildcards with no bounds, so return an EXACT match.
            result = EXACT_MATCH;
          } else {
            result = NO_MATCH;
          }
        } else if (gArg.getKind() == TypeKind.WILDCARD) {

          // If the generic type is a wildcard with no bounds,
          WildcardType gWild = (WildcardType) gArg;
          if (gWild.getExtendsBound() == null && gWild.getSuperBound() == null) {
            // If the concrete type is a mapped type, then return a NO_MATCH_EXACT_REQUIRED because
            // it must be matched exactly, otherwise return a WILDCARD match.
            if (isMappedTypeOrContainsMappedType(cArg)) {
              return NO_MATCH_EXACT_REQUIRED;
            } else {
              result = new MatchResult(MatchType.WILDCARD, 2);
            }
          } else {
            // The generic type is a wildcard with bounds, so return a NO_MATCH.
            // Need to revisit if we need to handle this.
            result = NO_MATCH;
          }
        } else {
          // Recursively match the generic type and concrete type.
          result = matchTypes(gArg, cArg, typeParameters);
        }
        // If the result is a MatchType.NO_MATCH, then return a MatchResult NO_MATCH.
        if (result.type() == MatchType.NONE) {
          return NO_MATCH;
        }
        if (result.type().rank() > maxType.rank()) {
          maxType = result.type();
        }
        totalScore += result.score();
      }
      return new MatchResult(maxType, totalScore);
    }

    return NO_MATCH;
  }

  /** The type of match found between a generic type and a concrete type. */
  public enum MatchType {
    EXACT(0),
    WILDCARD(1),
    NONE(2),
    NONE_EXACT_REQUIRED(3);

    private final int rank;

    MatchType(int rank) {
      this.rank = rank;
    }

    public int rank() {
      return rank;
    }
  }

  /** Represents the result of matching a generic type against a concrete type. */
  public record MatchResult(MatchType type, int score) {}

  /** Represents a candidate function for conversion, along with its match result and depth. */
  public record MatchCandidate(ExecutableElement function, MatchResult result, int depth)
      implements Comparable<MatchCandidate> {
    @Override
    public int compareTo(MatchCandidate other) {
      // Sort by match type first.
      int typeComp = this.result.type.compareTo(other.result.type);
      if (typeComp != 0) {
        return typeComp;
      }

      // Sort by depth second.
      int depthComp = Integer.compare(other.depth, this.depth); // Descending
      if (depthComp != 0) {
        return depthComp;
      }

      // Sort by score third.
      return Integer.compare(this.result.score, other.result.score);
    }
  }

  /** Recursively determines the type argument depth of a type. */
  private static int getDepth(TypeMirror type) {
    if (!(type instanceof DeclaredType declaredType)) {
      return 0;
    }
    if (declaredType.getTypeArguments().isEmpty()) {
      return 0;
    }
    int maxArgDepth = 0;
    for (TypeMirror arg : declaredType.getTypeArguments()) {
      maxArgDepth = Math.max(maxArgDepth, getDepth(arg));
    }
    return 1 + maxArgDepth;
  }

  private record CacheKey(TypeElement adapterElement, TypeMirror type, String prefix) {}

  private record CacheValue(MatchType matchType, @Nullable ExecutableElement function) {}

  private boolean isMappedTypeOrContainsMappedType(TypeMirror type) {
    return isRequiredMappedType(type) || containsMappedTypeInTypeArguments(type);
  }

  private boolean containsMappedTypeInTypeArguments(TypeMirror type) {
    if (!(type instanceof DeclaredType declaredType)) {
      return false;
    }
    return declaredType.getTypeArguments().stream()
        .anyMatch(arg -> isRequiredMappedType(arg) || containsMappedTypeInTypeArguments(arg));
  }

  // These are the types that are required to be mapped exactly in type parameters.
  private boolean isRequiredMappedType(TypeMirror type) {
    TypeMirror erasedType = typeUtil.erasure(type);
    return Stream.of("java.util.List", "java.util.Map", "java.util.Set", "java.lang.Boolean")
        .map(typeUtil::resolveJavaType)
        .filter(Objects::nonNull)
        .anyMatch(element -> typeUtil.isSubtype(erasedType, typeUtil.erasure(element.asType())));
  }

  private interface TypeSelector {
    TypeMirror select(ExecutableElement function);
  }

  /** Represents the best matching converter function found. */
  public record ConverterMatch(MatchType matchType, ExecutableElement converter) {}
}
