package beyondeyesight.util


import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class InstantToLocalDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
        val instant = Instant.parse(p.text)
        return instant.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
    }
}