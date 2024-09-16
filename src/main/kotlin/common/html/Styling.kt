package dev.limebeck.openconf.common.html

import kotlinx.html.*
import java.time.Instant


fun HEAD.addTablesStyle() {
    link(
        "https://cdn.jsdelivr.net/npm/water.css@2/out/water.css",
        rel = "stylesheet"
    )
//    style {
//        unsafe {
//            +"""
//            table {
//                width: 100%;
//            }
//            table, th, td {
//                border: 1px solid black;
//                border-collapse: collapse;
//            }
//            pre {
//                white-space: pre-wrap;       /* Since CSS 2.1 */
//                white-space: -moz-pre-wrap;  /* Mozilla, since 1999 */
//                white-space: -pre-wrap;      /* Opera 4-6 */
//                white-space: -o-pre-wrap;    /* Opera 7 */
//                word-wrap: break-word;       /* Internet Explorer 5.5+ */
//            }
//        """.trimIndent()
//        }
//    }
}

fun HEAD.addCodeHighlight() {
    link(
        rel = "stylesheet",
        href = "//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.5.1/styles/default.min.css"
    )
    script(src = "//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.5.1/highlight.min.js") {

    }
    script {
        unsafe { +"hljs.highlightAll();" }
    }
}
//
//fun HtmlBlockTag.addPaginationLinks(pagination: Pagination, paginatedResult: PaginatedResult<*>) {
//    val baseUri = request.path()
//    val totalPages = BigDecimal(paginatedResult.total)
//        .divide(BigDecimal(paginatedResult.limit), RoundingMode.UP)
//        .toInt()
//
//    div {
//        p { +"Total: ${paginatedResult.total}" }
//        p { +"Per page: ${paginatedResult.limit}" }
//        p { +"On this page: ${paginatedResult.list.size}" }
//        p { +"Current page: ${paginatedResult.page}" }
//        p { +"Total pages: $totalPages" }
//        if (totalPages >= 0) {
//            if (totalPages - 1 > paginatedResult.page) {
//                p { a("$baseUri?page=${paginatedResult.page + 1}&limit=${pagination.limit}") { +"Next page" } }
//            }
//            if (0 < paginatedResult.page) {
//                p { a("$baseUri?page=${paginatedResult.page - 1}&limit=${pagination.limit}") { +"Prev page" } }
//            }
//        }
//    }
//}

context (HtmlBlockTag)
fun Instant.toTableCell() = toString().trimEnd('Z').split('T').map {
    div {
        p { +it }
    }
}

context (HtmlBlockTag)
inline fun codeBlock(title: String, language: String? = null, crossinline codeProvider: () -> String) {
    p {
        +title
    }
    pre {
        code(classes = language?.let { "language-$it" }) {
            +codeProvider()
        }
    }
}