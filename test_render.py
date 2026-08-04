import re

with open("test_output.html", "r") as f:
    html = f.read()

CF_DARK_CSS = """
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=3.0">
<style>
  :root {
    --bg: #151515;
    --surface: #1E1E1E;
    --border: #333333;
    --text: #E0E0E0;
    --muted: #9E9E9E;
    --red: #EF5350;
    --blue: #4FC3F7;
    --mono: 'Roboto Mono', 'Courier New', monospace;
  }

  body {
    background: var(--bg) !important;
    color: var(--text) !important;
  }

  /* ── Hide Codeforces page chrome we don't want ── */
  #header, #footer, .roundbox.menu-box, .second-level-menu,
  .contest-name, #sidebar, .sidebar, #pageContent > *:not(.problemindexholder),
  .problemindexholder > *:not(.problem-statement),
  .alert, .top-links, .userbox, .lang-chooser, #navigation {
    display: none !important;
  }

  /* Make problem statement fit well */
  .problem-statement { 
    display: block !important;
    margin: 0 !important;
    padding: 0 !important;
    font-size: 14.5px !important;
    line-height: 1.55 !important;
  }

  #pageContent {
    margin: 0 !important;
    padding: 16px !important;
  }

  /* ── Fix text colors for dark mode ── */
  .problem-statement, .problem-statement p, .problem-statement div { color: var(--text) !important; }
  .problem-statement .header .title { color: #FFFFFF !important; }
  .problem-statement .section-title { color: #FFFFFF !important; }
  
  /* Math colors */
  .MathJax, .mjx-chtml, .mjx-math { color: var(--text) !important; }
  
  /* Tables */
  table { width: 100%; border-collapse: collapse; margin: 8px 0; font-size: 13px; }
  th, td { border: 1px solid var(--border) !important; color: var(--text) !important; padding: 6px 10px; }
  th { background: var(--surface) !important; }
  
  /* Sample tests */
  .sample-tests .sample-test {
    border: 1px solid #2B2B2B !important;
    border-radius: 6px !important;
  }
  .sample-tests .input > .title,
  .sample-tests .output > .title {
    color: #A0A0A0 !important;
    background: #1E1E1E !important;
    font-size: 12.5px;
    font-family: var(--mono);
    padding: 5px 12px;
  }
  .sample-tests pre {
    background: #121212 !important;
    color: #CCCCCC !important;
    border: none !important;
    padding: 0 !important;
    margin: 0 !important;
  }
  .sample-tests pre > div, .sample-tests pre > span {
    display: block;
    padding: 3px 12px;
    min-height: 18px;
  }
  .sample-tests pre > div:nth-child(even) {
    background: rgba(255, 255, 255, 0.03) !important;
  }
  
  /* Code and tt */
  tt, code, .tex-math {
    background: rgba(255,255,255,0.08) !important;
    color: #E0E0E0 !important;
    font-family: var(--mono);
    border-radius: 4px;
    padding: 1px 4px;
  }
</style>
"""

out = re.sub(r"</head>", f"{CF_DARK_CSS}</head>", html, flags=re.IGNORECASE)
with open("test_rendered.html", "w") as f:
    f.write(out)
print("Saved to test_rendered.html")
