package hr.unipu.journals.feature.manuscript

enum class ManuscriptState {
    AWAITING_EIC_REVIEW,
    AWAITING_EDITOR_REVIEW,
    AWAITING_REVIEWER_REVIEW,
    MINOR,
    MAJOR,
    REJECTED,
    PUBLISHED,
    HIDDEN,
    DRAFT,
    ARCHIVED
}