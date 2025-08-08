package hr.unipu.journals.data.enumeration

enum class ManuscriptState {
    AWAITING_INITIAL_EIC_REVIEW,
    AWAITING_INITIAL_EDITOR_REVIEW,
    AWAITING_REVIEWER_REVIEW,
    MINOR_FIXES,
    MAJOR_FIXES,
    REJECTED,
    PUBLISHED,
    HIDDEN,
    DRAFT,
}