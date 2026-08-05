package com.codeforces.app.ui.screens.problems

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeforces.app.ui.components.CfWebView
import com.codeforces.app.ui.components.buildEditorialHtml
import com.codeforces.app.ui.components.buildProblemHtml
import com.codeforces.app.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemDetailScreen(
    contestId: String,
    index: String,
    name: String,
    onLogin: () -> Unit,
    onBack: () -> Unit,
    viewModel: ProblemDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle(initialValue = emptySet())
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Description", "Submit", "Submission", "Solution")

    val isBookmarked = state.detail?.let { "${it.contestId}_${it.index}" in bookmarks } == true

    LaunchedEffect(contestId, index) {
        viewModel.load(contestId, index, name)
    }

    // Re-verify the login session whenever this screen is resumed (e.g. after
    // returning from the Login screen) so the Submit tab reflects reality.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshLoginState(autoLogin = false)
    }

    LaunchedEffect(selectedTabIndex, state.detail?.contestId) {
        when (selectedTabIndex) {
            1 -> viewModel.loadLanguages()
            2 -> viewModel.loadSubmissions()
            3 -> viewModel.loadEditorial()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Problem",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = CfTextPrimary,
                        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).padding(end = 48.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = CfTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Icon(
                            if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) CodeforcesAccent else CfTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        },
        containerColor = CfBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Tabs ──
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = CfSurface,
                contentColor = CfTextPrimary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = CodeforcesAccent,
                        height = 2.dp
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTabIndex == i,
                        onClick = { selectedTabIndex = i },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    title,
                                    color = if (selectedTabIndex == i) CfTextPrimary else CfTextSecondary,
                                    fontWeight = if (selectedTabIndex == i) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                if (title == "Submit") {
                                    Spacer(Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CodeforcesAccent)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "New",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            HorizontalDivider(color = CfDivider, thickness = 1.dp)

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                // ── Loading ──
                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CodeforcesAccent, strokeWidth = 3.dp)
                    }
                }

                // ── Error ──
                if (state.error != null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = CodeforcesAccent,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Failed to load", color = CfTextPrimary, fontWeight = FontWeight.Bold)
                            Text(state.error ?: "", color = CfTextSecondary, fontSize = 13.sp)
                            Button(
                                onClick = { viewModel.load(contestId, index, name) },
                                colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                // ── Content ──
                val detail = state.detail
                if (detail != null && !state.isLoading && state.error == null) {
                    when (selectedTabIndex) {
                        0 -> {
                            // Description tab
                            CfWebView(
                                html = buildProblemHtml(detail.statementHtml),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        1 -> {
                            // Submit tab
                            SubmitTab(state = state, viewModel = viewModel, onLogin = onLogin)
                        }
                        2 -> {
                            // Submission tab
                            SubmissionsTab(state = state, viewModel = viewModel)
                        }
                        else -> {
                            // Solution / Editorial tab
                            val editorial = detail.editorialHtml
                            if (editorial != null) {
                                CfWebView(
                                    html = buildEditorialHtml(editorial),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (state.isEditorialLoading) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = CodeforcesAccent, strokeWidth = 3.dp)
                                }
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "📝",
                                            fontSize = 40.sp
                                        )
                                        Text(
                                            "No solution found",
                                            color = CfTextSecondary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            "A solution for this problem\nis not available in the contest tutorial.",
                                            color = CfTextDisabled,
                                            fontSize = 13.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Hidden browser used to pass Cloudflare and submit code. Lives at
            // screen level so it stays warm across tab switches.
            val loaded = state.detail
            if (loaded != null && !state.isLoading && state.error == null) {
                HiddenSubmitWebView(
                    contestId = loaded.contestId,
                    index = loaded.index,
                    userAgent = state.submitUserAgent,
                    submitRequest = state.submitRequest,
                    reloadTrigger = state.languagesReloadTrigger,
                    onLanguagesLoaded = viewModel::onLanguagesLoaded,
                    onSubmitSucceeded = viewModel::onSubmitSucceeded,
                    onSubmitFailed = viewModel::onSubmitFailed,
                    onSubmitDispatched = viewModel::onSubmitDispatched
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmitTab(
    state: ProblemDetailUiState,
    viewModel: ProblemDetailViewModel,
    onLogin: () -> Unit
) {
    when (state.loginState) {
        is LoginState.LoggedIn, LoginState.Checking -> SubmitForm(state, viewModel)
        LoginState.LoggedOut -> LoginRequired(onLogin = onLogin)
    }
}

@Composable
private fun LoginRequired(onLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            tint = CfTextDisabled,
            modifier = Modifier.size(56.dp)
        )
        Text(
            "Sign in to submit",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = CfTextPrimary
        )
        Text(
            "Submitting a solution requires a real Codeforces account.\nSign in once and your session is kept for future submissions.",
            color = CfTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Button(
            onClick = onLogin,
            colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Login, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sign in with handle & password")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmitForm(
    state: ProblemDetailUiState,
    viewModel: ProblemDetailViewModel
) {
    var selectedLanguage by remember { mutableStateOf<String?>(null) }
    var code by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.languages) {
        if (selectedLanguage == null && state.languages.isNotEmpty()) {
            selectedLanguage = state.languages.first().id
        }
    }

    val selectedLabel = state.languages.firstOrNull { it.id == selectedLanguage }?.label
    val isLoggedIn = state.loginState is LoginState.LoggedIn
    val canSubmit = isLoggedIn && !state.isSubmitting && selectedLanguage != null && code.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isLoggedIn) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    color = CodeforcesAccent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Checking sign-in…",
                    color = CfTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = menuExpanded,
            onExpandedChange = { menuExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedLabel ?: "Loading languages…",
                onValueChange = {},
                readOnly = true,
                label = { Text("Language") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CodeforcesAccent,
                    focusedContainerColor = CfCardSurface,
                    unfocusedContainerColor = CfCardSurface
                )
            )
            ExposedDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                state.languages.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(lang.label, maxLines = 1) },
                        onClick = {
                            selectedLanguage = lang.id
                            menuExpanded = false
                        }
                    )
                }
            }
        }

        if (state.languages.isEmpty() && !state.isSubmitting) {
            TextButton(
                onClick = { viewModel.loadLanguages() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Languages didn't load — retry")
            }
        }

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Source code") },
            modifier = Modifier.fillMaxWidth().height(260.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CodeforcesAccent,
                focusedContainerColor = CfCardSurface,
                unfocusedContainerColor = CfCardSurface
            )
        )

        Button(
            onClick = {
                selectedLanguage?.let { viewModel.submitCode(it, code) }
            },
            enabled = canSubmit,
            colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Submitting…")
            } else {
                Text("Submit Code")
            }
        }

        // Live verdict line while the hidden browser submits + the API polls.
        if (state.isSubmitting) {
            val t = state.trackedSubmission
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    color = CodeforcesAccent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    when {
                        t != null && t.verdict == "TESTING" -> "Judging… test ${t.passedTestCount}"
                        t != null -> "In queue…"
                        else -> "Submitting your solution…"
                    },
                    color = CfTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        state.submitError?.let {
            Text(it, color = VerdictWA, style = MaterialTheme.typography.bodyMedium)
        }
        state.submitResult?.let {
            Text(it, color = verdictColor(state.submitVerdict), style = MaterialTheme.typography.bodyMedium)
        }
        if (state.submitError == null && state.submitResult == null && !state.isSubmitting) {
            Text(
                "Signed in as ${(state.loginState as? LoginState.LoggedIn)?.handle ?: "Codeforces user"}. Your session is used to submit on your behalf.",
                color = CfTextDisabled,
                fontSize = 12.sp
            )
        }
    }
}

// ── Submission history (verdicts from the public API) ─────────────────────────

@Composable
private fun SubmissionsTab(
    state: ProblemDetailUiState,
    viewModel: ProblemDetailViewModel
) {
    val context = LocalContext.current

    if (state.isSubmissionsLoading && state.submissions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CodeforcesAccent, strokeWidth = 3.dp)
        }
        return
    }

    if (state.submissions.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Rounded.History,
                contentDescription = null,
                tint = CfTextDisabled,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "No submissions yet",
                color = CfTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                "Submit a solution from the Submit tab to see it here.",
                color = CfTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = { viewModel.refreshSubmissions() }) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Refresh")
            }
            Spacer(Modifier.height(12.dp))
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.submissions, key = { it.id }) { sub ->
            val color = verdictColor(sub.verdict)
            Card(
                onClick = {
                    val contestId = state.detail?.contestId
                    if (contestId != null) {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://codeforces.com/contest/$contestId/submission/${sub.id}")
                            )
                        )
                    }
                },
                colors = CardDefaults.cardColors(containerColor = CfCardSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(color.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                verdictLabel(sub.verdict),
                                color = color,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text("#${sub.id}", color = CfTextSecondary, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(sub.language, color = CfTextPrimary, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (sub.passedTestCount > 0) {
                            InfoText("tests ${sub.passedTestCount}")
                        }
                        InfoText("${sub.timeMillis} ms")
                        InfoText(formatMemory(sub.memoryBytes))
                        InfoText(relativeTime(sub.creationTimeSeconds))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoText(text: String) {
    Text(text, color = CfTextSecondary, fontSize = 12.sp)
}

private fun formatMemory(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / 1048576.0)
    bytes > 0 -> "${bytes / 1024} KB"
    else -> "0 KB"
}

private fun relativeTime(seconds: Long): String {
    val diff = (System.currentTimeMillis() / 1000) - seconds
    return when {
        diff < 60 -> "just now"
        diff < 3600 -> "${diff / 60}m ago"
        diff < 86400 -> "${diff / 3600}h ago"
        else -> "${diff / 86400}d ago"
    }
}

private fun verdictColor(verdict: String?): Color = when (verdict) {
    "OK" -> VerdictOK
    "WRONG_ANSWER" -> VerdictWA
    "TIME_LIMIT_EXCEEDED" -> VerdictTLE
    "MEMORY_LIMIT_EXCEEDED" -> VerdictMLE
    "RUNTIME_ERROR" -> VerdictRTE
    "COMPILATION_ERROR" -> VerdictCE
    "SKIPPED", "CHALLENGED" -> VerdictSkipped
    else -> CodeforcesAccent
}

// ── Hidden submit WebView ────────────────────────────────────────────────────

private const val TAG = "CFSUBMIT"

private fun submitUrl(contestId: String) =
    "https://codeforces.com/contest/$contestId/submit?locale=en"

private fun gymSubmitUrl(contestId: String) =
    "https://codeforces.com/gym/$contestId/submit?locale=en"

/** Installed once per loaded submit page. Defines the helpers the app calls:
 *  language extraction, form filling + native POST, submission-id and error reads. */
private val INSTALL_HELPERS_JS = """
(function(){
  window.__CF_EXTRACT_LANGS=function(){
    function readOptions(){
      var sel=document.querySelector('select[name="programTypeId"], #programTypeId');
      if(!sel||!sel.options||!sel.options.length)return [];
      var out=[];
      for(var i=0;i<sel.options.length;i++){
        var o=sel.options[i];
        var label=(o.textContent||o.text||'').replace(/\s+/g,' ').trim();
        if(o.value&&label)out.push({id:String(o.value),label:label});
      }
      return out;
    }
    function send(arr){ if(arr.length){ CodeforcesApp.reportLanguages(JSON.stringify(arr)); return true; } return false; }
    try{
      if(send(readOptions()))return;
      fetch('/data/languages'+(window.__CF_CONTEST?'?contestId='+window.__CF_CONTEST:''),{credentials:'same-origin'}).then(function(r){return r.text();}).then(function(t){
        try{
          var body=t.replace(/^[\s\S]*?try\s*\{/,'').replace(/\}\s*catch\s*\([\s\S]*$/,'');
          var arr=null;
          try{arr=JSON.parse(body);}catch(e){arr=null;}
          if(arr&&arr.length&&arr[0].name!==undefined){
            var mapped=arr.map(function(x){return {id:String(x.id),label:String(x.name||'')};}).filter(function(x){return x.id&&x.label;});
            if(send(mapped))return;
          }
          try{(new Function(body))();}catch(e){}
          send(readOptions());
        }catch(e){}
      }).catch(function(){});
    }catch(e){}
  };
  window.__CF_SUBMIT=function(code,langId,index){
    try{
      var form=document.getElementById('submit-form')||document.querySelector('form.submit-form')||document.querySelector('form.submitForm')||document.querySelector('form[action*="csrf_token"]')||document.querySelector('form[action*="/submit"]')||document.querySelector('form');
      if(!form){CodeforcesApp.reportSubmit('error:No submit form found on the page.');return;}
      var sel=document.querySelector('select[name="programTypeId"], #programTypeId');
      if(sel){
        var found=false;
        for(var i=0;i<sel.options.length;i++){if(sel.options[i].value===String(langId)){sel.selectedIndex=i;found=true;break;}}
        if(!found){CodeforcesApp.reportSubmit('error:Language is not available on Codeforces. Pick another one.');return;}
        sel.dispatchEvent(new Event('change',{bubbles:true}));
      }
      var prob=document.querySelector('select[name="submittedProblemIndex"],#submittedProblemCode,select[name="submittedProblemCode"],input[name="submittedProblemCode"]');
      if(prob){prob.value=index;prob.dispatchEvent(new Event('change',{bubbles:true}));}
      var ta=form.querySelector('textarea[name="source"]')||document.getElementById('sourceCodeTextarea');
      if(!ta){CodeforcesApp.reportSubmit('error:No source editor found on the page.');return;}
      ta.value=code;
      ta.dispatchEvent(new Event('input',{bubbles:true}));
      var action=form.getAttribute('action')||'';
      if(action.indexOf('csrf_token')===-1){
        var csrf=form.querySelector('input[name="csrf_token"]')||form.querySelector('input[name="_csrf"]');
        if(csrf&&csrf.value){
          var sep=action.indexOf('?')===-1?'?':'&';
          form.setAttribute('action',action+sep+'csrf_token='+encodeURIComponent(csrf.value));
        }
      }
      form.submit();
    }catch(e){CodeforcesApp.reportSubmit('error:'+String((e&&e.message)||e));}
  };
})()
""".trimIndent()

private const val EXTRACT_LANGS_JS = "window.__CF_EXTRACT_LANGS&&window.__CF_EXTRACT_LANGS();"

private val EXTRACT_SUBMISSION_ID_JS = """
(function(){
  try{
    var a=document.querySelectorAll('a[href*="/submission/"]');
    for(var i=0;i<a.length;i++){
      var m=(a[i].getAttribute('href')||'').match(/\/submission\/(\d+)/);
      if(m)return m[1];
    }
    var r=document.querySelector('[data-submission-id]');
    if(r)return r.getAttribute('data-submission-id');
  }catch(e){}
  return '';
})()
""".trimIndent()

private val EXTRACT_ERROR_JS = """
(function(){
  try{
    var els=document.querySelectorAll('.error, #error, .form-error');
    var seen={};
    for(var i=0;i<els.length;i++){
      var t=(els[i].textContent||'').replace(/\s+/g,' ').trim();
      if(t&&!seen[t]){seen[t]=1;return t;}
    }
    var p=document.querySelector('.error pre, #error pre');
    if(p&&p.textContent.trim())return p.textContent.replace(/\s+/g,' ').trim();
  }catch(e){}
  return '';
})()
""".trimIndent()

/** @JavascriptInterface callbacks from the page run on a background thread,
 *  so everything is re-posted onto the main looper. */
private class SubmitJsBridge(
    private val handler: Handler,
    private val onReady: () -> Unit,
    private val onLanguages: (List<SubmitLanguage>) -> Unit,
    private val onFailed: (String) -> Unit
) {
    @JavascriptInterface
    fun reportLanguages(json: String) {
        val list = parseLanguagesJson(json)
        if (list.isNotEmpty()) {
            handler.post {
                onReady()
                onLanguages(list)
            }
        }
    }

    @JavascriptInterface
    fun reportSubmit(result: String) {
        if (result.startsWith("error:")) {
            val msg = result.removePrefix("error:")
            handler.post { onFailed(msg) }
        }
    }
}

private fun parseLanguagesJson(json: String): List<SubmitLanguage> {
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.getJSONObject(i)
            val id = o.optString("id")
            val label = o.optString("label")
            if (id.isNotBlank() && label.isNotBlank()) SubmitLanguage(id, label) else null
        }
    } catch (_: Exception) {
        emptyList()
    }
}

/** JSON-encode a value so it can be dropped into a JS string literal safely. */
private fun jsString(value: String): String {
    val json = JSONObject().put("v", value).toString()
    return json.substringAfter("\"v\":").dropLast(1)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HiddenSubmitWebView(
    contestId: String,
    index: String,
    userAgent: String?,
    submitRequest: SubmitRequest?,
    reloadTrigger: Int,
    onLanguagesLoaded: (List<SubmitLanguage>) -> Unit,
    onSubmitSucceeded: (Long?) -> Unit,
    onSubmitFailed: (String) -> Unit,
    onSubmitDispatched: () -> Unit
) {
    val context = LocalContext.current
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val pageReady = remember { mutableStateOf(false) }
    val pendingSubmit = remember { mutableStateOf<SubmitRequest?>(null) }
    val submitDispatched = remember { mutableStateOf(false) }
    val gymMode = remember { mutableStateOf(false) }
    val appliedUa = remember { mutableStateOf<String?>(null) }
    val lastReloadTrigger = remember { mutableIntStateOf(-1) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    fun currentSubmitUrl(): String =
        if (gymMode.value) gymSubmitUrl(contestId) else submitUrl(contestId)

    fun maybeDispatch() {
        val req = pendingSubmit.value ?: return
        val view = webViewRef.value ?: return
        val url = view.url ?: ""
        if (!url.contains("/submit") && !url.contains("/problemset/problem")) {
            pageReady.value = false
            view.post { view.loadUrl(currentSubmitUrl()) }
            return
        }
        if (!pageReady.value) return
        val script = "window.__CF_SUBMIT&&window.__CF_SUBMIT(" +
            jsString(req.code) + "," +
            jsString(req.languageId) + "," +
            jsString(index) + ");"
        Log.d(TAG, "dispatching submit")
        view.evaluateJavascript(script, null)
        pendingSubmit.value = null
        submitDispatched.value = true
        onSubmitDispatched()
        mainHandler.postDelayed({
            if (submitDispatched.value) {
                submitDispatched.value = false
                onSubmitFailed("Codeforces didn't respond. Please try again.")
            }
        }, 25000)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                // The cf_clearance cookie is bound to the UA of the WebView that
                // logged in, so this one must present the same UA.
                settings.userAgentString = userAgent ?: WebSettings.getDefaultUserAgent(ctx)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                addJavascriptInterface(
                    SubmitJsBridge(
                        handler = mainHandler,
                        onReady = {
                            pageReady.value = true
                            maybeDispatch()
                        },
                        onLanguages = onLanguagesLoaded,
                        onFailed = { msg ->
                            submitDispatched.value = false
                            pendingSubmit.value = null
                            onSubmitFailed(msg)
                        }
                    ),
                    "CodeforcesApp"
                )

                webViewClient = object : WebViewClient() {
                    override fun onReceivedHttpError(
                        view: WebView,
                        request: android.webkit.WebResourceRequest,
                        errorResponse: android.webkit.WebResourceResponse
                    ) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        // Gym problems aren't under /contest; retry under /gym.
                        if (!gymMode.value && request.isForMainFrame && errorResponse.statusCode == 404) {
                            gymMode.value = true
                            view.post { view.loadUrl(gymSubmitUrl(contestId)) }
                        }
                    }

                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        val u = url ?: return
                        Log.d(TAG, "onPageFinished $u")
                        if (u.contains("/my") || u.contains("/submissions")) {
                            if (submitDispatched.value) {
                                submitDispatched.value = false
                                view.postDelayed({
                                    view.evaluateJavascript(EXTRACT_SUBMISSION_ID_JS) { r ->
                                        val id = r?.trim()?.removeSurrounding("\"")?.toLongOrNull()
                                        mainHandler.post { onSubmitSucceeded(id) }
                                    }
                                }, 1500)
                            }
                            return
                        }
                        if (u.contains("/submit") || u.contains("/problemset")) {
                            view.evaluateJavascript("window.__CF_CONTEST=" + jsString(contestId) + ";", null)
                            view.evaluateJavascript(INSTALL_HELPERS_JS, null)
                            if (submitDispatched.value) {
                                // Came back to the form = validation failed.
                                submitDispatched.value = false
                                view.postDelayed({
                                    view.evaluateJavascript(EXTRACT_ERROR_JS) { r ->
                                        val err = r?.trim()?.removeSurrounding("\"")?.ifBlank { null }
                                        mainHandler.post {
                                            onSubmitFailed(err ?: "Submission failed. Please try again.")
                                        }
                                    }
                                }, 800)
                            } else {
                                view.postDelayed({ view.evaluateJavascript(EXTRACT_LANGS_JS, null) }, 600)
                            }
                        }
                    }
                }

                webViewRef.value = this
                loadUrl(submitUrl(contestId))
            }
        },
        update = { view ->
            // The saved login UA (which cf_clearance is bound to) may arrive
            // after the first load; apply it once and reload so the cookie matches.
            val targetUa = userAgent ?: WebSettings.getDefaultUserAgent(view.context)
            if (appliedUa.value != targetUa) {
                appliedUa.value = targetUa
                if (view.settings.userAgentString != targetUa) {
                    view.settings.userAgentString = targetUa
                    pageReady.value = false
                    view.reload()
                }
            }
            if (reloadTrigger != lastReloadTrigger.intValue) {
                lastReloadTrigger.intValue = reloadTrigger
                view.evaluateJavascript(EXTRACT_LANGS_JS, null)
            }
            val request = submitRequest
            if (request != null && pendingSubmit.value == null) {
                pendingSubmit.value = request
                maybeDispatch()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    )
}
