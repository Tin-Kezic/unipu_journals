package hr.unipu.journals.feature.manuscript.core

enum class ManuscriptStateFilter {
    ALL_AWAITING_REVIEW,
    AWAITING_EIC_REVIEW,
    AWAITING_EDITOR_REVIEW,
    AWAITING_ROUND_INITIALIZATION,
    AWAITING_REVIEWER_REVIEW,
    MINOR_MAJOR,
    PUBLISHED,
    REJECTED,
    ARCHIVED,
    HIDDEN
}