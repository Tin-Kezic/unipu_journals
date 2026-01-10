package hr.unipu.journals.feature.manuscript.review

data class ReviewerWithRounds(
    val reviewer: Int,
    val rounds: List<Int>,
)