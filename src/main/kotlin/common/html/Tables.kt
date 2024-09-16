package dev.limebeck.openconf.common.html

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.html.*

typealias RowDataProvider = Map<String, TD.() -> Unit>
typealias TableDataProvider = List<RowDataProvider>

inline fun HtmlBlockTag.tableFrom(block: () -> TableDataProvider) {
    val data = block()
    if (data.isEmpty()) {
        div {
            p { +"NO DATA" }
        }
    } else {
        val columnHeaders = data.first().keys
        table {
            thead {
                tr {
                    columnHeaders.forEach { columnName ->
                        td {
                            p { +columnName }
                        }
                    }
                }
            }
            tbody {
                data.forEach { row ->
                    tr {
                        row.forEach { (_, value) ->
                            td { value() }
                        }
                    }
                }
            }
        }
    }
}

suspend fun <T> ApplicationCall.respondTable(
    title: String,
    dataProvider: suspend () -> List<T>,
    viewMapper: T.() -> RowDataProvider
) {
    val data = dataProvider()
    respondHtml(HttpStatusCode.OK) {
        head {
            addCodeHighlight()
            addTablesStyle()
            title { +title }
        }
        body {
            div {
                tableFrom {
                    data.map { it.viewMapper() }
                }
            }
        }
    }
}
