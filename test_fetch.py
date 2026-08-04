import urllib.request

url = "https://codeforces.com/contest/1/problem/A?locale=en"
req = urllib.request.Request(url, headers={
    "User-Agent": "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.165 Mobile Safari/537.36",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
    "Accept-Language": "en-US,en;q=0.9",
    "Connection": "keep-alive",
    "Referer": "https://codeforces.com/"
})

try:
    with urllib.request.urlopen(req) as response:
        html = response.read().decode('utf-8')
        print(f"HTML Length: {len(html)}")
        print(f"Contains </head>? {'</head>' in html.lower()}")
        with open("test_output.html", "w") as f:
            f.write(html)
except Exception as e:
    print(f"Error: {e}")
