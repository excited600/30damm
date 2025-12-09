package beyondeyesight.domain.model

data class ScrollResult<I, C>(
    val items: List<I>,
    val cursor: C?,
    val hasNext: Boolean
)