package com.codeforces.app.ui.components

import androidx.compose.ui.graphics.Color
import com.codeforces.app.ui.theme.CfTextSecondary
import com.codeforces.app.ui.theme.CodeforcesAccent
import com.codeforces.app.ui.theme.VerdictCE
import com.codeforces.app.ui.theme.VerdictMLE
import com.codeforces.app.ui.theme.VerdictOK
import com.codeforces.app.ui.theme.VerdictRTE
import com.codeforces.app.ui.theme.VerdictSkipped
import com.codeforces.app.ui.theme.VerdictTLE
import com.codeforces.app.ui.theme.VerdictWA

/** Theme color for a Codeforces API verdict. */
fun verdictColor(verdict: String?): Color = when (verdict) {
    "OK" -> VerdictOK
    "WRONG_ANSWER" -> VerdictWA
    "TIME_LIMIT_EXCEEDED" -> VerdictTLE
    "MEMORY_LIMIT_EXCEEDED" -> VerdictMLE
    "RUNTIME_ERROR" -> VerdictRTE
    "COMPILATION_ERROR" -> VerdictCE
    "SKIPPED", "CHALLENGED" -> VerdictSkipped
    "TESTING" -> Color(0xFFFFC107)
    null, "", "IN_QUEUE" -> CodeforcesAccent
    else -> CfTextSecondary
}

/** Display label for a Codeforces API verdict. */
fun verdictLabel(verdict: String?): String = when (verdict) {
    null, "", "IN_QUEUE" -> "In queue"
    "TESTING" -> "Testing…"
    "OK" -> "Accepted"
    "WRONG_ANSWER" -> "Wrong answer"
    "TIME_LIMIT_EXCEEDED" -> "Time limit exceeded"
    "MEMORY_LIMIT_EXCEEDED" -> "Memory limit exceeded"
    "RUNTIME_ERROR" -> "Runtime error"
    "COMPILATION_ERROR" -> "Compilation error"
    "IDLENESS_LIMIT_EXCEEDED" -> "Idleness limit exceeded"
    "PRESENTATION_ERROR" -> "Presentation error"
    "SECURITY_VIOLATED" -> "Security violated"
    "CHALLENGED" -> "Hacked"
    "SKIPPED" -> "Skipped"
    "PARTIAL" -> "Partial"
    "CRASHED" -> "Crashed"
    "REJECTED" -> "Rejected"
    else -> verdict ?: "In queue"
}

/** Compact verdict tag used in list rows ("AC", "WA", …). */
fun verdictShort(verdict: String?): String = when (verdict) {
    "OK" -> "AC"
    "WRONG_ANSWER" -> "WA"
    "TIME_LIMIT_EXCEEDED" -> "TLE"
    "MEMORY_LIMIT_EXCEEDED" -> "MLE"
    "RUNTIME_ERROR" -> "RTE"
    "COMPILATION_ERROR" -> "CE"
    "IDLENESS_LIMIT_EXCEEDED" -> "ILE"
    "CHALLENGED" -> "HACK"
    "SKIPPED" -> "SKIP"
    "PARTIAL" -> "PRTL"
    "CRASHED" -> "CRSH"
    "REJECTED" -> "REJ"
    "TESTING" -> "RUN"
    else -> verdict?.take(6) ?: "—"
}

/** True once the judge has fully finished with this submission. */
fun isVerdictFinal(verdict: String?): Boolean =
    verdict != null && verdict != "" && verdict != "TESTING" && verdict != "IN_QUEUE"

/** True while the submission is queued or being judged. */
fun isVerdictRunning(verdict: String?): Boolean = !isVerdictFinal(verdict)

/** Verdicts where pointing at the failing test number makes sense. */
fun hasFailingTestNumber(verdict: String?): Boolean = verdict in setOf(
    "WRONG_ANSWER", "TIME_LIMIT_EXCEEDED", "MEMORY_LIMIT_EXCEEDED",
    "RUNTIME_ERROR", "IDLENESS_LIMIT_EXCEEDED", "PRESENTATION_ERROR"
)
