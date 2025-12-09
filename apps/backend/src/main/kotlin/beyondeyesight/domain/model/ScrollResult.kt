package beyondeyesight.domain.model

import java.util.UUID

data class ScrollResult<T>(
    val items: List<T>,
    val cursor: UUID?,
    val hasNext: Boolean
)