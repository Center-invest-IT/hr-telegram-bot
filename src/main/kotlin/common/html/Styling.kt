package dev.limebeck.openconf.common.html

import dev.limebeck.openconf.common.MSK_ZONE
import kotlinx.html.*
import java.time.Instant


fun HEAD.addTablesStyle() {
    link(
        "https://cdn.jsdelivr.net/npm/water.css@2/out/water.css",
        rel = "stylesheet"
    )
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

context (HtmlBlockTag)
fun Instant.toTableCell() = this.atZone(MSK_ZONE).toString().trimEnd('Z').split('T').map {
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