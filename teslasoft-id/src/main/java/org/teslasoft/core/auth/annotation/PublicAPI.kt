package org.teslasoft.core.auth.annotation

@Suppress("unused")
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.FILE
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class PublicAPI